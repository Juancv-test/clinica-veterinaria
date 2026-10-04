package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para crear o actualizar una cita.
 * La validación @FutureOrPresent garantiza que no se programen
 * citas en fechas pasadas.
 */
public record CitaDto(

        @NotNull(message = "El ID de la mascota es obligatorio")
        Long idMascota,

        @NotNull(message = "El ID del servicio es obligatorio")
        Integer idServicio,

        // Veterinario opcional: puede asignarse después
        Long idVeterinario,

        @NotNull(message = "La fecha de la cita es obligatoria")
        @FutureOrPresent(message = "La fecha de la cita no puede ser en el pasado")
        LocalDate fecha,

        @NotNull(message = "La hora de la cita es obligatoria")
        LocalTime hora,

        @Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
        String motivo
) {}
