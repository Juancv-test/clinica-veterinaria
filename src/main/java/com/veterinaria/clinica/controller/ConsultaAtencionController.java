package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.ConsultaAtencionDto;
import com.veterinaria.clinica.dto.RespuestaConsultaDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.ConsultaAtencion;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.ConsultaAtencionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar la atencion al cliente y resolver dudas sobre servicios, horarios y citas.
 */
@RestController
@RequestMapping("/api/consultas")
public class ConsultaAtencionController {

    private final ConsultaAtencionService consultaService;
    private final UsuarioDao usuarioDao;

    public ConsultaAtencionController(ConsultaAtencionService consultaService, UsuarioDao usuarioDao) {
        this.consultaService = consultaService;
        this.usuarioDao = usuarioDao;
    }

    @PostMapping("/publica")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaAtencion registrarConsultaPublica(@Valid @RequestBody com.veterinaria.clinica.dto.MensajeContactoDto dto) {
        return consultaService.registrarConsultaPublica(dto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaAtencion registrarConsulta(@Valid @RequestBody ConsultaAtencionDto dto, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        return consultaService.registrarConsulta(usuario.idUsuario(), dto);
    }

    @GetMapping("/mis-consultas")
    public List<ConsultaAtencion> listarMisConsultas(Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        return consultaService.listarPorCliente(usuario.idUsuario());
    }

    @GetMapping
    public List<ConsultaAtencion> listarTodas(@RequestParam(required = false) String estado) {
        return consultaService.listarTodas(estado);
    }

    @PutMapping("/{id}/responder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void responderConsulta(@PathVariable Long id,
                                  @Valid @RequestBody RespuestaConsultaDto dto,
                                  Authentication auth) {
        Usuario recepcionista = obtenerUsuarioEnSesion(auth);
        consultaService.responderConsulta(id, recepcionista.idUsuario(), dto.respuesta());
    }

    private Usuario obtenerUsuarioEnSesion(Authentication auth) {
        return usuarioDao.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no autenticado"));
    }
}
