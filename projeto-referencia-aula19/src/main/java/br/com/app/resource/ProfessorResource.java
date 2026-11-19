package br.com.app.resource;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.app.dto.ProfessorDTO;
import br.com.app.entity.Professor;
import br.com.app.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/professores")
@RequiredArgsConstructor
public class ProfessorResource {

	private final ProfessorService professorService;
	private final ModelMapper mapper;

	@GetMapping
	@Operation(description = "Retorna todos os professores")
	public ResponseEntity<List<ProfessorDTO>> buscarTodosProfessores() {
		List<ProfessorDTO> lista = professorService.buscarTodosProfessores()
				.stream()
				.map(professor -> mapper.map(professor, ProfessorDTO.class))
				.toList();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/{matricula}")
	@Operation(description = "Retorna o registro do professor pela matrícula")
	public ResponseEntity<ProfessorDTO> buscarProfessorPelaMatricula(
			@PathVariable Integer matricula) {
		Professor professor = professorService
				.buscarProfessorPorMatricula(matricula);
		return ResponseEntity.ok(mapper.map(professor, ProfessorDTO.class));
	}

	@PostMapping
	@Operation(description = "Cadastra um professor")
	public ResponseEntity<ProfessorDTO> cadastrarProfessor(
			@Valid @RequestBody ProfessorDTO professorDTO) {
		Professor professor = mapper.map(professorDTO, Professor.class);
		professor.setMatricula(null); // garante um INSERT
		professor = professorService.salvar(professor);
		ProfessorDTO novo = mapper.map(professor, ProfessorDTO.class);
		return ResponseEntity.status(HttpStatus.CREATED).body(novo);
	}

	@PutMapping("/{matricula}")
	@Operation(description = "Altera o registro do professor pela matrícula")
	public ResponseEntity<ProfessorDTO> atualizarProfessor(
			@PathVariable Integer matricula,
			@Valid @RequestBody ProfessorDTO professorDTO) {
		Professor professor = mapper.map(professorDTO, Professor.class);
		professor = professorService.updateProfessor(matricula, professor);
		return ResponseEntity.ok(mapper.map(professor, ProfessorDTO.class));
	}

	@DeleteMapping("/{matricula}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(description = "Remove um professor pela matrícula")
	public ResponseEntity<Void> excluirProfessor(@PathVariable Integer matricula) {
		professorService.deleteProfessor(matricula);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/paginacao")
	@Operation(description = "Retorna os professores de forma paginada e ordenada")
	public PagedModel<ProfessorDTO> buscarProfessorPorPaginacao(
			@RequestParam(defaultValue = "0") Integer pagina,
			@RequestParam(defaultValue = "5") Integer itensPorPagina,
			@RequestParam(defaultValue = "nome") String ordenacao,
			@RequestParam(defaultValue = "ASC") String tipoOrdenacao) {
		Sort sort = Sort.by(Sort.Direction.fromString(tipoOrdenacao), ordenacao);
		PageRequest page = PageRequest.of(pagina, itensPorPagina, sort);
		Page<ProfessorDTO> resultado = professorService
				.buscarProfessorPorPaginacao(page)
				.map(professor -> mapper.map(professor, ProfessorDTO.class));
		return new PagedModel<>(resultado);
	}
}
