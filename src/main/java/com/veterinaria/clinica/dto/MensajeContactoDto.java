package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para enviar un mensaje publico desde la seccion de contacto de la pagina web.
 */
public record MensajeContactoDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no debe superar 100 caracteres")
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato de correo no es valido")
        @Size(max = 150, message = "El correo no debe superar 150 caracteres")
        String correo,

        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 500, message = "El mensaje no debe superar 500 caracteres")
        String mensaje
) {}
