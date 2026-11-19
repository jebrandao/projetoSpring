package br.com.app.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

// Teste de validação sem Spring: usa direto o Bean Validation
class EstudanteDTOValidationTest {

	private static Validator validator;

	@BeforeAll
	static void criarValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private EstudanteDTO dtoValido() {
		EstudanteDTO dto = new EstudanteDTO();
		dto.setNome("Ana");
		dto.setEmail("ana@email.com");
		dto.setDataNascimento(LocalDate.of(2000, 1, 10));
		return dto;
	}

	@Test
	void dtoValidoNaoTemViolacoes() {
		assertThat(validator.validate(dtoValido())).isEmpty();
	}

	@Test
	void nomeEmBrancoEInvalido() {
		EstudanteDTO dto = dtoValido();
		dto.setNome("   ");

		Set<ConstraintViolation<EstudanteDTO>> violacoes = validator.validate(dto);

		assertThat(violacoes).extracting(v -> v.getPropertyPath().toString())
				.containsExactly("nome");
	}

	@Test
	void emailMalFormadoEInvalido() {
		EstudanteDTO dto = dtoValido();
		dto.setEmail("nao-e-email");

		assertThat(validator.validate(dto)).hasSize(1);
	}

	@Test
	void dataDeNascimentoNoFuturoEInvalida() {
		EstudanteDTO dto = dtoValido();
		dto.setDataNascimento(LocalDate.now().plusDays(1));

		assertThat(validator.validate(dto)).hasSize(1);
	}
}
