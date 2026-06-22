package com.ticketti.ms_mensajeria.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(
			@Nonnull HttpServletRequest request,
			@Nonnull HttpServletResponse response,
			@Nonnull FilterChain filterChain
	) throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			log.debug("Sin header Authorization en: {}", request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}

		final String jwt = authHeader.substring(7);

		try {
			if (jwtService.isTokenValid(jwt) && jwtService.validateTokenClaims(jwt)) {
				String correo = jwtService.extractCorreo(jwt);
				String role   = jwtService.extractRole(jwt);

				if (SecurityContextHolder.getContext().getAuthentication() == null) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
							correo,
							null,
							Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
					);
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
					log.debug("Autenticacion exitosa en ms-mensajeria para: {}, rol: {}", correo, role);
				}
			} else {
				log.warn("Token JWT invalido o claims incompletos en ms-mensajeria para: {}", request.getRequestURI());
			}
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("Error procesando JWT en ms-mensajeria en {}: {}", request.getRequestURI(), e.getMessage());
		}

		filterChain.doFilter(request, response);
	}
}
