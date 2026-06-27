package com.ticketti.ms_mensajeria.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
						// Swagger & OpenApi publicos
						.requestMatchers(
								"/v3/api-docs/**",
								"/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html"
						).permitAll()
						// Actuators publicos para monitoreo
						.requestMatchers(
								"/actuator/health",
								"/actuator/info"
						).permitAll()
						// Lectura de notificaciones propias (consumido internamente)
						.requestMatchers(HttpMethod.GET, "/api/v1/notificaciones/**").permitAll()
						// Formulario de contacto público (cualquier visitante puede escribir)
						.requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/contacto").permitAll()
						// Escritura requiere JWT
						.requestMatchers(HttpMethod.POST, "/api/v1/notificaciones/**").authenticated()
						.requestMatchers(HttpMethod.PUT, "/api/v1/notificaciones/**").authenticated()
						.requestMatchers(HttpMethod.DELETE, "/api/v1/notificaciones/**").authenticated()
						.anyRequest().authenticated()
				)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(
				"http://localhost:5173",
				"http://localhost:3000",
				"http://localhost:8222",
				"http://127.0.0.1:5173"
		));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
		configuration.setAllowedHeaders(Arrays.asList(
				"Authorization",
				"Content-Type",
				"Accept",
				"X-Usuario-Id",
				"X-Rol-Usuario-Id",
				"X-Requested-With"
		));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}
}
