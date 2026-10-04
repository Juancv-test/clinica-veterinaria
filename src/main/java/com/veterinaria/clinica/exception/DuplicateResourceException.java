package com.veterinaria.clinica.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso duplicado (409).
 * Ejemplos: DNI ya registrado, email ya en uso.
 * El GlobalExceptionHandler la captura y devuelve HTTP 409 con JSON.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }
}
