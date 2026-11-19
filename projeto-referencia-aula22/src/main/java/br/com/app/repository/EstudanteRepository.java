package br.com.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.app.entity.Estudante;

// JpaSpecificationExecutor habilita as consultas dinâmicas (Specification)
@Repository
public interface EstudanteRepository
		extends JpaRepository<Estudante, Integer>, JpaSpecificationExecutor<Estudante> {

	List<Estudante> findByNomeContainingIgnoreCase(String trecho);

	// JPQL com parâmetro nomeado
	@Query("""
			select e from Estudante e
			where lower(e.email) like lower(concat('%@', :dominio))
			order by e.nome""")
	List<Estudante> buscarPorDominioDoEmail(@Param("dominio") String dominio);
}
