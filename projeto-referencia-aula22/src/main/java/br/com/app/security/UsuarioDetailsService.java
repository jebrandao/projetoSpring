package br.com.app.security;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.app.entity.Papel;
import br.com.app.entity.Permissao;
import br.com.app.entity.Usuario;
import br.com.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

// O Spring Security chama este serviço para carregar o usuário no login
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepo;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		Usuario usuario = usuarioRepo.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Usuário não encontrado: " + username));

		return User.builder()
				.username(usuario.getUsername())
				.password(usuario.getPassword())
				.authorities(autoridadesDe(usuario))
				.build();
	}

	// Cada papel vira a autoridade ROLE_xxx e cada permissão vira
	// uma autoridade com o próprio nome
	private Set<GrantedAuthority> autoridadesDe(Usuario usuario) {
		Set<GrantedAuthority> autoridades = new LinkedHashSet<>();
		for (Papel papel : usuario.getPapeis()) {
			autoridades.add(new SimpleGrantedAuthority("ROLE_" + papel.getNome()));
			for (Permissao permissao : papel.getPermissoes()) {
				autoridades.add(new SimpleGrantedAuthority(permissao.getNome()));
			}
		}
		return autoridades;
	}
}
