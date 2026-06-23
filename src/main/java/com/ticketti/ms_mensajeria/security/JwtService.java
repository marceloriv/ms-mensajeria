package com.ticketti.ms_mensajeria.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JwtService {

	@Value("${jwt.secret}")
	private String secretKey;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
	}

	public String getCorreoFromContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new SecurityException("Usuario no autenticado");
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof String correo) {
			return correo;
		}
		throw new SecurityException("Formato de usuario invalido");
	}

	public String getRoleFromContext() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new SecurityException("Usuario no autenticado");
		}
		return authentication.getAuthorities().stream()
				.findFirst()
				.map(ga -> ga.getAuthority().replace("ROLE_", ""))
				.orElseThrow(() -> new SecurityException("Rol no encontrado"));
	}

	public String extractCorreo(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractRole(String token) {
		return extractClaim(token, claims -> claims.get("rol", String.class));
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public boolean isTokenValid(String token) {
		try {
			return extractCorreo(token) != null && !isTokenExpired(token);
		} catch (Exception e) {
			log.error("Error validando token: {}", e.getMessage());
			return false;
		}
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public boolean validateTokenClaims(String token) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.getSubject() != null
					&& claims.get("rol") != null
					&& claims.getExpiration() != null;
		} catch (Exception e) {
			log.error("Token invalido: {}", e.getMessage());
			return false;
		}
	}
}
