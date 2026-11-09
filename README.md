# Proyecto Veterinaria Huesitos - Avance 2 (Sistema Web)

Sistema web para gestión de una clínica veterinaria, desarrollado con Java 25, Spring Boot 4.1.1 y PostgreSQL 18. Este avance implementa el 100 % de las 16 funcionalidades del sistema (4 actores x 4 funcionalidades):
- **Cliente:** Registro de tutor y mascotas (edad en años y peso), solicitud de citas con validación de horarios, catálogo de servicios, atención al cliente y gestión de perfil.
- **Recepcionista:** Agenda médica, asignación rápida de veterinarios (`PATCH /api/citas/{id}/veterinario`), respuesta a consultas y Triaje de Emergencia Express con cola prioritaria en memoria RAM.
- **Veterinario:** Citas asignadas vs agenda médica global, atención clínica con diagnóstico/tratamiento (desencolando automáticamente al paciente de la memoria) e historial clínico.
- **Administrador:** Gestión integral de usuarios y roles RBAC (BCrypt), catálogo de tarifas y control de agenda.
- **Estructuras de datos:** `Deque` (`ArrayDeque`) con `addFirst` $O(1)$ para emergencias y `addLast` $O(1)$ para citas regulares (sincronizada para concurrencia), `HashMap` con precarga `@PostConstruct` para tarifas en $O(1)$, y transacciones atómicas `@Transactional`.

## Requisitos
- **Java 25**
- **Maven**
- **PostgreSQL 18** (base de datos `veterinaria` creada).

## Configuración y Ejecución
La aplicación está configurada para **fallar rápido** si no detecta las variables de entorno de seguridad (no tiene credenciales escritas en código).

Antes de ejecutar, abre una terminal PowerShell en la raíz del proyecto y configura:

```powershell
# 1. Contraseña de tu motor local de PostgreSQL
$env:DB_PASSWORD = "admin123"

# 2. Secreto para firmar los tokens JWT (Mínimo 32 caracteres)
$env:JWT_SECRET = "k9#Xp2$vL8zQw1Yn5Fm4Jv7Bx0Rt3W_z"

# 3. Contraseña inicial para el superadministrador
$env:ADMIN_PASSWORD = "AdminVet2026!"

# 4. Iniciar la aplicación
.\mvnw.cmd spring-boot:run

# 5. Atajo (copiar y pegar este comando en una sola línea en PowerShell):
$env:DB_PASSWORD="admin123"; $env:JWT_SECRET='k9#Xp2$vL8zQw1Yn5Fm4Jv7Bx0Rt3W_z'; $env:ADMIN_PASSWORD="AdminVet2026!"; .\mvnw.cmd spring-boot:run
```

## Credenciales Sembradas
Si el usuario administrador no existe, la app lo crea al iniciar:
- **Email:** `admin@veterinaria.com`
- **Contraseña:** `AdminVet2026!`

Usuarios de prueba:
- **Recepcionista:** `recepcionista@veterinaria.com` | `123456`
- **Veterinario:** `veterinario@veterinaria.com` | `123456`
- **Cliente:** `juancampos@gmail.com` | `123456`

## ¿Cómo probarlo?
1. **Frontend:** Abre `http://localhost:8080/index.html` en el navegador.
2. **Pruebas Automatizadas:** Ejecuta `.\mvnw.cmd test -Dtest=EstructurasDatosTest` para correr la suite de 7 pruebas unitarias (BUILD SUCCESS).
