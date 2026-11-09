package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dao.MascotaDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.dto.AtencionDto;
import com.veterinaria.clinica.exception.ResourceNotFoundException;
import com.veterinaria.clinica.model.Atencion;
import com.veterinaria.clinica.model.Mascota;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.AtencionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la atencion veterinaria y consulta de historial clinico.
 */
@RestController
@RequestMapping("/api/atenciones")
public class AtencionController {

    private final AtencionService atencionService;
    private final UsuarioDao usuarioDao;
    private final MascotaDao mascotaDao;

    public AtencionController(AtencionService atencionService,
                              UsuarioDao usuarioDao,
                              MascotaDao mascotaDao) {
        this.atencionService = atencionService;
        this.usuarioDao = usuarioDao;
        this.mascotaDao = mascotaDao;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Atencion registrarAtencion(@Valid @RequestBody AtencionDto dto, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        return atencionService.registrarAtencion(dto, usuario.idUsuario());
    }

    @GetMapping("/mascota/{idMascota}")
    public List<Atencion> obtenerHistorialPorMascota(@PathVariable Long idMascota, Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        if ("CLIENTE".equalsIgnoreCase(usuario.nombreRol())) {
            Mascota mascota = mascotaDao.findById(idMascota)
                    .orElseThrow(() -> new ResourceNotFoundException("Mascota no encontrada"));
            if (!mascota.idCliente().equals(usuario.idUsuario())) {
                throw new IllegalArgumentException("No tienes permiso para ver el historial de esta mascota");
            }
        }
        return atencionService.obtenerHistorialPorMascota(idMascota);
    }

    @GetMapping("/mis-atenciones")
    public List<Atencion> listarMisAtenciones(Authentication auth) {
        Usuario usuario = obtenerUsuarioEnSesion(auth);
        return atencionService.listarPorVeterinario(usuario.idUsuario());
    }

    @GetMapping
    public List<Atencion> listarTodas() {
        return atencionService.listarTodas();
    }

    private Usuario obtenerUsuarioEnSesion(Authentication auth) {
        return usuarioDao.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no autenticado"));
    }
}
