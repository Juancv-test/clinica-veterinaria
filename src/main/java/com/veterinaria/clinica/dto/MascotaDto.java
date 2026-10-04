package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar o actualizar una mascota.
 * idCliente solo es necesario cuando el ADMIN crea la mascota;
 * en el registro propio el servicio lo toma del token JWT.
 */
public record MascotaDto(

        @NotBlank(message = "El nombre de la mascota es obligatorio")
        @Size(max = 100)
        String nombre,

        @NotBlank(message = "La especie es obligatoria (ej: Perro, Gato)")
        @Size(max = 50)
        String especie,

        @Size(max = 100)
        String raza,

        @Pattern(regexp = "[MH]", message = "El sexo debe ser M (macho) o H (hembra)")
        String sexo,

        @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
        LocalDate fechaNacimiento,

        @Positive(message = "El peso debe ser un valor positivo")
        BigDecimal pesoKg,

        // Requerido solo al crear mascota como admin; en registro propio se ignora
        Long idCliente
) {}
