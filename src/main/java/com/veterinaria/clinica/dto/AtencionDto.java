package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO para registrar una atencion medica veterinaria.
 */
public record AtencionDto(
        Long idCita,

        @NotNull(message = "El ID de la mascota es obligatorio")
        Long idMascota,

        @NotBlank(message = "El diagnostico es obligatorio")
        @Size(max = 500, message = "El diagnostico no debe superar 500 caracteres")
        String diagnostico,

        @NotBlank(message = "El tratamiento es obligatorio")
        @Size(max = 500, message = "El tratamiento no debe superar 500 caracteres")
        String tratamiento,

        @Size(max = 500, message = "Las observaciones no deben superar 500 caracteres")
        String observaciones,

        @Positive(message = "El peso debe ser un valor positivo")
        BigDecimal pesoActualKg
) {}
