package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Rol;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para la tabla roles */
public interface RolDao {
    Optional<Rol> findByNombre(String nombre);
    Optional<Rol> findById(Integer id);
    List<Rol> findAll();
}
