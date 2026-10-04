# Proyecto Veterinaria - Avance 1 (30%)

Sistema web para gestión de una clínica veterinaria, desarrollado con Spring Boot y PostgreSQL. Este avance implementa el 30% del sistema:
- Inicio de sesión (Seguridad JWT).
- Registro público de Cliente y Mascota.
- Panel de Administrador (CRUD Usuarios, Citas, Mascotas).
- Estructuras de datos evaluadas: `HashMap` (Caché de servicios) y `Queue` (Cola FIFO de citas).

## Requisitos
- **Java 25**
- **Maven**
- **PostgreSQL** (base de datos `veterinaria` debe estar creada).

## Configuración y Ejecución
La aplicación está configurada para **fallar rápido** si no detecta las variables de entorno de seguridad (no tiene secretos "hardcodeados").

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

# 4. Atajo 
copia y pega este comando para iniciar el proyecto:
$env:DB_PASSWORD="admin123"; $env:JWT_SECRET='k9#Xp2$vL8zQw1Yn5Fm4Jv7Bx0Rt3W_z'; $env:ADMIN_PASSWORD="AdminVet2026!"; .\mvnw.cmd spring-boot:run
```

## Credenciales Sembradas
Si el usuario administrador no existe, la app lo crea al iniciar:
- **Email:** `admin@veterinaria.com`
- **Contraseña:** `AdminVet2026!` (o el valor que hayas puesto en la variable)

## ¿Cómo probarlo?
1. **Frontend:** Abre `http://localhost:8080/index.html` en el navegador.
2. **Postman:** Importa la colección que está en la carpeta `/postman`. Ejecuta primero el login; la colección guardará automáticamente el token y lo inyectará en las demás peticiones.
3. **Pruebas Unitarias:** Ejecuta `.\mvnw.cmd test` para correr las pruebas sobre la Cola y el HashMap.
