package br.com.app.dto;

public record TokenDTO(
		String token,
		String tipo,
		long expiraEmSegundos) {
}
