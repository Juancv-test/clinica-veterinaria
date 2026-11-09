package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.CitaDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Cita;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador de Citas veterinarias y cola de atencion.
 */
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;
    private final UsuarioDao usuarioDao;
    private final MascotaDao mascotaDao;

    public CitaController(CitaService citaService, UsuarioDao usuarioDao, MascotaDao mascotaDao) {
        this.citaService = citaService;
        this.usuarioDao = usuarioDao;
        this.mascotaDao = mascotaDao;
    }

    @GetMapping
    public List<Cita> listarCitas(@RequestParam(required = false) LocalDate fecha,
                                  @RequestParam(required = false) String estado) {
        return citaService.buscarCitas(fecha, estado);
    }

    @GetMapping("/pendientes")
    public List<Cita> listarColaPendientes() {
        return citaService.verColaPendientes();
    }

    @GetMapping("/mis-citas")
    public List<Cita> listarMisCitas(Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        if ("VETERINARIO".equalsIgnoreCase(usuario.nombreRol())) {
            return citaService.listarPorVeterinario(usuario.idUsuario());
        } else if ("CLIENTE".equalsIgnoreCase(usuario.nombreRol())) {
            return citaService.listarPorCliente(usuario.idUsuario());
        }
        return citaService.buscarCitas(null, null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cita programarCita(@Valid @RequestBody CitaDto dto) {
        return citaService.programarCita(dto);
    }

    @PostMapping("/solicitar")
    @ResponseStatus(HttpStatus.CREATED)
    public Cita solicitarCitaCliente(@Valid @RequestBody CitaDto dto, Authentication auth) {
        Usuario cliente = obtenerUsuarioEnSesion(auth);
        Mascota mascota = mascotaDao.findById(dto.idMascota())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada"));

        if (!mascota.idCliente().equals(cliente.idUsuario())) {
            throw new IllegalArgumentException("No puedes programar citas para una mascota que no te pertenece");
        }

        return citaService.programarCita(dto);
    }

    @PostMapping("/emergencia")
    @ResponseStatus(HttpStatus.CREATED)
    public Cita programarEmergencia(@RequestBody CitaDto dto) {
        // Emergencia: auto-asignar fecha/hora actual si no vienen
        LocalDate fecha = dto.fecha() != null ? dto.fecha() : LocalDate.now();
        LocalTime hora = dto.hora() != null ? dto.hora() : LocalTime.now().withSecond(0).withNano(0);
        CitaDto emergenciaDto = new CitaDto(
                dto.idMascota(),
                dto.idServicio(),
                dto.idVeterinario(),
                fecha,
                hora,
                dto.motivo() != null && !dto.motivo().isBlank() ? dto.motivo() : "Atencion de Emergencia",
                true
        );
        return citaService.programarCita(emergenciaDto);
    }

    @PutMapping("/{id}")
    public Cita modificarCita(@PathVariable Long id, @Valid @RequestBody CitaDto dto) {
        return citaService.actualizarCita(id, dto);
    }

    @PatchMapping("/{id}/veterinario")
    public Cita asignarVeterinarioRapido(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        Long idVeterinario = null;
        if (body != null && body.get("idVeterinario") != null && !body.get("idVeterinario").toString().isBlank()) {
            idVeterinario = Long.valueOf(body.get("idVeterinario").toString());
        }
        return citaService.asignarVeterinario(id, idVeterinario);
    }

    @PatchMapping("/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelarCita(@PathVariable Long id, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        if ("CLIENTE".equalsIgnoreCase(usuario.nombreRol())) {
            List<Cita> citasCliente = citaService.listarPorCliente(usuario.idUsuario());
            boolean esPropia = citasCliente.stream().anyMatch(c -> c.idCita().equals(id));
            if (!esPropia) {
                throw new IllegalArgumentException("No puedes cancelar una cita que no corresponde a tus mascotas");
            }
        }
        citaService.cancelarCita(id);
    }

    private Usuario obtenerUsuarioEnSesion(Authentication auth) {
        return usuarioDao.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no autenticado"));
    }
}
