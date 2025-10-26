package com.networkroom.skye_portfolio_backend.platform.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		
		CorsConfiguration contactConfig = new CorsConfiguration();

		contactConfig.setAllowedOrigins(List.of(
			"https://luvrksnsnskyedev.space"
		));
		
		contactConfig.setAllowedMethods(List.of("POST", "OPTIONS"));
		
		contactConfig.setAllowedHeaders(List.of(
			"Content-Type",
			"X-CLIENT-KEY"
		));
		
		contactConfig.setAllowCredentials(false);

		source.registerCorsConfiguration("/api/contact", contactConfig);

		return source;
	}
}
