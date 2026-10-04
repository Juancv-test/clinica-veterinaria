package com.veterinaria.clinica.controller;

import com.veterinaria.clinica.dto.LoginRequestDto;
import com.veterinaria.clinica.dto.LoginResponseDto;
import com.veterinaria.clinica.dto.RegistroClienteDto;
import com.veterinaria.clinica.dto.UsuarioResponseDto;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.service.AuthService;
import com.veterinaria.clinica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador PÚBLICO para autenticación y registro.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        // Lanza excepción (capturada por GlobalExceptionHandler o Security) si falla
        return authService.login(request);
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED) // Devuelve 201 Created
    public UsuarioResponseDto registrarCliente(@Valid @RequestBody RegistroClienteDto request) {
        // Crea el usuario con rol CLIENTE y opcionalmente su mascota
        Usuario creado = usuarioService.registrarClienteConMascota(request);
        return UsuarioResponseDto.from(creado); // Devuelve sin exponer el password_hash
    }
}
