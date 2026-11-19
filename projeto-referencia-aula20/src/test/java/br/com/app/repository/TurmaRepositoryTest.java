package br.com.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.app.entity.Turma;
import br.com.app.entity.Turno;

// @DataJpaTest sobe só a camada JPA (entidades e repositories) com um banco em memória.
// Cada teste roda dentro de uma transação que sofre rollback no final.
@DataJpaTest
class TurmaRepositoryTest {

	@Autowired
	private TurmaRepository turmaRepo;

	private Turma novaTurma(String numero) {
		Turma turma = new Turma();
		turma.setNumero(numero);
		turma.setTitulo("Redes");
		turma.setTurno(Turno.MANHA);
		return turma;
	}

	@Test
	void deveSalvarEBuscarTurma() {
		Turma salva = turmaRepo.save(novaTurma("20250001"));

		assertThat(salva.getId()).isNotNull();
		assertThat(turmaRepo.findById(salva.getId())).isPresent();
	}

	@Test
	void naoDevePermitirDuasTurmasComOMesmoNumero() {
		turmaRepo.saveAndFlush(novaTurma("20250002"));

		assertThatThrownBy(() -> turmaRepo.saveAndFlush(novaTurma("20250002")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
