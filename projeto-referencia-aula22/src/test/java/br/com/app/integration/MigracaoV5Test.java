package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

// Teste de migração com dados reais: sem Spring, só o Flyway e um banco H2 exclusivo.
// 1) aplica as migrações até a V4; 2) insere usuários no modelo antigo (coluna perfil);
// 3) aplica a V5 e confere se os usuários receberam os papéis correspondentes.
class MigracaoV5Test {

	private static final String URL = "jdbc:h2:mem:migracao_v5;MODE=MySQL;DB_CLOSE_DELAY=-1";

	private Flyway flyway(String versaoAlvo) {
		return Flyway.configure()
				.dataSource(URL, "sa", "")
				.locations("classpath:db/migration")
				.target(versaoAlvo)
				.load();
	}

	@Test
	void usuariosAntigosRecebemOPapelQueEstavaNaColunaPerfil() throws Exception {
		flyway("4").migrate();

		try (Connection conexao = DriverManager.getConnection(URL, "sa", "");
				Statement sql = conexao.createStatement()) {
			String insert = "INSERT INTO usuario (username, password, perfil) VALUES ";
			sql.executeUpdate(insert + "('antigo_admin', 'x', 'ADMIN')");
			sql.executeUpdate(insert + "('antigo_user', 'y', 'USER')");
		}

		flyway("5").migrate();

		List<String> resultado = new ArrayList<>();
		try (Connection conexao = DriverManager.getConnection(URL, "sa", "");
				Statement sql = conexao.createStatement();
				ResultSet linhas = sql.executeQuery("""
						SELECT u.username, p.nome
						FROM usuario u
						JOIN usuario_papel up ON up.usuario_id = u.id
						JOIN papel p ON p.id = up.papel_id
						ORDER BY u.username""")) {
			while (linhas.next()) {
				resultado.add(linhas.getString(1) + ":" + linhas.getString(2));
			}
		}
		assertThat(resultado).containsExactly("antigo_admin:ADMIN", "antigo_user:USER");

		try (Connection conexao = DriverManager.getConnection(URL, "sa", "");
				Statement sql = conexao.createStatement();
				ResultSet colunas = sql.executeQuery("""
						SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
						WHERE UPPER(TABLE_NAME) = 'USUARIO' AND UPPER(COLUMN_NAME) = 'PERFIL'""")) {
			colunas.next();
			assertThat(colunas.getInt(1)).isZero(); // a coluna antiga foi removida
		}
	}
}
