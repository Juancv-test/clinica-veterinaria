package com.veterinaria.clinica.config;

import com.veterinaria.clinica.dao.RolDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.model.Rol;
import com.veterinaria.clinica.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario administrador al iniciar la aplicación,
 * SOLO si aún no existe en la base de datos.
 *
 * CommandLineRunner: Spring lo ejecuta automáticamente después de que
 * el contexto de la aplicación está completamente levantado.
 *
 * La contraseña se lee de la variable de entorno ADMIN_PASSWORD
 * y se hashea con BCrypt antes de guardarla. Nunca se almacena en texto plano.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;
    private final BCryptPasswordEncoder passwordEncoder;

    /** Contraseña del admin leída de la variable de entorno ADMIN_PASSWORD */
    @Value("${admin.password}")
    private String adminPassword;

    public AdminUserInitializer(UsuarioDao usuarioDao,
                                RolDao rolDao,
                                BCryptPasswordEncoder passwordEncoder) {
        this.usuarioDao = usuarioDao;
        this.rolDao = rolDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@veterinaria.com";

        if (!usuarioDao.existsByEmail(adminEmail)) {
            // Obtener el ID del rol ADMIN (ya insertado por data.sql)
            Rol rolAdmin = rolDao.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException(
                            "Rol ADMIN no encontrado. Verifica que data.sql se ejecutó correctamente."));

            // Hashear la contraseña antes de guardarla (BCrypt genera un hash diferente
            // en cada llamada, incluso con la misma contraseña — eso es normal y seguro)
            String hash = passwordEncoder.encode(adminPassword);

            Usuario admin = new Usuario(
                    null,               // id_usuario: lo genera la secuencia BIGSERIAL
                    "Administrador",
                    "del Sistema",
                    "00000000",         // DNI ficticio único para el admin
                    null,               // telefono: opcional
                    null,               // direccion: opcional
                    adminEmail,
                    hash,               // password_hash: BCrypt
                    rolAdmin.idRol(),
                    null,               // nombreRol: solo se usa en consultas con JOIN
                    true,               // activo
                    null                // fecha_registro: la asigna la BD con DEFAULT NOW()
            );

            usuarioDao.save(admin);
            System.out.println("✅ [AdminUserInitializer] Usuario admin creado: " + adminEmail);

        } else {
            System.out.println("ℹ️  [AdminUserInitializer] Admin ya existe: " + adminEmail);
        }
    }
}
