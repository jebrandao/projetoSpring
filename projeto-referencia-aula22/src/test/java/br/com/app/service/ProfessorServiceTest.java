package br.com.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.app.entity.Professor;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.ProfessorRepository;

@ExtendWith(MockitoExtension.class)
class ProfessorServiceTest {

	@Mock
	private ProfessorRepository professorRepo;

	@InjectMocks
	private ProfessorService professorService;

	private Professor novoProfessor(Integer matricula, String nome) {
		Professor professor = new Professor();
		professor.setMatricula(matricula);
		professor.setNome(nome);
		professor.setEspecializacao("Java");
		professor.setTelefone("(21) 90000-0000");
		return professor;
	}

	@Test
	void deveListarTodosOsProfessores() {
		when(professorRepo.findAll())
				.thenReturn(List.of(novoProfessor(1, "Marta"), novoProfessor(2, "Paulo")));

		assertThat(professorService.buscarTodosProfessores()).hasSize(2);
	}

	@Test
	void deveBuscarProfessorPelaMatricula() {
		when(professorRepo.findById(1)).thenReturn(Optional.of(novoProfessor(1, "Marta")));

		assertThat(professorService.buscarProfessorPorMatricula(1).getNome()).isEqualTo("Marta");
	}

	@Test
	void deveLancarExcecaoQuandoAMatriculaNaoExiste() {
		when(professorRepo.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> professorService.buscarProfessorPorMatricula(99))
				.isInstanceOf(ObjectNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	void deveAtualizarOsDadosDoProfessor() {
		when(professorRepo.findById(1)).thenReturn(Optional.of(novoProfessor(1, "Marta")));
		when(professorRepo.save(any(Professor.class))).thenAnswer(i -> i.getArgument(0));

		Professor atualizado = professorService
				.updateProfessor(1, novoProfessor(null, "Marta Souza"));

		assertThat(atualizado.getNome()).isEqualTo("Marta Souza");
		assertThat(atualizado.getMatricula()).isEqualTo(1);
	}

	@Test
	void deveExcluirProfessorExistente() {
		Professor professor = novoProfessor(1, "Marta");
		when(professorRepo.findById(1)).thenReturn(Optional.of(professor));

		professorService.deleteProfessor(1);

		verify(professorRepo).delete(professor);
	}
}
