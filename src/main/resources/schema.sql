-- ============================================================
--  schema.sql  –  Esquema de base de datos (veterinaria)
--  Ejecutado automáticamente al iniciar la aplicación.
--  Idempotente: usa CREATE TABLE IF NOT EXISTS para no fallar
--  si las tablas ya existen de una ejecución anterior.
-- ============================================================

-- ------------------------------------------------------------
-- 1. ROLES  (catálogo fijo: ADMIN, CLIENTE, RECEPCIONISTA, VETERINARIO)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id_rol  SERIAL      PRIMARY KEY,
    nombre  VARCHAR(20) UNIQUE NOT NULL
);

-- ------------------------------------------------------------
-- 2. USUARIOS  (todos los actores del sistema)
--    dni y email tienen restricción UNIQUE para garantizar unicidad.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario      BIGSERIAL    PRIMARY KEY,
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    dni             VARCHAR(8)   UNIQUE NOT NULL,
    telefono        VARCHAR(15),
    direccion       VARCHAR(200),
    email           VARCHAR(150) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,          -- BCrypt hash, nunca texto plano
    id_rol          INTEGER      NOT NULL
                        REFERENCES roles(id_rol),
    activo          BOOLEAN      DEFAULT TRUE,       -- baja lógica
    fecha_registro  TIMESTAMP    DEFAULT NOW()
);

-- ------------------------------------------------------------
-- 3. MASCOTAS  (pertenecen a un usuario con rol CLIENTE)
--    sexo solo puede ser 'M' (macho) o 'H' (hembra).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mascotas (
    id_mascota       BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    especie          VARCHAR(50)  NOT NULL,
    raza             VARCHAR(100),
    sexo             CHAR(1)      CHECK (sexo IN ('M', 'H')),
    fecha_nacimiento DATE,
    peso_kg          NUMERIC(5,2),
    id_cliente       BIGINT       NOT NULL
                         REFERENCES usuarios(id_usuario),
    activo           BOOLEAN      DEFAULT TRUE       -- baja lógica
);

-- ------------------------------------------------------------
-- 4. SERVICIOS  (catálogo de servicios veterinarios)
--    nombre UNIQUE permite ON CONFLICT en data.sql.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS servicios (
    id_servicio  SERIAL       PRIMARY KEY,
    nombre       VARCHAR(100) UNIQUE NOT NULL,
    descripcion  VARCHAR(300),
    precio       NUMERIC(8,2),
    duracion_min INTEGER,
    activo       BOOLEAN      DEFAULT TRUE
);

-- ------------------------------------------------------------
-- 5. CITAS  (agenda de atenciones programadas)
--    estado limitado a tres valores controlados.
--    id_veterinario es opcional (NULL si aún no se asigna).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS citas (
    id_cita        BIGSERIAL   PRIMARY KEY,
    id_mascota     BIGINT      NOT NULL
                       REFERENCES mascotas(id_mascota),
    id_servicio    INTEGER     NOT NULL
                       REFERENCES servicios(id_servicio),
    id_veterinario BIGINT      REFERENCES usuarios(id_usuario),  -- puede ser NULL
    fecha          DATE        NOT NULL,
    hora           TIME        NOT NULL,
    estado         VARCHAR(15) DEFAULT 'PROGRAMADA'
                       CHECK (estado IN ('PROGRAMADA', 'CANCELADA', 'ATENDIDA')),
    motivo         VARCHAR(200)
);
