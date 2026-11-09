package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Atencion;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la tabla atenciones (historial clinico).
 */
public interface AtencionDao {

    Optional<Atencion> findById(Long id);

    /** Devuelve el historial de atenciones de una mascota ordenadas por fecha descendente */
    List<Atencion> findByMascotaId(Long idMascota);

    /** Devuelve las atenciones realizadas por un veterinario especifico */
    List<Atencion> findByVeterinarioId(Long idVeterinario);

    /** Devuelve todas las atenciones registradas */
    List<Atencion> findAll();

    /** Registra una nueva atencion y retorna el ID generado */
    Long save(Atencion atencion);
}
