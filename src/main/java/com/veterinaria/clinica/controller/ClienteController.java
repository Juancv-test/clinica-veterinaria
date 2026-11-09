package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dto.ActualizarPerfilDto;
import com.veterinaria.clinica.dto.UsuarioResponseDto;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador exclusivo para acciones del Cliente autenticado.
 */
@RestController
@RequestMapping("/api/cliente")
public class ClienteController {

    private final UsuarioService usuarioService;

    public ClienteController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public UsuarioResponseDto obtenerMiPerfil(Authentication auth) {
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName());
        if (!"CLIENTE".equalsIgnoreCase(usuario.nombreRol())) {
            throw new AccessDeniedException("Este perfil es exclusivo para clientes");
        }
        return UsuarioResponseDto.from(usuario);
    }

    @PutMapping("/perfil")
    public UsuarioResponseDto actualizarMiPerfil(@Valid @RequestBody ActualizarPerfilDto dto, Authentication auth) {
        Usuario usuario = usuarioService.buscarPorEmail(auth.getName());
        if (!"CLIENTE".equalsIgnoreCase(usuario.nombreRol())) {
            throw new AccessDeniedException("Este perfil es exclusivo para clientes");
        }
        Usuario actualizado = usuarioService.actualizarPerfil(usuario.idUsuario(), dto);
        return UsuarioResponseDto.from(actualizado);
    }
}
