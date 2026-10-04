package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dto.CitaDto;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Citas.
 * Protegido (requiere ADMIN) vía SecurityConfig.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<Cita> listarCitas(@RequestParam(required = false) LocalDate fecha,
                                  @RequestParam(required = false) String estado) {
        return citaService.buscarCitas(fecha, estado);
    }

    /** 
     * Endpoint para ver el estado de la COLA de citas pendientes.
     * Demuestra el uso de la estructura Queue pedida en el curso. 
     */
    @GetMapping("/pendientes")
    public List<Cita> listarColaPendientes() {
        return citaService.verColaPendientes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cita programarCita(@Valid @RequestBody CitaDto dto) {
        return citaService.programarCita(dto);
    }

    @PatchMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
    }
}
