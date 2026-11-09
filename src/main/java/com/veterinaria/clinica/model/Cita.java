package com.veterinaria.clinica.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa una cita programada o de urgencia.
 * Relaciona una mascota con un servicio y (opcionalmente) un veterinario.
 */
public record Cita(
        Long      idCita,
        Long      idMascota,
        String    nombreMascota,       // campo calculado del JOIN
        Integer   idServicio,
        String    nombreServicio,      // campo calculado del JOIN
        Long      idVeterinario,       // puede ser null si aun no se asigna
        String    nombreVeterinario,   // campo calculado del JOIN (puede ser null)
        LocalDate fecha,
        LocalTime hora,
        String    estado,              // PROGRAMADA | CANCELADA | ATENDIDA
        String    motivo,
        Boolean   esEmergencia,
        String    contactoEmergencia
) {
    public Cita(Long idCita, Long idMascota, String nombreMascota, Integer idServicio,
                String nombreServicio, Long idVeterinario, String nombreVeterinario,
                LocalDate fecha, LocalTime hora, String estado, String motivo, Boolean esEmergencia) {
        this(idCita, idMascota, nombreMascota, idServicio, nombreServicio, idVeterinario,
             nombreVeterinario, fecha, hora, estado, motivo, esEmergencia, null);
    }

    public Cita(Long idCita, Long idMascota, String nombreMascota, Integer idServicio,
                String nombreServicio, Long idVeterinario, String nombreVeterinario,
                LocalDate fecha, LocalTime hora, String estado, String motivo) {
        this(idCita, idMascota, nombreMascota, idServicio, nombreServicio, idVeterinario,
             nombreVeterinario, fecha, hora, estado, motivo, false, null);
    }
}
