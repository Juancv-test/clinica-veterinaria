package com.veterinaria.clinica.config;

import com.veterinaria.clinica.dao.RolDao;
import com.veterinaria.clinica.dao.UsuarioDao;
import com.veterinaria.clinica.model.Rol;
import com.veterinaria.clinica.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UsuarioDao usuarioDao;
    private final RolDao rolDao;
    private final BCryptPasswordEncoder passwordEncoder;

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
            Rol rolAdmin = rolDao.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado en base de datos."));

            String hash = passwordEncoder.encode(adminPassword);
            Usuario admin = new Usuario(
                    null,
                    "Administrador",
                    "del Sistema",
                    "00000000",
                    null,
                    null,
                    adminEmail,
                    hash,
                    rolAdmin.idRol(),
                    null,
                    true,
                    null
            );

            usuarioDao.save(admin);
        }
    }
}
