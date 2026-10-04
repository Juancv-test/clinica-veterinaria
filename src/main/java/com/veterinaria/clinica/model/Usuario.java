package com.veterinaria.clinica.model;

import java.time.LocalDateTime;

/**
 * Representa un usuario del sistema (cualquier actor).
 *
 * IMPORTANTE: passwordHash NUNCA debe devolverse en respuestas JSON.
 * El controlador/servicio usa un DTO de respuesta sin este campo.
 */
public record Usuario(
        Long          idUsuario,
        String        nombres,
        String        apellidos,
        String        dni,
        String        telefono,
        String        direccion,
        String        email,
        String        passwordHash,     // hash BCrypt — nunca exponer
        Integer       idRol,
        String        nombreRol,        // campo calculado del JOIN con roles
        Boolean       activo,           // false = baja lógica
        LocalDateTime fechaRegistro     // generado por la BD
) {}
