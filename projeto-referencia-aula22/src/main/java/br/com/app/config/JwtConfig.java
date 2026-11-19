package br.com.app.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

	// A chave secreta assina e valida os tokens.
	// Ela vem da configuração (variável de ambiente em produção), nunca do código.
	@Bean
	public SecretKey chaveJwt(@Value("${app.jwt.secret}") String segredo) {
		byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException(
					"app.jwt.secret precisa ter pelo menos 32 caracteres");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	// Gera tokens (usado no login)
	@Bean
	public JwtEncoder jwtEncoder(SecretKey chaveJwt) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
	}

	// Valida os tokens recebidos (assinatura e validade)
	@Bean
	public JwtDecoder jwtDecoder(SecretKey chaveJwt) {
		return NimbusJwtDecoder.withSecretKey(chaveJwt)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
	}
}
