package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.model.Servicio;
import com.veterinaria.clinica.service.ServicioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Servicios.
 * Protegido (pero accesible a CUALQUIER usuario autenticado) vía SecurityConfig.
 */
@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<Servicio> listarActivos() {
        return servicioService.listarActivos();
    }

    @GetMapping("/{id}")
    public Servicio obtenerPorId(@PathVariable Integer id) {
        // Esto consulta la caché HashMap en O(1), no la base de datos
        return servicioService.obtenerPorId(id);
    }
}
