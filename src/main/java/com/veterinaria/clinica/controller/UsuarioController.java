package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dto.CambioRolDto;
import com.veterinaria.clinica.dto.UsuarioDto;
import com.veterinaria.clinica.dto.UsuarioResponseDto;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador de Usuarios.
 * Todo bajo /api/usuarios está protegido y requiere ROLE_ADMIN (configurado en SecurityConfig).
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponseDto> listarUsuarios(@RequestParam(required = false) String rol) {
        List<Usuario> lista;
        if (rol != null && !rol.isBlank()) {
            lista = usuarioService.listarPorRol(rol.toUpperCase());
        } else {
            lista = usuarioService.listarTodos();
        }
        
        return lista.stream()
                .map(UsuarioResponseDto::from)
                .collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDto crearUsuario(@Valid @RequestBody UsuarioDto dto) {
        Usuario creado = usuarioService.crearUsuario(dto, "CLIENTE");
        return UsuarioResponseDto.from(creado);
    }

    @PatchMapping("/{id}/rol")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void cambiarRol(@PathVariable Long id, @Valid @RequestBody CambioRolDto dto) {
        usuarioService.cambiarRol(id, dto.idRol());
    }

    @PutMapping("/{id}")
    public UsuarioResponseDto actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDto dto) {
        Usuario actualizado = usuarioService.actualizarUsuario(id, dto);
        return UsuarioResponseDto.from(actualizado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.darDeBaja(id);
    }
}
