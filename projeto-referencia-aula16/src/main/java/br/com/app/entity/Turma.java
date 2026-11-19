package br.com.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
}
