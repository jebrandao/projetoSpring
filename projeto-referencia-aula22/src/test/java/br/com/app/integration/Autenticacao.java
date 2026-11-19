package br.com.app.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

// Apoio aos testes: faz o login de verdade em /auth/login e devolve um processador
// que acrescenta o cabeçalho "Authorization: Bearer <token>" às requisições.
public final class Autenticacao {

	private Autenticacao() {
	}

	public static String token(MockMvc mockMvc, String username, String senha)
			throws Exception {
		String corpo = """
				{ "username": "%s", "senha": "%s" }""".formatted(username, senha);
		String resposta = mockMvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.token");
	}

	public static RequestPostProcessor bearer(MockMvc mockMvc, String username,
			String senha) throws Exception {
		return comToken(token(mockMvc, username, senha));
	}

	public static RequestPostProcessor comToken(String token) {
		return requisicao -> {
			requisicao.addHeader("Authorization", "Bearer " + token);
			return requisicao;
		};
	}
}
