package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para el endpoint PATCH /api/usuarios/{id}/rol.
 * Solo lleva el nuevo id del rol a asignar.
 */
public record CambioRolDto(

        @NotNull(message = "El ID del rol es obligatorio")
        Integer idRol
) {}
