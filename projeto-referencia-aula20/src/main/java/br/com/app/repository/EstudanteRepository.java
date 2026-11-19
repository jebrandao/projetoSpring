package br.com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.app.entity.Estudante;

@Repository
public interface EstudanteRepository extends JpaRepository<Estudante, Integer> {
}
