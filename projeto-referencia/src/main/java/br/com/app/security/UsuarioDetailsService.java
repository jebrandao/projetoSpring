package br.com.app.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.app.entity.Usuario;
import br.com.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

// O Spring Security chama este serviço para carregar o usuário no login
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepo;

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		Usuario usuario = usuarioRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Usuário não encontrado: " + username));

		return User.builder()
				.username(usuario.getUsername())
				.password(usuario.getPassword())
				// o Spring adiciona o prefixo ROLE_ automaticamente
				.roles(usuario.getPerfil().name())
				.build();
	}
}
