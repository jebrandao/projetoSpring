package br.com.app.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

// Cadastro feito por um administrador: ele escolhe os papéis
public record UsuarioCadastroDTO(
		@NotBlank @Size(min = 3, max = 50) String username,
		@NotBlank @Size(min = 8, max = 72) String senha,
		@NotEmpty Set<String> papeis) {
}
