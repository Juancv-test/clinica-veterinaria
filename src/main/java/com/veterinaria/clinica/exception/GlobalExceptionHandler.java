package com.veterinaria.clinica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para toda la API REST.
 *
 * @RestControllerAdvice: intercepta excepciones lanzadas en cualquier @RestController
 * y devuelve respuestas JSON con el formato estándar:
 *   { timestamp, status, error, mensajes[] }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 — Errores de Bean Validation (@NotBlank, @Email, @Size, @Pattern, etc.) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        // Recopila todos los errores de campo en una lista de mensajes legibles
        List<String> mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());

        return respuesta(HttpStatus.BAD_REQUEST, "Error de validación", mensajes);
    }

    /** 404 — Recurso no encontrado (lanzado por los servicios) */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(ex.getMessage()));
    }

    /** 409 — Conflicto: DNI o email ya registrado */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), List.of(ex.getMessage()));
    }

    /** 500 — Error interno no controlado (última línea de defensa) */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor", List.of(ex.getMessage()));
    }

    // Método auxiliar: construye el mapa de respuesta de error estándar
    private ResponseEntity<Map<String, Object>> respuesta(
            HttpStatus status, String error, List<String> mensajes) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("mensajes", mensajes);

        return ResponseEntity.status(status).body(body);
    }
}
