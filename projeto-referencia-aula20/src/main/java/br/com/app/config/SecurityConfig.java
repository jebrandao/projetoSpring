package br.com.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // habilita @PreAuthorize nos métodos
public class SecurityConfig {

	private static final String[] RECURSOS = {
			"/estudantes/**", "/professores/**", "/turmas/**" };

	private static final String[] DOCUMENTACAO = {
			"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {
		http
			// API stateless: CSRF (proteção de sessão/cookie) não se aplica
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// o console do H2 é exibido dentro de frames
			.headers(headers -> headers
					.frameOptions(frame -> frame.sameOrigin()))
			.authorizeHttpRequests(auth -> auth
				// documentação e console H2 (só existe no perfil dev) liberados
				.requestMatchers(DOCUMENTACAO).permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				// leitura: qualquer usuário autenticado
				.requestMatchers(HttpMethod.GET, RECURSOS)
					.hasAnyRole("USER", "ADMIN")
				// escrita: somente ADMIN
				.requestMatchers(HttpMethod.POST, RECURSOS).hasRole("ADMIN")
				.requestMatchers(HttpMethod.PUT, RECURSOS).hasRole("ADMIN")
				.requestMatchers(HttpMethod.DELETE, RECURSOS).hasRole("ADMIN")
				// qualquer outra rota exige login
				.anyRequest().authenticated())
			.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}
