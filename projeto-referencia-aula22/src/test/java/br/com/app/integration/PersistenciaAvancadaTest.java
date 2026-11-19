package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

// Estes testes NÃO são @Transactional: precisamos ver o que realmente foi gravado no banco
@SpringBootTest
@AutoConfigureMockMvc
class PersistenciaAvancadaTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private JdbcTemplate jdbc;

	// Faz o login de verdade em /auth/login e devolve o cabeçalho Authorization com o token
	private RequestPostProcessor admin() throws Exception {
		return Autenticacao.bearer(mockMvc, "admin", "admin123");
	}

	private RequestPostProcessor aluno() throws Exception {
		return Autenticacao.bearer(mockMvc, "aluno", "aluno123");
	}

	private int criarEstudante(String email) throws Exception {
		String json = """
				{ "nome": "Persistência", "email": "%s" }""".formatted(email);
		String resposta = mockMvc.perform(post("/estudantes").with(admin())
				.contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.id");
	}

	private int criarTurma(String numero) throws Exception {
		String json = """
				{ "numero": "%s", "titulo": "Turma de teste", "turno": "NOITE" }""".formatted(numero);
		String resposta = mockMvc.perform(post("/turmas").with(admin())
				.contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.id");
	}

	@Test
	void auditoriaRegistraQuemCriouOUsuarioDoLogin() throws Exception {
		int id = criarEstudante("auditoria@email.com");

		mockMvc.perform(get("/estudantes/" + id).with(aluno()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.criadoPor").value("admin"))
				.andExpect(jsonPath("$.criadoEm").exists());
	}

	@Test
	void dadosCarregadosPelaInitFicamComoUsuarioSistema() throws Exception {
		mockMvc.perform(get("/estudantes/1").with(aluno()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.criadoPor").value("sistema"));
	}

	@Test
	void exclusaoLogicaMantemALinhaNoBancoMasEscondeDaApi() throws Exception {
		int id = criarEstudante("exclusao@email.com");

		mockMvc.perform(delete("/estudantes/" + id).with(admin())).andExpect(status().isNoContent());

		mockMvc.perform(get("/estudantes/" + id).with(aluno())).andExpect(status().isNotFound());
		Boolean excluido = jdbc.queryForObject(
				"SELECT excluido FROM estudante WHERE id = ?", Boolean.class, id);
		assertThat(excluido).isTrue();
	}

	@Test
	void bloqueioOtimistaRejeitaAlteracaoComVersaoDesatualizada() throws Exception {
		int id = criarEstudante("versao@email.com");
		String corpo = """
				{ "nome": "Alterado", "email": "versao@email.com", "versao": %d }""";

		// 1ª alteração com a versão 0: funciona e a versão passa a ser 1
		mockMvc.perform(put("/estudantes/" + id).with(admin())
				.contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(0)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.versao").value(1));

		// 2ª alteração ainda com a versão 0 (tela desatualizada): conflito
		mockMvc.perform(put("/estudantes/" + id).with(admin())
				.contentType(MediaType.APPLICATION_JSON).content(corpo.formatted(0)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("Optimistic lock"));
	}

	@Test
	void transferenciaEntreTurmasFuncionaQuandoTudoExiste() throws Exception {
		int estudante = criarEstudante("transfere@email.com");
		int origem = criarTurma("20980001");
		int destino = criarTurma("20980002");
		mockMvc.perform(post("/turmas/" + origem + "/estudantes/" + estudante).with(admin()))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/turmas/" + origem + "/estudantes/" + estudante
				+ "/transferir/" + destino).with(admin()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/turmas/" + origem + "/estudantes").with(aluno()))
				.andExpect(jsonPath("$.length()").value(0));
		mockMvc.perform(get("/turmas/" + destino + "/estudantes").with(aluno()))
				.andExpect(jsonPath("$[0].id").value(estudante));
	}

	@Test
	void transferenciaParaTurmaInexistenteFazRollbackDaRemocao() throws Exception {
		int estudante = criarEstudante("rollback@email.com");
		int origem = criarTurma("20980003");
		mockMvc.perform(post("/turmas/" + origem + "/estudantes/" + estudante).with(admin()))
				.andExpect(status().isNoContent());

		// o destino 99999 não existe: ocorre 404...
		mockMvc.perform(post("/turmas/" + origem + "/estudantes/" + estudante
				+ "/transferir/99999").with(admin()))
				.andExpect(status().isNotFound());

		// ...e a remoção da turma de origem foi desfeita pelo @Transactional
		mockMvc.perform(get("/turmas/" + origem + "/estudantes").with(aluno()))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(estudante));
	}

	@Test
	void endpointDeBuscaComFiltros() throws Exception {
		criarEstudante("busca1@email.com");

		mockMvc.perform(get("/estudantes/busca?nome=persist").with(aluno()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()")
						.value(greaterThanOrEqualTo(1)))
				.andExpect(jsonPath("$.page.totalElements").exists());
	}

	@Test
	void relatorioDeResumoDasTurmas() throws Exception {
		mockMvc.perform(get("/turmas/resumo").with(aluno()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.numero == '20240007')].professor")
						.value("Marta Oliveira Santos"));
	}
}
