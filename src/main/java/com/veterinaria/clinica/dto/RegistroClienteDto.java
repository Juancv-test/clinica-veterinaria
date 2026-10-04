package com.veterinaria.clinica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para registro de un cliente junto con su mascota.
 * Usado en:
 *   - POST /api/auth/registro (público, mascota requerida)
 *   - POST /api/clientes       (ADMIN, mascota opcional)
 *
 * @Valid en los campos anidados hace que Spring valide también los DTOs internos.
 */
public record RegistroClienteDto(

        @Valid
        @NotNull(message = "Los datos del cliente son obligatorios")
        UsuarioDto cliente,

        // mascota es opcional (puede ser null cuando el ADMIN crea solo el usuario)
        @Valid
        MascotaDto mascota
) {}
