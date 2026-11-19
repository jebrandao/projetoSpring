package br.com.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.app.entity.Professor;
import br.com.app.exception.ObjectNotFoundException;
import br.com.app.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfessorService {

	private final ProfessorRepository professorRepo;

	public Professor salvar(Professor professor) {
		return professorRepo.save(professor);
	}

	public List<Professor> buscarTodosProfessores() {
		return professorRepo.findAll();
	}

	public Professor buscarProfessorPorMatricula(Integer matricula) {
		return professorRepo.findById(matricula)
				.orElseThrow(() -> new ObjectNotFoundException(
						"Professor não encontrado: matrícula " + matricula));
	}

	public void deleteProfessor(Integer matricula) {
		Professor professor = buscarProfessorPorMatricula(matricula);
		professorRepo.delete(professor);
	}

	public Professor updateProfessor(Integer matricula, Professor professor) {
		Professor professorBD = buscarProfessorPorMatricula(matricula);
		professorBD.setNome(professor.getNome());
		professorBD.setEspecializacao(professor.getEspecializacao());
		professorBD.setTelefone(professor.getTelefone());
		return professorRepo.save(professorBD);
	}

	public Page<Professor> buscarProfessorPorPaginacao(PageRequest page) {
		return professorRepo.findAll(page);
	}
}
