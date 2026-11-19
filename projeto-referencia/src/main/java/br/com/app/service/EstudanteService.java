package br.com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.app.entity.Estudante;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.EstudanteRepository;
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
		estudanteRepo.delete(estudante);
	}

	public Estudante updateEstudante(Integer id, Estudante estudante) {
		Estudante estudanteBD = buscarEstudantePorId(id);
		estudanteBD.setNome(estudante.getNome());
		estudanteBD.setSobreNome(estudante.getSobreNome());
		estudanteBD.setEmail(estudante.getEmail());
		estudanteBD.setDataNascimento(estudante.getDataNascimento());
		estudanteBD.setEndereco(estudante.getEndereco());
		return estudanteRepo.save(estudanteBD);
	}

	public Page<Estudante> buscarEstudantePorPaginacao(PageRequest page) {
		return estudanteRepo.findAll(page);
	}
}
