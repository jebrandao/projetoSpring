package br.com.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import br.com.app.entity.Estudante;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.EstudanteRepository;

// Teste unitário: sem Spring, o repository é um "dublê" (mock) do Mockito
@ExtendWith(MockitoExtension.class)
class EstudanteServiceTest {

	@Mock
	private EstudanteRepository estudanteRepo;

	@InjectMocks
	private EstudanteService estudanteService;

	private Estudante novoEstudante(Integer id, String nome) {
		Estudante estudante = new Estudante();
		estudante.setId(id);
		estudante.setNome(nome);
		estudante.setEmail(nome.toLowerCase() + "@email.com");
		return estudante;
	}

	@Test
	void deveSalvarEstudante() {
		Estudante entrada = novoEstudante(null, "Ana");
		when(estudanteRepo.save(entrada)).thenReturn(novoEstudante(1, "Ana"));

		Estudante salvo = estudanteService.salvar(entrada);

		assertThat(salvo.getId()).isEqualTo(1);
		verify(estudanteRepo).save(entrada);
	}

	@Test
	void deveBuscarEstudantePorId() {
		when(estudanteRepo.findById(1)).thenReturn(Optional.of(novoEstudante(1, "Ana")));

		Estudante encontrado = estudanteService.buscarEstudantePorId(1);

		assertThat(encontrado.getNome()).isEqualTo("Ana");
	}

	@Test
	void deveLancarExcecaoQuandoIdNaoExiste() {
		when(estudanteRepo.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> estudanteService.buscarEstudantePorId(99))
				.isInstanceOf(ObjectNotFoundException.class)
				.hasMessageContaining("99");
	}

	@Test
	void deveAtualizarOsCamposDoEstudante() {
		Estudante existente = novoEstudante(1, "Ana");
		when(estudanteRepo.findById(1)).thenReturn(Optional.of(existente));
		when(estudanteRepo.saveAndFlush(any(Estudante.class)))
				.thenAnswer(invocacao -> invocacao.getArgument(0));

		Estudante novosDados = novoEstudante(null, "Ana Maria");
		Estudante atualizado = estudanteService.updateEstudante(1, novosDados);

		assertThat(atualizado.getNome()).isEqualTo("Ana Maria");
		assertThat(atualizado.getId()).isEqualTo(1);
	}

	@Test
	void deveRejeitarAtualizacaoComVersaoDesatualizada() {
		Estudante existente = novoEstudante(1, "Ana");
		existente.setVersao(3L);
		when(estudanteRepo.findById(1)).thenReturn(Optional.of(existente));

		Estudante telaAntiga = novoEstudante(null, "Ana Maria");
		telaAntiga.setVersao(2L); // o cliente editou a versão 2, mas o banco já está na 3

		assertThatThrownBy(() -> estudanteService.updateEstudante(1, telaAntiga))
				.isInstanceOf(ObjectOptimisticLockingFailureException.class);
		verify(estudanteRepo, never()).saveAndFlush(any(Estudante.class));
	}

	@Test
	void naoDeveExcluirEstudanteInexistente() {
		when(estudanteRepo.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> estudanteService.deleteEstudante(99))
				.isInstanceOf(ObjectNotFoundException.class);

		verify(estudanteRepo, never()).delete(any(Estudante.class));
	}
}
