package com.veterinaria.clinica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para el inicio de sesión (POST /api/auth/login).
 * Los records de Java son inmutables: constructor, getters y equals/hashCode automáticos.
 */
public record LoginRequestDto(

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}
