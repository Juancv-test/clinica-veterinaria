package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dto.IngresoEmergenciaExpressDto;
import com.veterinaria.clinica.dto.RegularizarEmergenciaDto;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.service.EmergenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la atencion de emergencias express y regularizacion de pacientes.
 */
@RestController
@RequestMapping("/api/emergencias")
public class EmergenciaController {

    private final EmergenciaService emergenciaService;

    public EmergenciaController(EmergenciaService emergenciaService) {
        this.emergenciaService = emergenciaService;
    }

    @PostMapping("/ingreso-express")
    @ResponseStatus(HttpStatus.CREATED)
    public Cita ingresoExpress(@Valid @RequestBody IngresoEmergenciaExpressDto dto) {
        return emergenciaService.registrarIngresoExpress(dto);
    }

    @PostMapping("/regularizar")
    public Mascota regularizar(@Valid @RequestBody RegularizarEmergenciaDto dto) {
        return emergenciaService.regularizarMascota(dto);
    }

    @GetMapping("/pendientes-regularizar")
    public List<Mascota> listarPendientesRegularizar() {
        return emergenciaService.listarTemporalesPendientes();
    }
}
