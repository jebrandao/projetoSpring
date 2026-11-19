package br.com.app.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import br.com.app.entity.Estudante;
import jakarta.persistence.criteria.Join;

// Cada método devolve uma condição (Specification) que pode ser combinada com as outras.
// Retornar null significa "sem filtro".
public final class EstudanteSpecs {

	private EstudanteSpecs() {
	}

	public static Specification<Estudante> nomeContem(String nome) {
		return (root, query, cb) -> (nome == null || nome.isBlank())
				? null
				: cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
	}

	public static Specification<Estudante> nascidoDepoisDe(LocalDate data) {
		return (root, query, cb) -> data == null
				? null
				: cb.greaterThanOrEqualTo(root.get("dataNascimento"), data);
	}

	public static Specification<Estudante> daTurma(Integer turmaId) {
		return (root, query, cb) -> {
			if (turmaId == null) {
				return null;
			}
			Join<Object, Object> turmas = root.join("turmas");
			return cb.equal(turmas.get("id"), turmaId);
		};
	}
}
