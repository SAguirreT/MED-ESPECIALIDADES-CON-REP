# GLAB-S10 – Configuración de Ambientes

Laboratorio académico integrado al sistema existente de médicos y especialidades. Configura cuatro ambientes mediante Spring Profiles y variables de entorno, agrega un CRUD REST de empleados y expone la configuración activa.

## Tecnologías

- Java 17 y Maven
- Spring Boot, Spring Web MVC, Spring Data JPA y Hibernate
- MySQL

## Requisitos

- JDK 17
- MySQL local, por ejemplo XAMPP en el puerto 3306
- Maven (o el Maven Wrapper incluido)

## Bases de datos

Ejecuta `database/glab-s10.sql` en MySQL Workbench, la consola de MySQL o phpMyAdmin. El script crea las bases `empleados_dev`, `empleados_qa`, `empleados_test` y `empleados_prod`, y la tabla `empleado` en cada una.

Hibernate usa `update` en DEV, QA y TEST. PROD usa `validate`; el script incluye las tablas de empleados y las tablas requeridas por las entidades existentes de médicos y especialidades para que ese perfil pueda iniciar sin modificar el esquema al arrancar.

## Ambientes y variables

| Perfil | Ambiente | Puerto predeterminado | Base de datos |
|---|---|---:|---|
| `dev` | DESARROLLO | 8080 | `empleados_dev` |
| `qa` | CALIDAD | 8082 | `empleados_qa` |
| `test` | PRUEBAS | 8084 | `empleados_test` |
| `prod` | PRODUCCION | 8086 | `empleados_prod` |

`src/main/resources/application.properties` selecciona `dev` si no se define `PROFILE_ACTIVE`. En cada configuración de perfil, `DB_URL`, `DB_USER` y `DB_PASSWORD` son variables requeridas. `SERVER_PORT` puede redefinir el puerto predeterminado.

Spring Boot no carga automáticamente archivos `.env`. `.env.example` es solo una plantilla: copia los valores del perfil deseado en el entorno de ejecución y configura tu contraseña de MySQL de forma local, sin guardar credenciales reales en el repositorio.

### IntelliJ IDEA

Abre **Run > Edit Configurations...**, selecciona la configuración de `MedicosApplication` y agrega las variables en **Environment variables**. Por ejemplo para DEV:

```text
PROFILE_ACTIVE=dev;DB_URL=jdbc:mysql://localhost:3306/empleados_dev?useSSL=false&serverTimezone=America/Lima;DB_USER=root;DB_PASSWORD=;SERVER_PORT=8080
```

Para QA, TEST o PROD cambia el perfil, el nombre de base de datos y el puerto de acuerdo con la tabla. La contraseña vacía de ejemplo solo aplica si tu MySQL local no tiene contraseña.

### PowerShell

Desde la raíz del repositorio, configura las variables en la terminal antes de iniciar:

```powershell
$env:PROFILE_ACTIVE = "dev"
$env:DB_URL = "jdbc:mysql://localhost:3306/empleados_dev?useSSL=false&serverTimezone=America/Lima"
$env:DB_USER = "root"
$env:DB_PASSWORD = ""
$env:SERVER_PORT = "8080"
.\mvnw.cmd spring-boot:run
```

En QA usa `qa`, `empleados_qa`, `8082`; en TEST usa `test`, `empleados_test`, `8084`; en PROD usa `prod`, `empleados_prod`, `8086`.

## Ejecución

1. Inicia MySQL y ejecuta el script SQL.
2. Configura las variables del perfil que utilizarás en IntelliJ IDEA o en tu terminal.
3. Ejecuta `.\mvnw.cmd spring-boot:run` en Windows o `./mvnw spring-boot:run` en Linux/macOS.
4. Abre `http://localhost:<puerto>/configuracion` para revisar el ambiente conectado.

## Endpoints

### Empleados

| Método | Ruta | Resultado |
|---|---|---|
| POST | `/empleados` | Crea un empleado (HTTP 201) |
| GET | `/empleados` | Lista empleados |
| GET | `/empleados/{id}` | Busca un empleado; responde HTTP 404 si no existe |
| PUT | `/empleados/{id}` | Actualiza un empleado |
| DELETE | `/empleados/{id}` | Elimina un empleado (HTTP 204) |

Ejemplo de cuerpo para POST y PUT:

```json
{
  "nombres": "Ana",
  "apellidos": "Pérez",
  "email": "ana@example.com",
  "sueldo": 2500.00
}
```

### Configuración y sistema médico existente

- `GET /configuracion`: informa ambiente, perfil activo, puerto y base de datos obtenidos de la configuración actual.
- Los endpoints existentes de médicos y especialidades conservan sus rutas, por ejemplo `/api/medicos` y `/api/especialidades`.

## Verificación y evidencias

Ejecuta el mismo endpoint `/configuracion` en los puertos 8080, 8082, 8084 y 8086, cambiando las variables y reiniciando la aplicación para cada perfil. En Postman prueba POST, GET, GET por ID, PUT y DELETE en `/empleados`; busca también un ID inexistente para observar el HTTP 404. Las capturas del laboratorio pueden organizarse como CAPTURA 01 a CAPTURA 10, según las secciones solicitadas en la guía GLAB-S10.
