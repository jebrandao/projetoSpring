package br.com.app.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.app.entity.Perfil;
import br.com.app.entity.Usuario;
import br.com.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

	private final UsuarioRepository usuarioRepo;
	private final PasswordEncoder passwordEncoder;

	// Cria o usuário guardando apenas o HASH da senha
	public Usuario criarUsuario(String username, String senhaPura, Perfil perfil) {
		Usuario usuario = new Usuario();
		usuario.setUsername(username);
		usuario.setPassword(passwordEncoder.encode(senhaPura));
		usuario.setPerfil(perfil);
		return usuarioRepo.save(usuario);
	}

	public boolean existe(String username) {
		return usuarioRepo.findByUsername(username).isPresent();
	}
}
