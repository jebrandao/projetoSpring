package br.com.app.exception;

// Violação de uma regra de negócio ou dado inválido que o @Valid não detecta (gera 400)
public class RegraNegocioException extends RuntimeException {

	public RegraNegocioException(String message) {
		super(message);
	}
}
