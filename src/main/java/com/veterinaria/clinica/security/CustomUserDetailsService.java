package com.veterinaria.clinica.security;

import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.model.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de UserDetailsService que carga el usuario desde PostgreSQL.
 *
 * Spring Security llama a loadUserByUsername() cuando necesita autenticar
 * a alguien (ya sea en login o en la validación del token JWT).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioDao usuarioDao;

    public CustomUserDetailsService(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Buscar el usuario en la BD por email
        Usuario usuario = usuarioDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email));

        // Verificar que el usuario no esté dado de baja
        if (!usuario.activo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        /*
         * User.builder() de Spring Security construye un UserDetails.
         * .roles("ADMIN") agrega automáticamente el prefijo "ROLE_"
         * que Spring Security usa internamente (→ "ROLE_ADMIN").
         * Esto se verifica en SecurityConfig con .hasRole("ADMIN").
         */
        return User.builder()
                .username(usuario.email())
                .password(usuario.passwordHash())   // ya hasheado con BCrypt
                .roles(usuario.nombreRol())         // ADMIN → ROLE_ADMIN
                .build();
    }
}
