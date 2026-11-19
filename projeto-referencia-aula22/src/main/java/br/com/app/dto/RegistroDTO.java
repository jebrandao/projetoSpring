package br.com.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Auto-cadastro: a pessoa informa só usuário e senha (o papel é sempre USER)
public record RegistroDTO(
		@NotBlank @Size(min = 3, max = 50) String username,
		@NotBlank @Size(min = 8, max = 72) String senha) {
}
