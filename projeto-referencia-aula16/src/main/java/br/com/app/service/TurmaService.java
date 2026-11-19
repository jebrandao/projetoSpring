package br.com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.app.entity.Turma;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TurmaService {

	private final TurmaRepository turmaRepo;

	public Turma salvar(Turma turma) {
		return turmaRepo.save(turma);
	}

	public List<Turma> buscarTodasTurmas() {
		return turmaRepo.findAll();
	}

	public Turma buscarTurmaPorId(Integer id) {
		return turmaRepo.findById(id)
				.orElseThrow(() -> new ObjectNotFoundException(
						"Turma não encontrada: id " + id));
	}

	public void deleteTurma(Integer id) {
		Turma turma = buscarTurmaPorId(id);
		turmaRepo.delete(turma);
	}

	public Turma updateTurma(Integer id, Turma turma) {
		Turma turmaBD = buscarTurmaPorId(id);
		turmaBD.setNumero(turma.getNumero());
		turmaBD.setTitulo(turma.getTitulo());
		turmaBD.setTurno(turma.getTurno());
		return turmaRepo.save(turmaBD);
	}

	public Page<Turma> buscarTurmaPorPaginacao(PageRequest page) {
		return turmaRepo.findAll(page);
	}
}
