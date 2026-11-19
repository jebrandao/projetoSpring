package br.com.app.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

// Demonstra por que NUNCA se edita uma migração que já foi aplicada:
// o Flyway guarda o checksum (impressão digital) de cada script e recusa o arquivo alterado.
class ImutabilidadeDasMigracoesTest {

	@TempDir
	Path pasta;

	@Test
	void migracaoAlteradaDepoisDeAplicadaEhRejeitada() throws Exception {
		Path script = pasta.resolve("V1__criar_tabela_teste.sql");
		Files.writeString(script, "CREATE TABLE teste (id INT PRIMARY KEY);");

		Flyway flyway = Flyway.configure()
				.dataSource("jdbc:h2:mem:imutabilidade;DB_CLOSE_DELAY=-1", "sa", "")
				.locations("filesystem:" + pasta)
				.load();
		flyway.migrate(); // aplica a V1

		// alguém "corrige" o script depois que ele já rodou...
		Files.writeString(script, "CREATE TABLE teste (id INT PRIMARY KEY, nome VARCHAR(50));");

		// ...e o Flyway percebe que o conteúdo mudou
		assertThatThrownBy(flyway::validate)
				.isInstanceOf(FlywayValidateException.class)
				.hasMessageContaining("checksum mismatch");
	}
}
