package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la tabla usuarios.
 * Todas las consultas usarán SQL parametrizado (sin concatenar strings).
 */
public interface UsuarioDao {

    /** Busca un usuario por email (para autenticación) */
    Optional<Usuario> findByEmail(String email);

    /** Busca un usuario por su ID */
    Optional<Usuario> findById(Long id);

    /** Devuelve todos los usuarios activos */
    List<Usuario> findAll();

    /** Devuelve todos los usuarios activos con un rol específico (p.ej. "CLIENTE") */
    List<Usuario> findAllByRol(String rol);

    /**
     * Inserta un nuevo usuario y devuelve el ID generado.
     * El passwordHash ya viene hasheado con BCrypt desde el servicio.
     */
    Long save(Usuario usuario);

    /** Actualiza datos de perfil (sin contraseña ni rol) */
    void update(Usuario usuario);

    /** Cambia el rol de un usuario */
    void updateRol(Long id, Integer idRol);

    /** Baja lógica: marca activo = false */
    void deactivate(Long id);

    /** Verifica si ya existe un usuario con ese email */
    boolean existsByEmail(String email);

    /** Verifica si ya existe un usuario con ese DNI */
    boolean existsByDni(String dni);
}
