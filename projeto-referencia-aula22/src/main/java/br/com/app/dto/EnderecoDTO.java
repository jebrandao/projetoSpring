package br.com.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderecoDTO {

	private Integer id;

	@NotBlank
	private String logradouro;

	private String numero;

	@NotBlank
	private String cep;

	@NotBlank
	private String cidade;
}
