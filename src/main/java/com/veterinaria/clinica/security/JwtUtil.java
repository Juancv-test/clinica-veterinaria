package com.veterinaria.clinica.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utilidad para crear y validar tokens JWT.
 *
 * JWT tiene tres partes: HEADER.PAYLOAD.SIGNATURE
 *   - Header: algoritmo de firma (HS256)
 *   - Payload: claims (sub=email, rol, idUsuario, exp)
 *   - Signature: HMAC-SHA256 del header+payload con el secreto
 */
@Component
public class JwtUtil {

    /** Secreto leído de la variable de entorno JWT_SECRET (mínimo 32 caracteres) */
    @Value("${jwt.secret}")
    private String secret;

    /** Tiempo de expiración en milisegundos (2 horas = 7 200 000 ms) */
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // ---------------------------------------------------------------
    // GENERACIÓN
    // ---------------------------------------------------------------

    /**
     * Crea un JWT firmado con HS256.
     * Claims incluidos: subject (email), rol, idUsuario, iat (issued-at), exp.
     */
    public String generateToken(String email, String rol, Long idUsuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)                   // identidad del usuario
                .claim("rol", rol)                // rol para autorización
                .claim("idUsuario", idUsuario)    // cómodo para el frontend
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(getSigningKey())         // firma HS256
                .compact();
    }

    // ---------------------------------------------------------------
    // EXTRACCIÓN DE CLAIMS
    // ---------------------------------------------------------------

    /** Extrae el email (subject) del token */
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** Devuelve el timestamp Unix (ms) de expiración */
    public long getExpiration(String token) {
        return parseClaims(token).getExpiration().getTime();
    }

    // ---------------------------------------------------------------
    // VALIDACIÓN
    // ---------------------------------------------------------------

    /**
     * Devuelve true si el token tiene firma válida y no ha expirado.
     * JJWT 0.12.x lanza JwtException automáticamente si algo falla.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);   // lanza excepción si inválido
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ---------------------------------------------------------------
    // MÉTODOS INTERNOS
    // ---------------------------------------------------------------

    /**
     * Parsea el token y devuelve sus claims.
     * Verifica firma y expiración; lanza JwtException si algo falla.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Genera la clave de firma derivando los bytes UTF-8 del secreto.
     * Keys.hmacShaKeyFor() requiere mínimo 32 bytes para HS256.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
