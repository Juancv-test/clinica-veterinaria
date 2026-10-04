package com.veterinaria.clinica.service;

import com.veterinaria.clinica.dto.LoginRequestDto;
import com.veterinaria.clinica.dto.LoginResponseDto;
import com.veterinaria.clinica.model.Usuario;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la orquestación del inicio de sesión.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioDao usuarioDao;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UsuarioDao usuarioDao) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioDao = usuarioDao;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        // 1. Delegar a Spring Security la verificación de credenciales y hash BCrypt
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Si es exitoso, obtener los datos del usuario desde la BD (para sacar su rol real y nombre)
        Usuario usuario = usuarioDao.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras auth exitosa"));

        // 3. Generar el token JWT
        String token = jwtUtil.generateToken(usuario.email(), usuario.nombreRol(), usuario.idUsuario());
        long expiraEn = jwtUtil.getExpiration(token);

        // 4. Retornar DTO formateado para el frontend
        return new LoginResponseDto(
                token,
                "Bearer",
                usuario.nombreRol(),
                usuario.nombres(),
                expiraEn
        );
    }
}
