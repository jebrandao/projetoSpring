package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaJwtTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtEncoder jwtEncoder; // o mesmo encoder da aplicação (mesma chave)

	private String json(String username, String senha) {
		return """
				{ "username": "%s", "senha": "%s" }""".formatted(username, senha);
	}

	private String gerarToken(JwtEncoder encoder, Instant expiraEm) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("api-escola")
				.subject("admin")
				.issuedAt(expiraEm.minusSeconds(3600))
				.expiresAt(expiraEm)
				.claim("autoridades", List.of("LEITURA", "ESCRITA", "USUARIOS"))
				.build();
		return encoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
	}

	// ---------- login

	@Test
	void loginValidoDevolveUmTokenJwt() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("admin", "admin123")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEmSegundos").value(3600))
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void loginComSenhaErradaRetorna401ComMensagemGenerica() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("admin", "errada")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
	}

	@Test
	void loginDeUsuarioInexistenteTemaMesmaResposta() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(json("fantasma", "qualquer123")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Usuário ou senha inválidos"));
	}

	// ---------- validação do token

	@Test
	void tokenCarregaApenasAsAutoridadesDoNegocio() throws Exception {
		String token = Autenticacao.token(mockMvc, "aluno", "aluno123");
		String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]),
				StandardCharsets.UTF_8);

		assertThat(payload)
				.contains("\"sub\":\"aluno\"")
				.contains("ROLE_USER", "LEITURA")
				.doesNotContain("ESCRITA", "FACTOR_");
	}

	@Test
	void semTokenRetorna401() throws Exception {
		mockMvc.perform(get("/estudantes")).andExpect(status().isUnauthorized());
	}

	@Test
	void tokenSemSentidoRetorna401() throws Exception {
		mockMvc.perform(get("/estudantes").header(HttpHeaders.AUTHORIZATION, "Bearer abc.def.ghi"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenAdulteradoRetorna401() throws Exception {
		String token = Autenticacao.token(mockMvc, "admin", "admin123");
		// troca o conteúdo (payload) mantendo a assinatura antiga
		String[] partes = token.split("\\.");
		String payloadFalso = Base64.getUrlEncoder().withoutPadding().encodeToString(
				"""
				{"sub":"admin","autoridades":["LEITURA","ESCRITA","USUARIOS"],"exp":9999999999}"""
						.getBytes(StandardCharsets.UTF_8));
		String adulterado = partes[0] + "." + payloadFalso + "." + partes[2];

		mockMvc.perform(get("/estudantes").with(Autenticacao.comToken(adulterado)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenExpiradoRetorna401() throws Exception {
		String expirado = gerarToken(jwtEncoder, Instant.now().minusSeconds(600));

		mockMvc.perform(get("/estudantes").with(Autenticacao.comToken(expirado)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenAssinadoComOutraChaveRetorna401() throws Exception {
		byte[] outraChave = "outra-chave-secreta-com-mais-de-32-caracteres"
				.getBytes(StandardCharsets.UTF_8);
		JwtEncoder encoderDoAtacante = new NimbusJwtEncoder(
				new ImmutableSecret<>(new SecretKeySpec(outraChave, "HmacSHA256")));
		String falso = gerarToken(encoderDoAtacante, Instant.now().plusSeconds(3600));

		mockMvc.perform(get("/estudantes").with(Autenticacao.comToken(falso)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenValidoGeradoPelaAplicacaoEhAceito() throws Exception {
		String valido = gerarToken(jwtEncoder, Instant.now().plusSeconds(3600));

		mockMvc.perform(get("/estudantes").with(Autenticacao.comToken(valido)))
				.andExpect(status().isOk());
	}

	@Test
	void httpBasicNaoEhMaisAceito() throws Exception {
		String basic = Base64.getEncoder().encodeToString("admin:admin123".getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(get("/estudantes").header(HttpHeaders.AUTHORIZATION, "Basic " + basic))
				.andExpect(status().isUnauthorized());
	}

	// ---------- autorização por permissão

	@Test
	void usuarioComPapelUserLeMasNaoEscreveNemGerenciaUsuarios() throws Exception {
		var aluno = Autenticacao.bearer(mockMvc, "aluno", "aluno123");

		mockMvc.perform(get("/estudantes").with(aluno)).andExpect(status().isOk());
		mockMvc.perform(post("/estudantes").with(aluno).contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\":\"X\",\"email\":\"x@x.com\"}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/usuarios").with(aluno)).andExpect(status().isForbidden());
	}

	// ---------- cadastro de usuários

	@Test
	void adminListaUsuariosSemExporSenhas() throws Exception {
		var admin = Autenticacao.bearer(mockMvc, "admin", "admin123");

		mockMvc.perform(get("/usuarios").with(admin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.username == 'admin' && @.papeis[0] == 'ADMIN')]").exists())
				.andExpect(jsonPath("$[0].password").doesNotExist())
				.andExpect(jsonPath("$[0].senha").doesNotExist());
	}

	@Test
	void adminCadastraUsuarioQueEntaoConseguePassarPeloLogin() throws Exception {
		var admin = Autenticacao.bearer(mockMvc, "admin", "admin123");
		mockMvc.perform(post("/usuarios").with(admin).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "username": "secretaria", "senha": "senha-forte-1", "papeis": ["USER"] }"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("secretaria"))
				.andExpect(jsonPath("$.papeis[0]").value("USER"));

		var secretaria = Autenticacao.bearer(mockMvc, "secretaria", "senha-forte-1");
		mockMvc.perform(get("/estudantes").with(secretaria)).andExpect(status().isOk());
	}

	@Test
	void cadastroInvalidoRetorna400() throws Exception {
		var admin = Autenticacao.bearer(mockMvc, "admin", "admin123");

		// senha curta e nenhum papel
		mockMvc.perform(post("/usuarios").with(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"username\": \"novo\", \"senha\": \"123\", \"papeis\": [] }"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(2));

		// papel que não existe
		mockMvc.perform(post("/usuarios").with(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"username\": \"novo\", \"senha\": \"senha-forte-1\", \"papeis\": [\"DONO\"] }"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Papel inexistente: DONO"));

		// login já usado
		mockMvc.perform(post("/usuarios").with(admin).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"username\": \"admin\", \"senha\": \"senha-forte-1\", \"papeis\": [\"USER\"] }"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void autoCadastroCriaUsuarioComPapelUserSomente() throws Exception {
		mockMvc.perform(post("/auth/registrar").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"username\": \"visitante\", \"senha\": \"senha-forte-2\" }"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.papeis[0]").value("USER"));

		var visitante = Autenticacao.bearer(mockMvc, "visitante", "senha-forte-2");
		mockMvc.perform(get("/estudantes").with(visitante)).andExpect(status().isOk());
		mockMvc.perform(get("/usuarios").with(visitante)).andExpect(status().isForbidden());
	}

	// ---------- CORS

	@Test
	void corsPermiteAOrigemConfigurada() throws Exception {
		mockMvc.perform(options("/estudantes")
				.header(HttpHeaders.ORIGIN, "http://localhost:3000")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
	}

	@Test
	void corsBloqueiaOrigemDesconhecida() throws Exception {
		mockMvc.perform(options("/estudantes")
				.header(HttpHeaders.ORIGIN, "http://site-malicioso.com")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
				.andExpect(status().isForbidden());
	}
}
