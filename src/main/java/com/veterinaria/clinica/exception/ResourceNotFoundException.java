package com.veterinaria.clinica.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso (404).
 * El GlobalExceptionHandler la captura y devuelve HTTP 404 con JSON.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
