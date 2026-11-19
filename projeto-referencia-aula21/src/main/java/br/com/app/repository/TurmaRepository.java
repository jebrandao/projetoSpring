package br.com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.app.dto.TurmaResumoDTO;
import br.com.app.entity.Turma;
import br.com.app.entity.Turno;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Integer> {

	// Consultas derivadas do nome do método
	List<Turma> findByTurno(Turno turno);

	List<Turma> findByProfessorMatricula(Integer matricula);

	// EntityGraph: carrega o professor junto, em um único SELECT (evita o problema N+1)
	@EntityGraph(attributePaths = "professor")
	@Query("select t from Turma t")
	List<Turma> findAllComProfessor();

	// JPQL com projeção direta em um record (relatório com agrupamento)
	@Query("""
			select new br.com.app.dto.TurmaResumoDTO(
			    t.numero, t.titulo, p.nome, count(e))
			from Turma t
			left join t.professor p
			left join t.estudantes e
			group by t.id, t.numero, t.titulo, p.nome
			order by t.numero""")
	List<TurmaResumoDTO> resumoDasTurmas();
}
