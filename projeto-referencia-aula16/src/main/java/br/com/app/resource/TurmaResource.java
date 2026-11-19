package br.com.app.resource;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.app.dto.TurmaDTO;
import br.com.app.entity.Turma;
import br.com.app.service.TurmaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaResource {

	private final TurmaService turmaService;
	private final ModelMapper mapper;

	@GetMapping
	@Operation(description = "Retorna todas as turmas")
	public ResponseEntity<List<TurmaDTO>> buscarTodasTurmas() {
		List<TurmaDTO> lista = turmaService.buscarTodasTurmas()
				.stream()
				.map(turma -> mapper.map(turma, TurmaDTO.class))
				.toList();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/{id}")
	@Operation(description = "Retorna o registro da turma pelo id")
	public ResponseEntity<TurmaDTO> buscarTurmaPeloId(@PathVariable Integer id) {
		Turma turma = turmaService.buscarTurmaPorId(id);
		return ResponseEntity.ok(mapper.map(turma, TurmaDTO.class));
	}

	@PostMapping
	@Operation(description = "Cadastra uma turma")
	public ResponseEntity<TurmaDTO> cadastrarTurma(
			@Valid @RequestBody TurmaDTO turmaDTO) {
		Turma turma = mapper.map(turmaDTO, Turma.class);
		turma.setId(null); // garante um INSERT
		turma = turmaService.salvar(turma);
		TurmaDTO nova = mapper.map(turma, TurmaDTO.class);
		return ResponseEntity.status(HttpStatus.CREATED).body(nova);
	}

	@PutMapping("/{id}")
	@Operation(description = "Altera o registro da turma pelo id")
	public ResponseEntity<TurmaDTO> atualizarTurma(
			@PathVariable Integer id,
			@Valid @RequestBody TurmaDTO turmaDTO) {
		Turma turma = mapper.map(turmaDTO, Turma.class);
		turma = turmaService.updateTurma(id, turma);
		return ResponseEntity.ok(mapper.map(turma, TurmaDTO.class));
	}

	@DeleteMapping("/{id}")
	@Operation(description = "Remove uma turma pelo id")
	public ResponseEntity<Void> excluirTurma(@PathVariable Integer id) {
		turmaService.deleteTurma(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/paginacao")
	@Operation(description = "Retorna as turmas de forma paginada e ordenada")
	public PagedModel<TurmaDTO> buscarTurmaPorPaginacao(
			@RequestParam(defaultValue = "0") Integer pagina,
			@RequestParam(defaultValue = "5") Integer itensPorPagina,
			@RequestParam(defaultValue = "titulo") String ordenacao,
			@RequestParam(defaultValue = "ASC") String tipoOrdenacao) {
		Sort sort = Sort.by(Sort.Direction.fromString(tipoOrdenacao), ordenacao);
		PageRequest page = PageRequest.of(pagina, itensPorPagina, sort);
		Page<TurmaDTO> resultado = turmaService.buscarTurmaPorPaginacao(page)
				.map(turma -> mapper.map(turma, TurmaDTO.class));
		return new PagedModel<>(resultado);
	}
}
