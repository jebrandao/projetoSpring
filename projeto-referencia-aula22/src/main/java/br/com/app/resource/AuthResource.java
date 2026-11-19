package br.com.app.resource;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.app.dto.LoginDTO;
import br.com.app.dto.RegistroDTO;
import br.com.app.dto.TokenDTO;
import br.com.app.dto.UsuarioDTO;
import br.com.app.entity.Usuario;
import br.com.app.service.TokenService;
import br.com.app.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthResource {

	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;
	private final UsuarioService usuarioService;

	@PostMapping("/login")
	@Operation(description = "Autentica e devolve um token JWT")
	public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginDTO login) {
		Authentication autenticacao = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(
						login.username(), login.senha()));
		return ResponseEntity.ok(tokenService.gerarToken(autenticacao));
	}

	@PostMapping("/registrar")
	@Operation(description = "Auto-cadastro: cria um usuário com o papel USER")
	public ResponseEntity<UsuarioDTO> registrar(
			@Valid @RequestBody RegistroDTO registro) {
		Usuario usuario = usuarioService.criarUsuario(
				registro.username(), registro.senha(), Set.of("USER"));
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(UsuarioDTO.de(usuario));
	}
}
