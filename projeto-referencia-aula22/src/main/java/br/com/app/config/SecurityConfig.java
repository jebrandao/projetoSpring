package br.com.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
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

	// Usado no login: confere usuário e senha com o UserDetailsService e o BCrypt
	@Bean
	public AuthenticationManager authenticationManager(
			UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider =
				new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	// Lê as autoridades da claim "autoridades" do token (sem prefixo adicional)
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter autoridades =
				new JwtGrantedAuthoritiesConverter();
		autoridades.setAuthoritiesClaimName("autoridades");
		autoridades.setAuthorityPrefix("");

		JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(autoridades);
		return conversor;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
			JwtAuthenticationConverter conversor) throws Exception {
		http
			// API stateless com token: sem sessão e sem cookie, logo sem CSRF
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults()) // usa o CorsConfigurationSource
			.sessionManagement(session -> session
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			// o console do H2 é exibido dentro de frames
			.headers(headers -> headers
					.frameOptions(frame -> frame.sameOrigin()))
			.authorizeHttpRequests(auth -> auth
				// públicas: login, cadastro, documentação e console H2 (só no dev)
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers(DOCUMENTACAO).permitAll()
				.requestMatchers("/h2-console/**").permitAll()
				// gestão de usuários: somente com a permissão USUARIOS
				.requestMatchers("/usuarios/**").hasAuthority("USUARIOS")
				// recursos da escola: decididos por permissão
				.requestMatchers(HttpMethod.GET, RECURSOS).hasAuthority("LEITURA")
				.requestMatchers(HttpMethod.POST, RECURSOS).hasAuthority("ESCRITA")
				.requestMatchers(HttpMethod.PUT, RECURSOS).hasAuthority("ESCRITA")
				.requestMatchers(HttpMethod.DELETE, RECURSOS).hasAuthority("ESCRITA")
				// qualquer outra rota exige login
				.anyRequest().authenticated())
			// a API vira um "resource server": aceita o JWT no cabeçalho Authorization
			.oauth2ResourceServer(oauth -> oauth
					.jwt(jwt -> jwt.jwtAuthenticationConverter(conversor)));

		return http.build();
	}
}
