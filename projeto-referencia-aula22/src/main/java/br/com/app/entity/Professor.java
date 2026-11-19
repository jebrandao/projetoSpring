package br.com.app.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Professor extends Auditavel {

	// A matrícula é a chave primária: sequencial e numérica
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer matricula;

	private String nome;
	private String especializacao;
	private String telefone;

	// 1 professor leciona N turmas.
	// "professor" é o atributo de Turma que guarda a chave estrangeira.
	@OneToMany(mappedBy = "professor")
	private List<Turma> turmas = new ArrayList<>();
}
