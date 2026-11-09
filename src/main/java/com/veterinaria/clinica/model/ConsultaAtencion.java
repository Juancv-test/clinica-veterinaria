package com.veterinaria.clinica.model;

import java.time.LocalDateTime;

/**
 * Representa una consulta o requerimiento de atencion al cliente.
 */
public record ConsultaAtencion(
        Long          idConsulta,
        Long          idCliente,
        String        nombreCliente,
        String        emailCliente,
        String        asunto,
        String        mensaje,
        String        respuesta,
        Long          idRecepcionista,
        String        nombreRecepcionista,
        String        estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaRespuesta
) {}
