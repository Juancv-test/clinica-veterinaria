package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para responder a una consulta de atencion al cliente.
 */
public record RespuestaConsultaDto(
        @NotBlank(message = "La respuesta es obligatoria")
        @Size(max = 500, message = "La respuesta no debe superar 500 caracteres")
        String respuesta
) {}
