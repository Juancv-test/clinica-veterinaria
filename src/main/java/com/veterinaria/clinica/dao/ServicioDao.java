package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Servicio;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para la tabla servicios */
public interface ServicioDao {

    Optional<Servicio> findById(Integer id);

    /** Devuelve todos los servicios activos */
    List<Servicio> findAll();

    /** Devuelve TODOS los servicios incluyendo inactivos (para la caché) */
    List<Servicio> findAllIncludingInactive();

    boolean existsById(Integer id);
}
