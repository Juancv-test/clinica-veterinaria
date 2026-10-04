package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Mascota;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para la tabla mascotas */
public interface MascotaDao {

    Optional<Mascota> findById(Long id);

    /** Devuelve todas las mascotas activas */
    List<Mascota> findAll();

    /** Devuelve las mascotas activas de un cliente específico */
    List<Mascota> findByClienteId(Long clienteId);

    /** Inserta una nueva mascota y devuelve el ID generado */
    Long save(Mascota mascota);

    /** Actualiza datos de la mascota */
    void update(Long id, Mascota mascota);

    /** Baja lógica: marca activo = false */
    void deactivate(Long id);

    /** Verifica si la mascota existe y está activa */
    boolean existsById(Long id);
}
