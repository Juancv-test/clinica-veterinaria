package com.veterinaria.clinica.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa una atencion medica veterinaria registrada en el historial clinico.
 */
public record Atencion(
        Long          idAtencion,
        Long          idCita,
        Long          idMascota,
        String        nombreMascota,
        Long          idVeterinario,
        String        nombreVeterinario,
        LocalDateTime fecha,
        String        diagnostico,
        String        tratamiento,
        String        observaciones,
        BigDecimal    pesoActualKg
) {}
