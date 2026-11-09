package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para que un usuario autenticado actualice sus datos de perfil.
 */
public record ActualizarPerfilDto(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben superar 100 caracteres")
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos numéricos")
        String dni,

        @Pattern(regexp = "^$|\\d{9}", message = "El teléfono debe tener 9 dígitos")
        String telefono,

        @Size(max = 200, message = "La dirección no debe superar 200 caracteres")
        String direccion,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo electrónico inválido")
        String email,

        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        String nuevaPassword
) {}
