package br.com.app.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import br.com.app.config.CorsConfig;
import br.com.app.config.ModelConfig;
import br.com.app.config.SecurityConfig;
import br.com.app.entity.Estudante;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.service.EstudanteService;

// @WebMvcTest sobe só a camada web. O service é substituído por um mock.
// O JwtDecoder e o UserDetailsService também são mocks: aqui não validamos tokens de verdade,
// o processador jwt() já injeta uma autenticação com as autoridades desejadas.
@WebMvcTest(EstudanteResource.class)
@Import({ ModelConfig.class, SecurityConfig.class, CorsConfig.class })
class EstudanteResourceTest {

	private static final String JSON_VALIDO = """
			{ "nome": "Ana", "email": "ana@email.com" }""";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EstudanteService estudanteService;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private UserDetailsService userDetailsService;

	private RequestPostProcessor leitor() {
		return jwt().authorities(new SimpleGrantedAuthority("LEITURA"));
	}

	private RequestPostProcessor escritor() {
		return jwt().authorities(new SimpleGrantedAuthority("LEITURA"),
				new SimpleGrantedAuthority("ESCRITA"));
	}

	private Estudante estudante(Integer id, String nome) {
		Estudante estudante = new Estudante();
		estudante.setId(id);
		estudante.setNome(nome);
		estudante.setEmail(nome.toLowerCase() + "@email.com");
		return estudante;
	}

	@Test
	void semTokenRetorna401() throws Exception {
		mockMvc.perform(get("/estudantes")).andExpect(status().isUnauthorized());
	}

	@Test
	void quemTemLeituraPodeListar() throws Exception {
		given(estudanteService.buscarTodosEstudantes())
				.willReturn(List.of(estudante(1, "Ana"), estudante(2, "Bia")));

		mockMvc.perform(get("/estudantes").with(leitor()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].nome").value("Ana"));
	}

	@Test
	void idInexistenteRetorna404ComCorpoPadronizado() throws Exception {
		given(estudanteService.buscarEstudantePorId(99))
				.willThrow(new ObjectNotFoundException("Estudante não encontrado: id 99"));

		mockMvc.perform(get("/estudantes/99").with(leitor()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Estudante não encontrado: id 99"));
	}

	@Test
	void quemSoTemLeituraNaoPodeCadastrar() throws Exception {
		mockMvc.perform(post("/estudantes").with(leitor())
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON_VALIDO))
				.andExpect(status().isForbidden());
	}

	@Test
	void quemTemEscritaCadastraERetorna201() throws Exception {
		given(estudanteService.salvar(any(Estudante.class)))
				.willReturn(estudante(10, "Ana"));

		mockMvc.perform(post("/estudantes").with(escritor())
				.contentType(MediaType.APPLICATION_JSON)
				.content(JSON_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10));
	}

	@Test
	void dadosInvalidosRetornam400ComListaDeErros() throws Exception {
		mockMvc.perform(post("/estudantes").with(escritor())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "nome": "", "email": "invalido" }"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors.length()").value(2));
	}

	@Test
	void quemTemEscritaExcluiERetorna204() throws Exception {
		mockMvc.perform(delete("/estudantes/1").with(escritor()))
				.andExpect(status().isNoContent());

		verify(estudanteService).deleteEstudante(1);
	}
}
