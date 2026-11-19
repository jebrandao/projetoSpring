package br.com.app.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

// Um papel agrupa permissões: ADMIN e USER
@Entity
@Getter
@Setter
public class Papel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(unique = true, nullable = false, length = 50)
	private String nome;

	// EAGER: as permissões são necessárias a cada login
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "papel_permissao",
		joinColumns = @JoinColumn(name = "papel_id"),
		inverseJoinColumns = @JoinColumn(name = "permissao_id"))
	private Set<Permissao> permissoes = new HashSet<>();
}
