package com.veterinaria.clinica.config;

import com.veterinaria.clinica.security.JwtAccessDeniedHandler;
import com.veterinaria.clinica.security.JwtAuthEntryPoint;
import com.veterinaria.clinica.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion principal de Spring Security con control de acceso basado en roles (RBAC).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint authEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          JwtAuthEntryPoint authEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // --- Rutas publicas ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/index.html", "/registro.html",
                                "/cliente.html", "/admin.html",
                                "/recepcionista.html", "/veterinario.html").permitAll()
                        // --- Recursos estaticos publicos ---
                        .requestMatchers("/js/**", "/css/**", "/img/**", "/favicon.ico").permitAll()

                        // --- Servicios: consulta publica para la pagina principal y usuarios ---
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**").permitAll()

                        // --- Mascotas ---
                        .requestMatchers("/api/mascotas/mis-mascotas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/mascotas").hasAnyRole("ADMIN", "RECEPCIONISTA", "CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/mascotas/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "CLIENTE")
                        .requestMatchers(HttpMethod.DELETE, "/api/mascotas/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/mascotas/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO", "CLIENTE")

                        // --- Citas ---
                        .requestMatchers("/api/citas/mis-citas").hasAnyRole("CLIENTE", "VETERINARIO", "ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/citas/solicitar").hasRole("CLIENTE")
                        .requestMatchers("/api/citas/emergencia").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/citas/*/cancelar").hasAnyRole("ADMIN", "RECEPCIONISTA", "CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/api/citas/*/veterinario").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/api/citas/pendientes").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                        .requestMatchers(HttpMethod.GET, "/api/citas/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")
                        .requestMatchers(HttpMethod.POST, "/api/citas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.PUT, "/api/citas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")

                        // --- Emergencias Express y Regularizacion ---
                        .requestMatchers("/api/emergencias/**").hasAnyRole("ADMIN", "RECEPCIONISTA", "VETERINARIO")

                        // --- Atenciones e Historial Clinico ---
                        .requestMatchers(HttpMethod.POST, "/api/atenciones/**").hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.GET, "/api/atenciones/mascota/**").hasAnyRole("ADMIN", "VETERINARIO", "RECEPCIONISTA", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/atenciones/mis-atenciones").hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers(HttpMethod.GET, "/api/atenciones/**").hasAnyRole("ADMIN", "VETERINARIO")

                        // --- Atencion al Cliente / Consultas ---
                        .requestMatchers(HttpMethod.POST, "/api/consultas/publica").permitAll()
                        .requestMatchers("/api/consultas/mis-consultas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.POST, "/api/consultas").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PUT, "/api/consultas/*/responder").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.GET, "/api/consultas/**").hasAnyRole("ADMIN", "RECEPCIONISTA")

                        // --- Perfil exclusivo para Clientes ---
                        .requestMatchers("/api/cliente/**").hasRole("CLIENTE")

                        // --- Perfil propio de usuario autenticado ---
                        .requestMatchers("/api/usuarios/perfil").authenticated()

                        // --- Gestion de Usuarios ---
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Cualquier otro endpoint
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:8080",
                "http://localhost:3000",
                "http://127.0.0.1:5500"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
