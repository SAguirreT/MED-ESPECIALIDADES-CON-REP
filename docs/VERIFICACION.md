# Evidencias de verificación

Fecha de trabajo: **18 de octubre de 2026, America/Lima**. Las pruebas utilizan únicamente información ficticia.

## Compilación y pruebas automatizadas

Se ejecutó una compilación limpia con Maven:

```text
mvn -B -o clean verify
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Entorno verificado: Windows, Oracle JDK 21.0.12, compilación con `release 17`, Maven 3.9.16, Spring Boot 4.1.1. Se generó el JAR ejecutable `target/medicos-1.0.0.jar`. El resumen original de JUnit está en `junit-results.txt`.

Las 17 pruebas de integración cubren:

1. Seis casos JWT y credenciales correctas/incorrectas.
2. Registro público sin posibilidad de elegir ADMIN y pendiente de activación.
3. Escritura denegada al rol de consulta y revocación al cerrar sesión.
4. Token vencido y cuenta desactivada.
5. Duración cero rechazada y registro de horario sin médico redundante en el cuerpo.
6. Generación acotada cerca de medianoche y tolerancia a registros antiguos de duración cero.
7. Reservas, conflictos con bloqueos y registro de auditoría.
8. Vacaciones canceladas y médico inactivo.
9. Cruces de horarios y CMP duplicado.
10. Dos reservas simultáneas: exactamente una se confirma.
11. Protección del último administrador activo.
12. Paciente con dos citas simultáneas en médicos diferentes.
13. Validación de cruces al editar y reasignar consultorios.
14. Reprogramación que libera el cupo anterior y rechazo de atención futura.
15. Médico inexistente y especialidad no asociada.
16. Ausencia de hashes de contraseña y versión de token en JSON.
17. Dos asignaciones simultáneas al mismo consultorio: exactamente una se confirma.

## Validación con XAMPP

Se creó una instancia **aislada de MariaDB 10.4.32**, incluida en XAMPP, en `127.0.0.1:3308`, con archivos nuevos bajo `data/mysql-review`. No se usó ni modificó la base original `hospital_db`.

Se verificaron:

- Arranque con el perfil `xampp`, MariaDB Connector/J y `MariaDBLegacyDialect`.
- 14 tablas y 13 claves foráneas efectivas en `information_schema.REFERENTIAL_CONSTRAINTS`.
- Persistencia de registros y estados tras reiniciar la aplicación.
- **43 peticiones HTTP** del script `scripts/smoke_api.py`, con los códigos esperados y comprobaciones adicionales sobre los cuerpos JSON.
- Creación de paciente, médico, especialidad, consultorio y horario; reserva, reprogramación, cancelación, bloqueos, ausencias, estados, reportes y auditoría.
- Las pruebas conservan registros QA inactivos y citas canceladas para respetar el historial.

Resultados detallados: `mysql-api-results.json`. Esquema exportado sin datos: `schema-mariadb.sql`.

No se ejecutó una instancia independiente de **Oracle MySQL 8**. Su perfil y controlador están configurados, pero la validación con motor externo realizada corresponde a MariaDB de XAMPP. Las pruebas automatizadas de integración usan H2 en memoria.

## Verificación en navegador

Se abrió la aplicación en el navegador integrado y se comprobó:

- Inicio de sesión como administrador con las cuentas ficticias.
- Panel diario con información persistida.
- Reserva de una cita de prueba para Elena Díaz, inicialmente a las 08:00.
- Reprogramación a las 08:30 y actualización del listado.
- Franjas ocupadas deshabilitadas en el formulario.
- Respuestas reales 200 y 401 en las tarjetas JWT y acceso ADMIN autorizado.
- Consulta de reportes y descarga de `aurora-citas.csv`: **8 columnas y 19 filas de datos** en esa captura de la demostración.
- Diseño de escritorio y adaptación a un ancho móvil de 390 px.

Capturas:

- `capturas/aurora-escritorio.png`
- `capturas/aurora-movil.png`

Los 403 de permisos y las demás reglas de seguridad se verificaron también mediante MockMvc y peticiones HTTP directas. No se afirma que cada combinación posible de formularios haya sido recorrida manualmente en el navegador.

## Comprobaciones adicionales

- JavaScript validado con `node --check`.
- Script de arranque PowerShell analizado sin errores de sintaxis.
- `git diff --check` sin errores.
- Fuentes Java formateadas y assets HTML/CSS/JavaScript organizados para lectura.
- Colección Postman generada como JSON; sus escenarios HTTP equivalentes se ejecutaron con el script de prueba. No se afirma haber ejecutado la colección dentro de la aplicación Postman.

## Límites de esta verificación

No se realizaron pruebas de carga hospitalaria, auditoría externa de seguridad, revisión legal de tratamiento de datos, despliegue público ni ejecución del workflow en GitHub. La aplicación fue validada como entrega académica local con datos ficticios. Los procesos temporales de prueba se detienen al finalizar; la demostración se inicia con el lanzador incluido.

La corrección de sockets del lanzador responde al entorno de Windows observado: si el socket local del JDK no puede crearse, se induce su fallback TCP mediante una ruta temporal deliberadamente inexistente. No se cambian la configuración global de Java ni las políticas del sistema.
