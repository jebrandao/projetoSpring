package br.com.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Professor {

	// A matrícula é a chave primária: sequencial e numérica
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer matricula;

	private String nome;
	private String especializacao;
	private String telefone;
}
