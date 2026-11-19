package br.com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.app.entity.Turma;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Integer> {
}
