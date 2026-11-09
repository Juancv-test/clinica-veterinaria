package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para enviar una consulta de atencion al cliente.
 */
public record ConsultaAtencionDto(
        @NotBlank(message = "El asunto es obligatorio")
        @Size(max = 100, message = "El asunto no debe superar 100 caracteres")
        String asunto,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 500, message = "El mensaje no debe superar 500 caracteres")
        String mensaje
) {}
