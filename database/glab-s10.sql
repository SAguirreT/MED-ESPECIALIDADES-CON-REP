CREATE DATABASE IF NOT EXISTS empleados_dev;
CREATE DATABASE IF NOT EXISTS empleados_qa;
CREATE DATABASE IF NOT EXISTS empleados_test;
CREATE DATABASE IF NOT EXISTS empleados_prod;

CREATE TABLE IF NOT EXISTS empleados_dev.empleado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    email VARCHAR(100),
    sueldo DECIMAL(10,2)
);

CREATE TABLE IF NOT EXISTS empleados_qa.empleado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    email VARCHAR(100),
    sueldo DECIMAL(10,2)
);

CREATE TABLE IF NOT EXISTS empleados_test.empleado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    email VARCHAR(100),
    sueldo DECIMAL(10,2)
);

CREATE TABLE IF NOT EXISTS empleados_prod.empleado (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100),
    apellidos VARCHAR(100),
    email VARCHAR(100),
    sueldo DECIMAL(10,2)
);

-- PROD usa ddl-auto=validate, por lo que también necesita el esquema actual
-- de médicos y especialidades antes de iniciar la aplicación.
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS empleados_prod.medicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    apellido VARCHAR(100) NOT NULL,
    cmp VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    estado BIT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS empleados_prod.especialidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(255),
    estado BIT NOT NULL,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS empleados_prod.consultorios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    area VARCHAR(100),
    codigo VARCHAR(20) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    piso VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS empleados_prod.horarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dia_semana ENUM('DOMINGO','JUEVES','LUNES','MARTES','MIERCOLES','SABADO','VIERNES') NOT NULL,
    duracion_cita_minutos INT NOT NULL,
    estado BIT NOT NULL,
    hora_fin TIME NOT NULL,
    hora_inicio TIME NOT NULL,
    consultorio_id BIGINT,
    medico_id BIGINT NOT NULL,
    CONSTRAINT fk_prod_horario_consultorio FOREIGN KEY (consultorio_id)
        REFERENCES empleados_prod.consultorios(id),
    CONSTRAINT fk_prod_horario_medico FOREIGN KEY (medico_id)
        REFERENCES empleados_prod.medicos(id)
);

CREATE TABLE IF NOT EXISTS empleados_prod.medico_especialidad (
    medico_id BIGINT NOT NULL,
    especialidad_id BIGINT NOT NULL,
    KEY idx_prod_medico_especialidad_medico (medico_id),
    KEY idx_prod_medico_especialidad_especialidad (especialidad_id),
    CONSTRAINT fk_prod_me_medico FOREIGN KEY (medico_id)
        REFERENCES empleados_prod.medicos(id),
    CONSTRAINT fk_prod_me_especialidad FOREIGN KEY (especialidad_id)
        REFERENCES empleados_prod.especialidades(id)
);

CREATE TABLE IF NOT EXISTS empleados_prod.bloqueos_horario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    estado VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL,
    hora_fin TIME NOT NULL,
    hora_inicio TIME NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    medico_id BIGINT NOT NULL,
    CONSTRAINT fk_prod_bloqueo_medico FOREIGN KEY (medico_id)
        REFERENCES empleados_prod.medicos(id)
);

CREATE TABLE IF NOT EXISTS empleados_prod.vacaciones_permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    estado VARCHAR(20) NOT NULL,
    fecha_fin DATE NOT NULL,
    fecha_inicio DATE NOT NULL,
    motivo VARCHAR(255),
    medico_id BIGINT NOT NULL,
    CONSTRAINT fk_prod_vacacion_medico FOREIGN KEY (medico_id)
        REFERENCES empleados_prod.medicos(id)
);

SET FOREIGN_KEY_CHECKS = 1;
