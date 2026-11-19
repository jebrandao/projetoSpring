package br.com.app.dto;

import br.com.app.commons.ExampleValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = ExampleValues.EXEMPLO_PROFESSOR)
public class ProfessorDTO {

	private Integer matricula;

	@NotBlank
	private String nome;

	@NotBlank
	private String especializacao;

	@NotBlank
	private String telefone;
}
