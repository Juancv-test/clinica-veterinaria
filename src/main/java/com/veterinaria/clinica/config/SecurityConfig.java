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
 * Configuración principal de Spring Security.
 *
 * En Spring Security 6+ (y 7) se usa SecurityFilterChain en lugar del antiguo
 * WebSecurityConfigurerAdapter (que fue eliminado).
 * Se usa la DSL con lambdas: csrf(c -> c.disable()), no el estilo encadenado antiguo.
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

    /**
     * Define las reglas de seguridad HTTP.
     * Orden de las reglas: de más específico a más general.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF: no aplica a APIs REST stateless con JWT
                .csrf(csrf -> csrf.disable())

                // Configurar CORS para el frontend en localhost
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Sin sesiones: cada petición se autentica por su propio token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorización por URL (orden importa: primero las más específicas)
                .authorizeHttpRequests(auth -> auth

                        // --- Rutas públicas ---
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/index.html", "/registro.html",
                                "/cliente.html", "/admin.html").permitAll()
                        .requestMatchers("/js/**", "/css/**", "/favicon.ico").permitAll()

                        // --- Servicios: cualquier usuario autenticado (sin importar rol) ---
                        .requestMatchers(HttpMethod.GET, "/api/servicios/**").authenticated()

                        // --- Gestión: solo ADMIN ---
                        // hasRole("ADMIN") internamente busca "ROLE_ADMIN" en las authorities
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/mascotas/**").hasRole("ADMIN")
                        .requestMatchers("/api/clientes/**").hasRole("ADMIN")
                        .requestMatchers("/api/citas/**").hasRole("ADMIN")

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )

                // Manejadores JSON para 401 y 403
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Insertar el filtro JWT antes del filtro de formulario de Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expone el AuthenticationManager como bean.
     * Lo usa AuthController para autenticar email+contraseña en el login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /** Bean de codificador BCrypt. Inyectado en servicios y en AdminUserInitializer. */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración CORS: permite peticiones desde el frontend local.
     * En producción se debería restringir a los orígenes reales del dominio.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:8080",    // mismo servidor (frontend en static)
                "http://localhost:3000",    // React / Vue dev server
                "http://127.0.0.1:5500"    // Live Server de VS Code
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
