package br.com.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Turma {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// Formato AAAANNNN: ano + número da turma. Exemplo: 20240007
	@Column(unique = true, nullable = false, length = 8)
	private String numero;

	private String titulo;

	@Enumerated(EnumType.STRING)
	private Turno turno;

	// N turmas pertencem a 1 professor.
	// Esta entidade guarda a chave estrangeira professor_matricula.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "professor_matricula")
	private Professor professor;

	// N turmas possuem N estudantes.
	// Turma é a "dona" da relação e define a tabela de ligação.
	@ManyToMany
	@JoinTable(name = "turma_estudante",
		joinColumns = @JoinColumn(name = "turma_id"),
		inverseJoinColumns = @JoinColumn(name = "estudante_id"))
	private Set<Estudante> estudantes = new HashSet<>();
}
