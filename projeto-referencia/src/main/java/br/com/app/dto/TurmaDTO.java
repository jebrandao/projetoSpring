package br.com.app.dto;

import br.com.app.commons.ExampleValues;
import br.com.app.entity.Turno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = ExampleValues.EXEMPLO_TURMA)
public class TurmaDTO {

	private Integer id;

	// Formato AAAANNNN (8 dígitos). Exemplo: 20240007
	@NotBlank
	@Pattern(regexp = "\\d{8}",
			message = "O número deve ter 8 dígitos (AAAANNNN)")
	private String numero;

	@NotBlank
	private String titulo;

	@NotNull
	private Turno turno;

	// Matrícula do professor responsável (relação N:1)
	private Integer professorMatricula;
}
