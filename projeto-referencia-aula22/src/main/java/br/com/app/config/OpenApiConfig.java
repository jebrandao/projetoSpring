package br.com.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		Info info = new Info()
				.title("API Escola")
				.version("1.0")
				.description("API de gestão escolar - curso Spring Boot");

		SecurityScheme bearer = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.bearerFormat("JWT");

		return new OpenAPI()
				.info(info)
				// habilita o botão "Authorize" no Swagger UI
				.components(new Components().addSecuritySchemes(BEARER_AUTH, bearer))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
