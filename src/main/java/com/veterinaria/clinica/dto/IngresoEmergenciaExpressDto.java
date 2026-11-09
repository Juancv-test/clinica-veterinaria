package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para el triage e ingreso express de emergencia.
 * Requiere datos minimos para atender a la mascota en segundos.
 */
public record IngresoEmergenciaExpressDto(
        @NotBlank(message = "La especie es obligatoria (ej: Perro, Gato)")
        @Size(max = 50)
        String especie,

        @Size(max = 100)
        String nombreTemporal,

        @NotBlank(message = "El motivo urgente es obligatorio")
        @Size(max = 200)
        String motivoUrgencia,

        @Size(max = 150)
        String contactoEmergencia,

        String raza,

        String sexo
) {}
