package com.veterinaria.clinica.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Manejador de error 403 (Acceso denegado).
 *
 * Se activa cuando el usuario ESTÁ autenticado (tiene token válido)
 * pero NO tiene el rol necesario para el recurso solicitado.
 * Ejemplo: un CLIENTE intenta acceder a /api/usuarios (solo ADMIN).
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403

        response.getWriter().write(
                "{\"timestamp\":\"%s\",\"status\":403," +
                "\"error\":\"Acceso denegado\"," +
                "\"mensajes\":[\"No tienes permisos para acceder a este recurso.\"]}"
                        .formatted(LocalDateTime.now()));
    }
}
