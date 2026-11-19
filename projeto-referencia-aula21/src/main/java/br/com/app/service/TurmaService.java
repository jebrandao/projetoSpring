package br.com.app.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.app.dto.TurmaResumoDTO;
import br.com.app.entity.Estudante;
import br.com.app.entity.Professor;
import br.com.app.entity.Turma;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TurmaService {

	private final TurmaRepository turmaRepo;
	private final ProfessorService professorService;
	private final EstudanteService estudanteService;

	public Turma salvar(Turma turma) {
		turma.setProfessor(carregarProfessor(turma.getProfessor()));
		return turmaRepo.save(turma);
	}

	// Carrega o professor junto com as turmas (um único SELECT)
	public List<Turma> buscarTodasTurmas() {
		return turmaRepo.findAllComProfessor();
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
		turmaBD.setProfessor(carregarProfessor(turma.getProfessor()));
		return turmaRepo.save(turmaBD);
	}

	public Page<Turma> buscarTurmaPorPaginacao(PageRequest page) {
		return turmaRepo.findAll(page);
	}

	public List<TurmaResumoDTO> resumoDasTurmas() {
		return turmaRepo.resumoDasTurmas();
	}

	// Relação N:N - matricula um estudante em uma turma
	@Transactional
	public Turma matricularEstudante(Integer turmaId, Integer estudanteId) {
		Turma turma = buscarTurmaPorId(turmaId);
		Estudante estudante = estudanteService.buscarEstudantePorId(estudanteId);
		turma.getEstudantes().add(estudante);
		return turmaRepo.save(turma);
	}

	// Relação N:N - remove a matrícula de um estudante
	@Transactional
	public Turma desmatricularEstudante(Integer turmaId, Integer estudanteId) {
		Turma turma = buscarTurmaPorId(turmaId);
		Estudante estudante = estudanteService.buscarEstudantePorId(estudanteId);
		turma.getEstudantes().remove(estudante);
		return turmaRepo.save(turma);
	}

	// Duas operações que precisam ser atômicas: ou acontecem as duas, ou nenhuma.
	// Se a turma de destino não existir, o RuntimeException desfaz (rollback)
	// também a remoção da turma de origem, que já tinha sido enviada ao banco.
	@Transactional
	public void transferirEstudante(Integer origemId, Integer destinoId,
			Integer estudanteId) {
		Turma origem = buscarTurmaPorId(origemId);
		Estudante estudante = estudanteService.buscarEstudantePorId(estudanteId);

		origem.getEstudantes().remove(estudante);
		turmaRepo.saveAndFlush(origem); // o DELETE já foi para o banco...

		Turma destino = buscarTurmaPorId(destinoId); // ...mas, se isto falhar, tudo é desfeito
		destino.getEstudantes().add(estudante);
		turmaRepo.save(destino);
	}

	// readOnly: a coleção LAZY precisa ser lida dentro da transação
	@Transactional(readOnly = true)
	public List<Estudante> buscarEstudantesDaTurma(Integer turmaId) {
		Turma turma = buscarTurmaPorId(turmaId);
		return new ArrayList<>(turma.getEstudantes());
	}

	// O DTO traz só a matrícula do professor: buscamos o professor completo
	// (ou lançamos 404 se ele não existir)
	private Professor carregarProfessor(Professor professor) {
		if (professor == null || professor.getMatricula() == null) {
			return null;
		}
		return professorService
				.buscarProfessorPorMatricula(professor.getMatricula());
	}
}
