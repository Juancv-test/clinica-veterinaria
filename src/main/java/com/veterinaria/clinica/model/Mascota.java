package com.veterinaria.clinica.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa una mascota registrada en el sistema.
 * Puede pertenecer a un usuario con rol CLIENTE, o ser temporal en caso de emergencia medica.
 */
public record Mascota(
        Long       idMascota,
        String     nombre,
        String     especie,
        String     raza,
        String     sexo,             // "M" = macho, "H" = hembra
        LocalDate  fechaNacimiento,
        BigDecimal pesoKg,
        Long       idCliente,        // puede ser null si es un caso de emergencia express no regularizado
        String     nombreCliente,    // campo calculado del JOIN con usuarios
        Boolean    activo,           // false = baja logica
        Boolean    esTemporal        // true si ingreso por urgencia sin registro de dueno previo
) {
    public Mascota(Long idMascota, String nombre, String especie, String raza, String sexo,
                   LocalDate fechaNacimiento, BigDecimal pesoKg, Long idCliente,
                   String nombreCliente, Boolean activo) {
        this(idMascota, nombre, especie, raza, sexo, fechaNacimiento, pesoKg, idCliente, nombreCliente, activo, false);
    }
}
