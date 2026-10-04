package com.veterinaria.clinica.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Filtro JWT: se ejecuta UNA VEZ por petición HTTP (OncePerRequestFilter).
 *
 * Flujo:
 *   1. Lee el header "Authorization: Bearer <token>"
 *   2. Valida el token con JwtUtil
 *   3. Carga el usuario desde la BD
 *   4. Registra la autenticación en el SecurityContext
 *
 * Si el token falta o es inválido, la petición continúa sin autenticación
 * y Spring Security se encarga de devolver el 401 (vía JwtAuthEntryPoint).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Sin header Bearer → continuar la cadena de filtros sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token quitando "Bearer " (7 caracteres)
        String token = authHeader.substring(7);

        try {
            String email = jwtUtil.extractEmail(token);

            // Procesar solo si hay email y el SecurityContext aún no tiene autenticación
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                if (jwtUtil.isTokenValid(token)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // Crear el objeto de autenticación con los roles del usuario
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    // Adjuntar detalles de la petición (IP, session id, etc.)
                    autenticacion.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // Registrar en el contexto para que Spring Security lo use en esta petición
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            // Token malformado, firma inválida o expirado → responder 401 en JSON
            escribirErrorJson(response, 401, "Token inválido o expirado");
            return;
        }

        // Continuar con el siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }

    /** Escribe un error JSON directamente en la respuesta HTTP */
    private void escribirErrorJson(HttpServletResponse response, int status, String mensaje)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        response.getWriter().write(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"mensajes\":[\"%s\"]}"
                        .formatted(LocalDateTime.now(), status, mensaje, mensaje));
    }
}
