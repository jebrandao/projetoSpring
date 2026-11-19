package br.com.app.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Estudante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String nome;
	private String sobreNome;
	private String email;
	private LocalDate dataNascimento;

	// Coluna criada pela migração V2
	private String telefone;

	// 1 estudante possui 1 endereço.
	// Esta entidade é a "dona" da relação (guarda a chave estrangeira).
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "endereco_id")
	private Endereco endereco;

	// Lado inverso da relação N:N (a tabela de ligação é definida em Turma).
	@ManyToMany(mappedBy = "estudantes")
	private Set<Turma> turmas = new HashSet<>();
}
