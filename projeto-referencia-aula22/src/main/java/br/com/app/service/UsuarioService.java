package br.com.app.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.app.entity.Papel;
import br.com.app.entity.Usuario;
import br.com.app.exception.RegraNegocioException;
import br.com.app.repository.PapelRepository;
import br.com.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarioRepo;
	private final PapelRepository papelRepo;
	private final PasswordEncoder passwordEncoder;

	// Cria o usuário guardando apenas o HASH da senha
	@Transactional
	public Usuario criarUsuario(String username, String senhaPura,
			Set<String> nomesDosPapeis) {
		if (usuarioRepo.findByUsername(username).isPresent()) {
			throw new RegraNegocioException(
					"Já existe um usuário com o login " + username);
		}

		Set<Papel> papeis = nomesDosPapeis.stream()
				.map(this::buscarPapel)
				.collect(Collectors.toSet());

		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(senhaPura));
		usuario.setPapeis(papeis);
		return usuarioRepo.save(usuario);
	}

	public List<Usuario> listarUsuarios() {
		return usuarioRepo.findAll();
	}

	public boolean existe(String username) {
		return usuarioRepo.findByUsername(username).isPresent();
	}

	private Papel buscarPapel(String nome) {
		return papelRepo.findByNome(nome).orElseThrow(
				() -> new RegraNegocioException("Papel inexistente: " + nome));
	}
}
