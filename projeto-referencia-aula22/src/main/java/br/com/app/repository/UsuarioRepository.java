package br.com.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.app.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	// Query method: o Spring Data gera o SQL a partir do nome do método
	Optional<Usuario> findByUsername(String username);
}
