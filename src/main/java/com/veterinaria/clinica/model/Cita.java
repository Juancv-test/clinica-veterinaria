package com.veterinaria.clinica.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa una cita programada.
 * Relaciona una mascota con un servicio y (opcionalmente) un veterinario.
 * Esta clase también se usa como elemento de la cola (Queue) de citas pendientes.
 */
public record Cita(
        Long      idCita,
        Long      idMascota,
        String    nombreMascota,       // campo calculado del JOIN
        Integer   idServicio,
        String    nombreServicio,      // campo calculado del JOIN
        Long      idVeterinario,       // puede ser null si aún no se asigna
        String    nombreVeterinario,   // campo calculado del JOIN (puede ser null)
        LocalDate fecha,
        LocalTime hora,
        String    estado,              // PROGRAMADA | CANCELADA | ATENDIDA
        String    motivo
) {}
