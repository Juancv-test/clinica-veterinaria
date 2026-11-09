package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO para regularizar y asociar los datos definitivos del dueno
 * a una mascota atendida previamente de emergencia.
 */
public record RegularizarEmergenciaDto(
        @NotNull(message = "El ID de la mascota es obligatorio")
        Long idMascota,

        String nombreDefinitivo,

        String raza,

        String sexo,

        LocalDate fechaNacimiento,

        // ID de cliente si ya existe en la base de datos
        Long idClienteExistente,

        // Datos para crear nuevo usuario cliente si no existia
        UsuarioDto nuevoCliente
) {}
