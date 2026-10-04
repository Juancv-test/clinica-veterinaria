package com.veterinaria.clinica.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa una mascota registrada en el sistema.
 * Cada mascota pertenece a un usuario con rol CLIENTE.
 */
public record Mascota(
        Long       idMascota,
        String     nombre,
        String     especie,
        String     raza,
        String     sexo,             // "M" = macho, "H" = hembra
        LocalDate  fechaNacimiento,
        BigDecimal pesoKg,
        Long       idCliente,
        String     nombreCliente,    // campo calculado del JOIN con usuarios
        Boolean    activo            // false = baja lógica
) {}
