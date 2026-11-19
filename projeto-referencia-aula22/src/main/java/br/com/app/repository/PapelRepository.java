package br.com.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.app.entity.Papel;

@Repository
public interface PapelRepository extends JpaRepository<Papel, Integer> {

	Optional<Papel> findByNome(String nome);
}
