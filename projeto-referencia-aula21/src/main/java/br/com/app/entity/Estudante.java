package br.com.app.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Entity
// Exclusão lógica: o delete vira um UPDATE (os parâmetros são o id e a versão)
// e o filtro @SQLRestriction esconde os registros excluídos de todas as consultas
@SQLDelete(sql = "UPDATE estudante SET excluido = TRUE WHERE id = ? AND versao = ?")
@SQLRestriction("excluido = FALSE")
@Getter
@Setter
public class Estudante extends Auditavel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	// Controle de versão: o Hibernate soma 1 a cada UPDATE (bloqueio otimista)
	@Version
	private Long versao;

	private String nome;
	private String sobreNome;
	private String email;
	private LocalDate dataNascimento;

	// Coluna criada pela migração V2
	private String telefone;

	// 1 estudante possui 1 endereço.
	// Esta entidade é a "dona" da relação (guarda a chave estrangeira).
	// Sem cascade de remoção: com a exclusão lógica o estudante continua na tabela
	// e ainda referencia o endereço.
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "endereco_id")
	private Endereco endereco;

	// Lado inverso da relação N:N (a tabela de ligação é definida em Turma).
	@ManyToMany(mappedBy = "estudantes")
	private Set<Turma> turmas = new HashSet<>();
}
