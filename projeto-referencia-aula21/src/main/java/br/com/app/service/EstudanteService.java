package br.com.app.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import br.com.app.entity.Estudante;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.EstudanteRepository;
import br.com.app.specification.EstudanteSpecs;
import lombok.RequiredArgsConstructor;

@Service
// O Lombok gera o construtor com os campos final:
// é a injeção de dependência por construtor
@RequiredArgsConstructor
public class EstudanteService {

	private final EstudanteRepository estudanteRepo;

	public Estudante salvar(Estudante estudante) {
		return estudanteRepo.save(estudante);
	}

	public List<Estudante> buscarTodosEstudantes() {
		return estudanteRepo.findAll();
	}

	public Estudante buscarEstudantePorId(Integer id) {
		return estudanteRepo.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(
						"Estudante não encontrado: id " + id));
	}

	public void deleteEstudante(Integer id) {
		Estudante estudante = buscarEstudantePorId(id);
		estudanteRepo.delete(estudante); // exclusão lógica (@SoftDelete)
	}

	public Estudante updateEstudante(Integer id, Estudante estudante) {
		Estudante estudanteBD = buscarEstudantePorId(id);

		// Bloqueio otimista: o cliente editou uma versão que já foi alterada por outro
		if (estudante.getVersao() != null
				&& !estudante.getVersao().equals(estudanteBD.getVersao())) {
			throw new ObjectOptimisticLockingFailureException(Estudante.class, id);
		}

		estudanteBD.setNome(estudante.getNome());
		estudanteBD.setSobreNome(estudante.getSobreNome());
		estudanteBD.setEmail(estudante.getEmail());
		estudanteBD.setDataNascimento(estudante.getDataNascimento());
		estudanteBD.setTelefone(estudante.getTelefone());
		estudanteBD.setEndereco(estudante.getEndereco());
		return estudanteRepo.saveAndFlush(estudanteBD);
	}

	public Page<Estudante> buscarEstudantePorPaginacao(PageRequest page) {
		return estudanteRepo.findAll(page);
	}

	// Consulta dinâmica: só aplica os filtros que foram informados
	public Page<Estudante> buscarComFiltros(String nome, LocalDate nascidoApos,
			Integer turmaId, PageRequest page) {
		Specification<Estudante> filtros = Specification.allOf(
				EstudanteSpecs.nomeContem(nome),
				EstudanteSpecs.nascidoDepoisDe(nascidoApos),
				EstudanteSpecs.daTurma(turmaId));
		return estudanteRepo.findAll(filtros, page);
	}
}
