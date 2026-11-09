package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.Mascota;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Contrato de acceso a datos para la tabla mascotas */
public interface MascotaDao {

    Optional<Mascota> findById(Long id);

    /** Devuelve todas las mascotas activas */
    List<Mascota> findAll();

    /** Devuelve las mascotas activas de un cliente especifico */
    List<Mascota> findByClienteId(Long clienteId);

    /** Devuelve mascotas temporales ingresadas por emergencia pendientes de regularizar */
    List<Mascota> findTemporalesPendientes();

    /** Inserta una nueva mascota y devuelve el ID generado */
    Long save(Mascota mascota);

    /** Actualiza datos de la mascota */
    void update(Long id, Mascota mascota);

    /** Regulariza la mascota asociandola a un cliente formal y marcandola como no temporal */
    void regularizar(Long idMascota, Long idCliente, String nombre, String raza, String sexo, LocalDate fechaNacimiento);

    /** Baja logica: marca activo = false */
    void deactivate(Long id);

    /** Verifica si la mascota existe y esta activa */
    boolean existsById(Long id);
}
