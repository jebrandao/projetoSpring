package br.com.app.dto;

import java.time.LocalDate;

import br.com.app.commons.ExampleValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = ExampleValues.EXEMPLO_ESTUDANTE)
public class EstudanteDTO {

	private Integer id;

	@NotBlank
	private String nome;

	private String sobreNome;

	@NotBlank
	@Email
	private String email;

	@Past
	private LocalDate dataNascimento;
}
