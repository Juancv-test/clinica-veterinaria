package com.veterinaria.clinica.model;

/**
 * Representa un rol del sistema.
 * Los registros (records) en Java son clases inmutables y concisas.
 * Roles posibles: ADMIN, CLIENTE, RECEPCIONISTA, VETERINARIO.
 */
public record Rol(
        Integer idRol,
        String nombre
) {}
