package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la tabla citas.
 * Las consultas con filtros opcionales se implementan de forma segura
 * construyendo la cláusula WHERE dinámicamente (los valores van como
 * parámetros, nunca concatenados en el SQL).
 */
public interface CitaDao {

    Optional<Cita> findById(Long id);

    /**
     * Devuelve citas con filtros opcionales.
     * @param fecha  si no es null, filtra por esa fecha
     * @param estado si no es null, filtra por ese estado
     */
    List<Cita> findAll(LocalDate fecha, String estado);

    /** Devuelve todas las citas en estado PROGRAMADA ordenadas por fecha y hora ASC.
     *  Se usa para reconstruir la cola (Queue) al iniciar la aplicación. */
    List<Cita> findProgramadasOrdenadas();

    /** Inserta una nueva cita y devuelve el ID generado */
    Long save(Cita cita);

    /** Actualiza los datos de una cita (fecha, hora, servicio, veterinario, motivo) */
    void update(Long id, Cita cita);

    /** Cambia el estado de la cita a CANCELADA */
    void cancel(Long id);

    /**
     * Verifica si ya existe una cita PROGRAMADA para la misma mascota,
     * misma fecha y misma hora (evita duplicados al programar).
     */
    boolean existsConflicto(Long idMascota, LocalDate fecha, LocalTime hora);
}
