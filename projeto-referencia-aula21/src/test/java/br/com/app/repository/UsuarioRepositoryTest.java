package br.com.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import br.com.app.entity.Perfil;
import br.com.app.entity.Usuario;

// replace = NONE: usa o banco configurado (H2 em modo MySQL), em vez de um H2 genérico
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

	@Autowired
	private UsuarioRepository usuarioRepo;

	@Test
	void deveEncontrarUsuarioPeloUsername() {
		Usuario usuario = new Usuario();
		usuario.setUsername("maria");
		usuario.setPassword("hash-qualquer");
		usuario.setPerfil(Perfil.USER);
		usuarioRepo.save(usuario);

		assertThat(usuarioRepo.findByUsername("maria"))
				.isPresent()
				.get()
				.extracting(Usuario::getPerfil)
				.isEqualTo(Perfil.USER);
	}

	@Test
	void naoDeveEncontrarUsuarioInexistente() {
		assertThat(usuarioRepo.findByUsername("fantasma")).isEmpty();
	}
}
