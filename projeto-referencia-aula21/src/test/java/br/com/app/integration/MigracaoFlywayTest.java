package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MigracaoFlywayTest {

	@Autowired
	private JdbcTemplate jdbc;

	@Autowired
	private MockMvc mockMvc;

	@Test
	void todasAsMigracoesForamAplicadasComSucesso() {
		// o Flyway registra cada migração na tabela flyway_schema_history
		String sql = """
				SELECT "version" FROM "flyway_schema_history"
				WHERE "success" = TRUE AND "version" IS NOT NULL
				ORDER BY "installed_rank\"""";
		List<String> versoes = jdbc.queryForList(sql, String.class);

		assertThat(versoes).containsExactly("1", "2", "3", "4");
	}

	@Test
	void colunaCriadaPelaMigracaoV2ExisteNaTabela() {
		Integer colunas = jdbc.queryForObject(
				"SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
						+ "WHERE UPPER(TABLE_NAME) = 'ESTUDANTE' AND UPPER(COLUMN_NAME) = 'TELEFONE'",
				Integer.class);

		assertThat(colunas).isEqualTo(1);
	}

	@Test
	void emailDuplicadoViolaAConstraintDaMigracaoV3() throws Exception {
		String json = """
				{ "nome": "Duplicado", "email": "duplicado@email.com", "telefone": "(21) 90000-0000" }""";

		mockMvc.perform(post("/estudantes").with(httpBasic("admin", "admin123"))
				.contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.telefone").value("(21) 90000-0000"));

		mockMvc.perform(post("/estudantes").with(httpBasic("admin", "admin123"))
				.contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isConflict());
	}
}
