package com.dinojan.taskmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Type-safe binding of the {@code app.cors.*} properties, e.g.
 * {@code app.cors.allowed-origins=http://localhost:5173,https://tasks.example.com}.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

	public CorsProperties {
		allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
	}
}
