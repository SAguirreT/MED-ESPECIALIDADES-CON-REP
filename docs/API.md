# Contrato de la API

Base local: `http://localhost:8080/api`. Fechas `YYYY-MM-DD`, horas `HH:mm` y zona funcional `America/Lima`. JSON UTF-8. El encabezado protegido es `Authorization: Bearer <token>`.

## Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| POST | /auth/signup | Solicitud de cuenta USER inactiva; ignora privilegios enviados |
| POST | /auth/signin | Login con correo y contraseña; devuelve accessToken |
| GET | /auth/me | Usuario actual sin contraseña ni tokenVersion |
| POST | /auth/signout | Revoca todos los tokens del usuario actual |
| PUT | /auth/password | Cambia contraseña propia y revoca tokens |
| GET | /info | Información pública del modo y zona horaria |

```json
{"email":"admin@aurora.demo","password":"AuroraAdmin2026!"}
```

La respuesta de signin incluye `accessToken`, `tokenType`, `expiresIn`, `id`, `username`, `email` y `roles`. El login incorrecto, expiración, revocación o cuenta inactiva devuelve 401 en las rutas protegidas. Las solicitudes públicas de alta responden 201 y requieren aprobación de ADMIN antes de ingresar.

## Catálogos

Para `medicos`, `especialidades`, `consultorios` y `pacientes`:

- `GET /{catalogo}` y `GET /{catalogo}/{id}`: consulta.
- `POST /{catalogo}`: creación, responde 201.
- `PUT /{catalogo}/{id}`: actualización de datos.
- `PATCH /{catalogo}/{id}/estado?estado=true|false`: estado.

Los catálogos devuelven listas completas; la interfaz realiza búsqueda y paginación visual. Pacientes admite escritura por ADMIN y MODERATOR; los demás catálogos solo por ADMIN.

Ejemplos de entrada:

```json
{
  "nombre":"Ana",
  "apellido":"Ejemplo",
  "cmp":"DEMO-900",
  "telefono":"900000000",
  "email":"ana@example.com",
  "especialidadIds":[1]
}
```

```json
{
  "nombre":"Paciente",
  "apellido":"Ficticio",
  "dni":"12345678",
  "fechaNacimiento":"1995-04-12",
  "telefono":"900000001",
  "email":"paciente@example.com"
}
```

Especialidad: `nombre`, `descripcion`. Consultorio: `codigo`, `nombre`, `piso`, `area`.

`POST /medicos/{id}/especialidades` agrega IDs; `PUT` en la misma ruta reemplaza la selección completa. Se envía un arreglo como `[1,2]`. Retirar una especialidad con citas futuras confirmadas se rechaza.

## Horarios y excepciones

| Método | Ruta |
|---|---|
| GET | /horarios |
| GET | /horarios/{id} |
| GET, POST | /horarios/medico/{medicoId} |
| PUT | /horarios/{id} |
| PATCH | /horarios/{id}/estado?estado=false |
| PATCH | /consultorios/horario/{horarioId}/asignar/{consultorioId} |
| GET, POST | /horarios/medico/{medicoId}/bloqueos |
| GET | /bloqueos |
| PATCH | /bloqueos/{id}/cancelar |
| GET, POST | /vacaciones |
| PATCH | /vacaciones/{id}/cancelar |

Horario:

```json
{"diaSemana":"LUNES","horaInicio":"08:00","horaFin":"12:00","duracionCitaMinutos":30,"consultorioId":1}
```

Bloqueo:

```json
{"fecha":"2026-12-21","horaInicio":"10:00","horaFin":"11:00","motivo":"Reunión de equipo"}
```

Vacaciones/permiso:

```json
{"medicoId":1,"fechaInicio":"2026-12-21","fechaFin":"2026-12-23","motivo":"Permiso programado"}
```

## Disponibilidad y citas

`GET /disponibilidad/medico/{id}?fecha=YYYY-MM-DD` devuelve franjas con `horarioId`, `consultorioId`, `consultorio`, `horaInicio`, `horaFin`, `disponible` y `motivo`. Un médico inexistente devuelve 404; uno inactivo devuelve una lista vacía. Las franjas pasadas y ocupadas se marcan no disponibles.

- `GET /citas?desde=YYYY-MM-DD&hasta=YYYY-MM-DD`: rango máximo de 366 días.
- `POST /citas`: reservar.
- `PUT /citas/{id}`: reprogramar una cita confirmada.
- `PATCH /citas/{id}/cancelar`: cuerpo `{"motivo":"Solicitud del paciente"}`.
- `PATCH /citas/{id}/atender`: registra atención si la cita ya comenzó.

Reserva y reprogramación usan el mismo contrato. El ID del horario y la hora deben provenir de disponibilidad; el final se calcula en el servidor.

```json
{"pacienteId":1,"medicoId":1,"especialidadId":1,"horarioId":1,"fecha":"2026-12-21","horaInicio":"08:00","motivo":"Control preventivo"}
```

Los IDs del ejemplo son ilustrativos. Consulta los registros de tu base y una fecha futura; no asumas que esos IDs existen.

## Usuarios, auditoría y reportes

- `GET /usuarios`, `POST /usuarios`: ADMIN. Alta con `username`, `email`, `password`, `roles`.
- `PATCH /usuarios/{id}/acceso`: ADMIN. Cuerpo `{"activo":true,"roles":["MODERATOR"]}`. Revoca sesiones y protege al último administrador activo.
- `GET /roles`: ADMIN.
- `GET /auditoria?page=0&size=20&usuario=&tabla=`: ADMIN. Filtros por usuario/tabla, tamaño máximo 100, orden descendente de ID.
- `GET /dashboard`: indicadores diarios y agenda.
- `GET /reportes/medicos-por-especialidad`: médicos activos por especialidad, con estado de especialidad.
- Las exportaciones CSV se construyen en el navegador con los resultados autorizados de citas y reportes.

## Errores

```json
{"status":409,"message":"El paciente tiene otra cita en ese intervalo"}
```

Errores de validación incluyen un objeto `fields`. Un 400 indica una entrada inválida, 401 falta de autenticación, 403 falta de permiso, 404 ausencia del registro y 409 un conflicto. No se exponen sentencias SQL ni trazas en el contrato de errores.

## Cambios respecto al API original

Se conservan las rutas principales, pero las solicitudes de escritura ahora usan DTO. Los horarios reciben `consultorioId`, y los médicos `especialidadIds`; ya no se envían entidades anidadas ni un `medico` redundante en el cuerpo del horario. Toda ruta de negocio exige autenticación. La respuesta de especialidad ya no incluye una lista inversa de médicos, evitando ciclos JSON. Las integraciones antiguas deben adaptar esos cuerpos y obtener un token.
