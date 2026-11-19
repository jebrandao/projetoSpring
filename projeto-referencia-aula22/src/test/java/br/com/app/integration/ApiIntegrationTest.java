package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

// Teste de integração: sobe a aplicação inteira (com o banco H2 e a classe Init)
// e exercita o caminho completo: requisição > segurança > resource > service > banco.
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	// Faz o login de verdade em /auth/login e devolve o cabeçalho Authorization com o token
	private RequestPostProcessor bearer(String username, String senha) throws Exception {
		return Autenticacao.bearer(mockMvc, username, senha);
	}

	private static final String NOVO_ESTUDANTE = """
			{
			  "nome": "Teste Integração",
			  "email": "integracao@email.com",
			  "dataNascimento": "2001-03-15",
			  "endereco": { "logradouro": "Rua A", "numero": "1", "cep": "20000-000", "cidade": "Rio" }
			}""";

	@Test
	void semLoginRetorna401() throws Exception {
		mockMvc.perform(get("/estudantes")).andExpect(status().isUnauthorized());
	}

	@Test
	void alunoConsultaOsDadosCarregadosPelaInit() throws Exception {
		mockMvc.perform(get("/estudantes").with(bearer("aluno", "aluno123")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.nome == 'Alberto')]").exists());
	}

	@Test
	void alunoNaoPodeExcluir() throws Exception {
		mockMvc.perform(delete("/estudantes/1").with(bearer("aluno", "aluno123")))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCadastraConsultaEExcluiEstudante() throws Exception {
		String resposta = mockMvc.perform(post("/estudantes")
				.with(bearer("admin", "admin123"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(NOVO_ESTUDANTE))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.read(resposta, "$.id");
		assertThat(id).isPositive();

		mockMvc.perform(get("/estudantes/" + id).with(bearer("aluno", "aluno123")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.endereco.cidade").value("Rio"));

		mockMvc.perform(delete("/estudantes/" + id).with(bearer("admin", "admin123")))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/estudantes/" + id).with(bearer("aluno", "aluno123")))
				.andExpect(status().isNotFound());
	}

	@Test
	void adminMatriculaEstudanteNaTurma() throws Exception {
		mockMvc.perform(post("/turmas/2/estudantes/3").with(bearer("admin", "admin123")))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/turmas/2/estudantes").with(bearer("aluno", "aluno123")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == 3)]").exists());
	}

	@Test
	void matricularEmTurmaInexistenteRetorna404() throws Exception {
		mockMvc.perform(post("/turmas/999/estudantes/1").with(bearer("admin", "admin123")))
				.andExpect(status().isNotFound());
	}
}
