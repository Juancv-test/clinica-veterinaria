package com.veterinaria.clinica.dto;

/**
 * DTO de respuesta para el inicio de sesión (POST /api/auth/login).
 * Contiene el token JWT y la información básica del usuario.
 * NOTA: nunca incluye el password_hash.
 */
public record LoginResponseDto(
        String token,
        String tipo,        // siempre "Bearer"
        String rol,         // nombre del rol: ADMIN, CLIENTE, etc.
        String nombres,
        Long   expiraEn     // timestamp Unix (ms) de expiración del token
) {}
