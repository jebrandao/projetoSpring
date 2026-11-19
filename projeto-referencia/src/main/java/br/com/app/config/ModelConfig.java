package br.com.app.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

	// disponível para injeção em qualquer classe
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
