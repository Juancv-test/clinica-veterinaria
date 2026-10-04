package com.veterinaria.clinica.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Manejador de error 401 (No autenticado).
 *
 * Se activa cuando alguien intenta acceder a un recurso protegido
 * sin haber enviado un token JWT válido.
 * Devuelve una respuesta JSON en lugar de la página de error por defecto.
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        response.getWriter().write(
                "{\"timestamp\":\"%s\",\"status\":401," +
                "\"error\":\"No autenticado\"," +
                "\"mensajes\":[\"Se requiere autenticaci\u00f3n. Env\u00eda un token JWT v\u00e1lido en el header Authorization: Bearer <token>\"]}"
                        .formatted(LocalDateTime.now()));
    }
}
