package br.com.app.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.app.commons.ExampleValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
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

	@Size(max = 20)
	private String telefone;

	// Versão do registro: o cliente devolve o valor recebido para detectar edição concorrente
	private Long versao;

	// Campos de auditoria: somente leitura
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private LocalDateTime criadoEm;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private String criadoPor;

	// @Valid propaga a validação para o objeto aninhado
	@Valid
	private EnderecoDTO endereco;
}
