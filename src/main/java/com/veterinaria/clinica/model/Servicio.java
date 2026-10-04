package com.veterinaria.clinica.model;

import java.math.BigDecimal;

/**
 * Representa un servicio veterinario ofrecido por la clínica.
 * Los servicios activos son el catálogo que se muestra a los clientes.
 */
public record Servicio(
        Integer    idServicio,
        String     nombre,
        String     descripcion,
        BigDecimal precio,
        Integer    duracionMin,  // duración estimada en minutos
        Boolean    activo
) {}
