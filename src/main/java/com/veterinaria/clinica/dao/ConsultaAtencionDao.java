package com.veterinaria.clinica.dao;

import com.veterinaria.clinica.model.ConsultaAtencion;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para la tabla consultas_atencion (atencion al cliente).
 */
public interface ConsultaAtencionDao {

    Optional<ConsultaAtencion> findById(Long id);

    /** Devuelve todas las consultas con filtro opcional por estado */
    List<ConsultaAtencion> findAll(String estado);

    /** Devuelve las consultas enviadas por un cliente especifico */
    List<ConsultaAtencion> findByClienteId(Long idCliente);

    /** Guarda una nueva consulta de atencion al cliente y devuelve el ID generado */
    Long save(ConsultaAtencion consulta);

    /** Registra la respuesta a una consulta y actualiza su estado a RESPONDIDA */
    void responder(Long idConsulta, String respuesta, Long idRecepcionista);
}
