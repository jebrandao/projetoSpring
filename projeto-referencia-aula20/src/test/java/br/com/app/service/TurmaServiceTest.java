package br.com.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.app.entity.Estudante;
import br.com.app.entity.Professor;
import br.com.app.entity.Turma;
import br.com.app.entity.Turno;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

	@Mock
	private TurmaRepository turmaRepo;

	@Mock
	private ProfessorService professorService;

	@Mock
	private EstudanteService estudanteService;

	@InjectMocks
	private TurmaService turmaService;

	private Turma novaTurma(Professor professor) {
		Turma turma = new Turma();
		turma.setId(1);
		turma.setNumero("20240007");
		turma.setTitulo("Desenvolvimento de Sistemas");
		turma.setTurno(Turno.NOITE);
		turma.setProfessor(professor);
		return turma;
	}

	@Test
	void deveMatricularEstudanteNaTurma() {
		Turma turma = novaTurma(null);
		Estudante estudante = new Estudante();
		estudante.setId(5);
		when(turmaRepo.findById(1)).thenReturn(Optional.of(turma));
		when(estudanteService.buscarEstudantePorId(5)).thenReturn(estudante);
		when(turmaRepo.save(any(Turma.class))).thenAnswer(i -> i.getArgument(0));

		Turma resultado = turmaService.matricularEstudante(1, 5);

		assertThat(resultado.getEstudantes()).containsExactly(estudante);
	}

	@Test
	void deveCarregarOProfessorCompletoAoSalvarTurma() {
		Professor soComMatricula = new Professor();
		soComMatricula.setMatricula(7);
		Professor completo = new Professor();
		completo.setMatricula(7);
		completo.setNome("Marta");
		when(professorService.buscarProfessorPorMatricula(7)).thenReturn(completo);
		when(turmaRepo.save(any(Turma.class))).thenAnswer(i -> i.getArgument(0));

		Turma salva = turmaService.salvar(novaTurma(soComMatricula));

		assertThat(salva.getProfessor().getNome()).isEqualTo("Marta");
	}

	@Test
	void deveFalharQuandoOProfessorNaoExiste() {
		Professor inexistente = new Professor();
		inexistente.setMatricula(99);
		when(professorService.buscarProfessorPorMatricula(99))
				.thenThrow(new ObjectNotFoundException("Professor não encontrado"));

		assertThatThrownBy(() -> turmaService.salvar(novaTurma(inexistente)))
				.isInstanceOf(ObjectNotFoundException.class);
	}
}
