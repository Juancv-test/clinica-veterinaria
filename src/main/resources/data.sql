-- ============================================================
--  data.sql  –  Datos iniciales (semilla)
--  Idempotente: ON CONFLICT DO NOTHING evita duplicados.
--  El usuario admin NO se inserta aquí; lo crea el
--  CommandLineRunner con BCrypt (clase AdminUserInitializer).
-- ============================================================

-- ------------------------------------------------------------
-- Roles del sistema (4 actores definidos en el caso de uso)
-- ------------------------------------------------------------
INSERT INTO roles (nombre) VALUES ('ADMIN')          ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre) VALUES ('CLIENTE')        ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre) VALUES ('RECEPCIONISTA')  ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (nombre) VALUES ('VETERINARIO')    ON CONFLICT (nombre) DO NOTHING;

-- ------------------------------------------------------------
-- Servicios veterinarios de ejemplo (6 servicios)
-- ON CONFLICT (nombre) DO NOTHING requiere UNIQUE en nombre
-- (ya definido en schema.sql).
-- ------------------------------------------------------------
INSERT INTO servicios (nombre, descripcion, precio, duracion_min) VALUES
    ('Consulta General',
        'Revisión general del estado de salud de la mascota',
        50.00, 30),
    ('Vacunación',
        'Aplicación de vacunas preventivas según el esquema de la mascota',
        80.00, 20),
    ('Baño y Corte',
        'Servicio de baño, secado y corte de pelo profesional',
        60.00, 60),
    ('Desparasitación',
        'Tratamiento interno y externo contra parásitos',
        40.00, 15),
    ('Cirugía Menor',
        'Procedimientos quirúrgicos menores con anestesia local',
        300.00, 90),
    ('Control de Peso',
        'Seguimiento nutricional y control del peso de la mascota',
        30.00, 20)
ON CONFLICT (nombre) DO NOTHING;
