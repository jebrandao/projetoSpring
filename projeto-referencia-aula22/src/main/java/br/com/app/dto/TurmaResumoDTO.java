package br.com.app.dto;

// Um record é uma classe imutável e concisa (Java 16+): ideal para DTOs só de leitura
public record TurmaResumoDTO(
		String numero,
		String titulo,
		String professor,
		Long totalEstudantes) {
}
