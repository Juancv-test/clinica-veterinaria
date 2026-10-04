package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para crear o actualizar un usuario (usado por el ADMIN).
 * Las anotaciones de Bean Validation son evaluadas por Spring antes de
 * que el controlador llame al servicio (cuando se usa @Valid en el parámetro).
 */
public record UsuarioDto(

        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 100, message = "Los nombres no deben superar 100 caracteres")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no deben superar 100 caracteres")
        String apellidos,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos numéricos")
        String dni,

        @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
        String telefono,

        @Size(max = 200, message = "La dirección no debe superar 200 caracteres")
        String direccion,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        // Solo el ADMIN puede asignar un rol; si es null, el servicio asigna CLIENTE por defecto
        Integer idRol
) {}
