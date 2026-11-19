package br.com.app.dto;

import java.util.Set;
import java.util.stream.Collectors;

import br.com.app.entity.Papel;
import br.com.app.entity.Usuario;

// Resposta: nunca devolve a senha (nem o hash)
public record UsuarioDTO(
		Integer id,
		String username,
		Set<String> papeis) {

	public static UsuarioDTO de(Usuario usuario) {
		Set<String> nomes = usuario.getPapeis().stream()
				.map(Papel::getNome)
				.collect(Collectors.toSet());
		return new UsuarioDTO(usuario.getId(), usuario.getUsername(), nomes);
	}
}
