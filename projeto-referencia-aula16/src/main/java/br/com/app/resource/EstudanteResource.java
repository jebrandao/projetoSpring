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

import br.com.app.dto.EstudanteDTO;
import br.com.app.entity.Estudante;
import br.com.app.service.EstudanteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estudantes")
@RequiredArgsConstructor
public class EstudanteResource {

	private final EstudanteService estudanteService;
	private final ModelMapper mapper;

	@GetMapping
	@Operation(description = "Retorna todos os estudantes")
	public ResponseEntity<List<EstudanteDTO>> buscarTodosEstudantes() {
		List<EstudanteDTO> lista = estudanteService.buscarTodosEstudantes()
				.stream()
				.map(estudante -> mapper.map(estudante, EstudanteDTO.class))
				.toList();
		return ResponseEntity.ok(lista);
	}

	@GetMapping("/{id}")
	@Operation(description = "Retorna o registro do estudante pelo id")
	public ResponseEntity<EstudanteDTO> buscarEstudantePeloId(
			@PathVariable Integer id) {
		Estudante estudante = estudanteService.buscarEstudantePorId(id);
		return ResponseEntity.ok(mapper.map(estudante, EstudanteDTO.class));
	}

	@PostMapping
	@Operation(description = "Cadastra um estudante")
	public ResponseEntity<EstudanteDTO> cadastrarEstudante(
			@Valid @RequestBody EstudanteDTO estudanteDTO) {
		Estudante estudante = mapper.map(estudanteDTO, Estudante.class);
		estudante.setId(null); // garante um INSERT, mesmo que o cliente envie um id
		estudante = estudanteService.salvar(estudante);
		EstudanteDTO novo = mapper.map(estudante, EstudanteDTO.class);
		return ResponseEntity.status(HttpStatus.CREATED).body(novo);
	}

	@PutMapping("/{id}")
	@Operation(description = "Altera o registro do estudante pelo id")
	public ResponseEntity<EstudanteDTO> atualizarEstudante(
			@PathVariable Integer id,
			@Valid @RequestBody EstudanteDTO estudanteDTO) {
		Estudante estudante = mapper.map(estudanteDTO, Estudante.class);
		estudante = estudanteService.updateEstudante(id, estudante);
		return ResponseEntity.ok(mapper.map(estudante, EstudanteDTO.class));
	}

	@DeleteMapping("/{id}")
	@Operation(description = "Remove um estudante pelo id")
	public ResponseEntity<Void> excluirEstudante(@PathVariable Integer id) {
		estudanteService.deleteEstudante(id);
		return ResponseEntity.noContent().build();
	}

	// Exemplo: /estudantes/paginacao?pagina=0&itensPorPagina=2&ordenacao=nome
	@GetMapping("/paginacao")
	@Operation(description = "Retorna os estudantes de forma paginada e ordenada")
	public PagedModel<EstudanteDTO> buscarEstudantePorPaginacao(
			@RequestParam(defaultValue = "0") Integer pagina,
			@RequestParam(defaultValue = "5") Integer itensPorPagina,
			@RequestParam(defaultValue = "nome") String ordenacao,
			@RequestParam(defaultValue = "ASC") String tipoOrdenacao) {
		Sort sort = Sort.by(Sort.Direction.fromString(tipoOrdenacao), ordenacao);
		PageRequest page = PageRequest.of(pagina, itensPorPagina, sort);
		Page<EstudanteDTO> resultado = estudanteService
				.buscarEstudantePorPaginacao(page)
				.map(estudante -> mapper.map(estudante, EstudanteDTO.class));
		return new PagedModel<>(resultado);
	}
}
