package br.com.app.resource;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.app.dto.UsuarioCadastroDTO;
import br.com.app.dto.UsuarioDTO;
import br.com.app.entity.Usuario;
import br.com.app.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Acesso restrito à permissão USUARIOS (regra definida no SecurityConfig)
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioResource {

	private final UsuarioService usuarioService;

	@GetMapping
	@Operation(description = "Lista os usuários e seus papéis")
	public ResponseEntity<List<UsuarioDTO>> listar() {
		List<UsuarioDTO> lista = usuarioService.listarUsuarios().stream()
				.map(UsuarioDTO::de)
				.toList();
		return ResponseEntity.ok(lista);
	}

	@PostMapping
	@Operation(description = "Cadastra um usuário com os papéis informados")
	public ResponseEntity<UsuarioDTO> cadastrar(
			@Valid @RequestBody UsuarioCadastroDTO dto) {
		Usuario usuario = usuarioService.criarUsuario(
				dto.username(), dto.senha(), dto.papeis());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(UsuarioDTO.de(usuario));
	}
}
