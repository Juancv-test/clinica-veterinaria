package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la tabla citas.
 */
public interface CitaDao {

    Optional<Cita> findById(Long id);

    /**
     * Devuelve citas con filtros opcionales.
     * @param fecha  si no es null, filtra por esa fecha
     * @param estado si no es null, filtra por ese estado
     */
    List<Cita> findAll(LocalDate fecha, String estado);

    /** Devuelve todas las citas en estado PROGRAMADA ordenadas por fecha y hora ASC. */
    List<Cita> findProgramadasOrdenadas();

    /** Devuelve citas asignadas a un veterinario especifico */
    List<Cita> findByVeterinario(Long idVeterinario);

    /** Devuelve citas de las mascotas pertenecientes a un cliente especifico */
    List<Cita> findByCliente(Long idCliente);

    /** Inserta una nueva cita y devuelve el ID generado */
    Long save(Cita cita);

    /** Actualiza los datos de una cita (fecha, hora, servicio, veterinario, motivo) */
    void update(Long id, Cita cita);

    /** Cambia el estado de la cita a CANCELADA */
    void cancel(Long id);

    /** Cambia el estado de la cita a ATENDIDA */
    void marcarAtendida(Long id);

    /**
     * Verifica si ya existe una cita PROGRAMADA para la misma mascota,
     * misma fecha y misma hora (evita duplicados al programar).
     */
    boolean existsConflicto(Long idMascota, LocalDate fecha, LocalTime hora);

    /**
     * Verifica si el horario (fecha y hora) ya está ocupado por otra cita PROGRAMADA.
     * Considera si un veterinario específico ya está ocupado o si se superó la capacidad de la clínica.
     */
    boolean existsConflictoHorario(LocalDate fecha, LocalTime hora, Long excludeIdCita, Long idVeterinario);

    /**
     * Asigna o desasigna rápidamente un veterinario a una cita existente.
     */
    void updateVeterinario(Long idCita, Long idVeterinario);
}
