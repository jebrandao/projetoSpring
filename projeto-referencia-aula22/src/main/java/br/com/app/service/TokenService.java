package br.com.app.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.com.app.dto.TokenDTO;

@Service
public class TokenService {

	private final JwtEncoder jwtEncoder;
	private final long expiracaoEmSegundos;

	public TokenService(JwtEncoder jwtEncoder,
			@Value("${app.jwt.expiracao-segundos}") long expiracaoEmSegundos) {
		this.jwtEncoder = jwtEncoder;
		this.expiracaoEmSegundos = expiracaoEmSegundos;
	}

	// O token é assinado (HS256) e carrega quem é o usuário e o que ele pode fazer
	public TokenDTO gerarToken(Authentication autenticacao) {
		Instant agora = Instant.now();
		// O Spring Security 7 acrescenta autoridades internas "FACTOR_..." (indicam como o
		// usuário se autenticou). Elas não pertencem ao token.
		List<String> autoridades = autenticacao.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(nome -> !nome.startsWith("FACTOR_"))
				.toList();

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("api-escola")
				.subject(autenticacao.getName())
				.issuedAt(agora)
				.expiresAt(agora.plusSeconds(expiracaoEmSegundos))
				.claim("autoridades", autoridades)
				.build();

		JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder
				.encode(JwtEncoderParameters.from(cabecalho, claims))
				.getTokenValue();
		return new TokenDTO(token, "Bearer", expiracaoEmSegundos);
	}
}
