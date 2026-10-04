package com.veterinaria.clinica.dto;

import com.veterinaria.clinica.model.Usuario;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para datos de usuario.
 *
 * NO incluye passwordHash.
 * Los controladores deben devolver SIEMPRE este DTO, nunca el record Usuario.
 *
 * El método estático from() centraliza la conversión para no repetir código.
 */
public record UsuarioResponseDto(
        Long          idUsuario,
        String        nombres,
        String        apellidos,
        String        dni,
        String        telefono,
        String        direccion,
        String        email,
        String        rol,            // nombre del rol (sin prefijo ROLE_)
        Boolean       activo,
        LocalDateTime fechaRegistro
) {
    /**
     * Convierte un Usuario (modelo interno con passwordHash) en un
     * UsuarioResponseDto seguro para devolver por la API.
     *
     * Uso en los controladores:
     *   return UsuarioResponseDto.from(usuario);
     */
    public static UsuarioResponseDto from(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.idUsuario(),
                usuario.nombres(),
                usuario.apellidos(),
                usuario.dni(),
                usuario.telefono(),
                usuario.direccion(),
                usuario.email(),
                usuario.nombreRol(),   // campo del JOIN en los DAO
                usuario.activo(),
                usuario.fechaRegistro()
        );
    }
}
