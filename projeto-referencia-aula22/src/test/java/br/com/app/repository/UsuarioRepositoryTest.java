package br.com.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import br.com.app.entity.Papel;
import br.com.app.entity.Permissao;
import br.com.app.entity.Usuario;

// replace = NONE: usa o banco configurado (H2 em modo MySQL), em vez de um H2 genérico
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UsuarioRepositoryTest {

	@Autowired
	private UsuarioRepository usuarioRepo;

	@Autowired
	private PapelRepository papelRepo;

	private Set<String> nomesDasPermissoes(Papel papel) {
		return papel.getPermissoes().stream()
				.map(Permissao::getNome)
				.collect(java.util.stream.Collectors.toSet());
	}

	@Test
	void papeisIniciaisCriadosPelaMigracaoV5() {
		Papel admin = papelRepo.findByNome("ADMIN").orElseThrow();
		Papel user = papelRepo.findByNome("USER").orElseThrow();

		assertThat(nomesDasPermissoes(admin))
				.containsExactlyInAnyOrder("LEITURA", "ESCRITA", "USUARIOS");
		assertThat(nomesDasPermissoes(user)).containsExactly("LEITURA");
	}

	@Test
	void deveEncontrarUsuarioPeloUsernameComSeusPapeis() {
		Usuario usuario = new Usuario();
		usuario.setUsername("maria");
		usuario.setPassword("hash-qualquer");
		usuario.getPapeis().add(papelRepo.findByNome("USER").orElseThrow());
		usuarioRepo.save(usuario);

		assertThat(usuarioRepo.findByUsername("maria"))
				.isPresent()
				.get()
				.satisfies(u -> assertThat(u.getPapeis())
						.extracting(Papel::getNome).containsExactly("USER"));
	}

	@Test
	void naoDeveEncontrarUsuarioInexistente() {
		assertThat(usuarioRepo.findByUsername("fantasma")).isEmpty();
	}
}
