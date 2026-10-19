
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `agenda_lock` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `auditoria` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `accion` varchar(30) NOT NULL,
  `detalle` varchar(255) NOT NULL,
  `fecha_hora` datetime(6) NOT NULL,
  `registro_id` bigint(20) DEFAULT NULL,
  `tabla` varchar(60) NOT NULL,
  `usuario` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bloqueos_horario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `estado` varchar(20) NOT NULL,
  `fecha` date NOT NULL,
  `hora_fin` time NOT NULL,
  `hora_inicio` time NOT NULL,
  `motivo` varchar(255) NOT NULL,
  `medico_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcawdob8vnr731u3a8yvhsasui` (`medico_id`),
  CONSTRAINT `FKcawdob8vnr731u3a8yvhsasui` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `citas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `estado` varchar(20) NOT NULL,
  `fecha` date NOT NULL,
  `hora_fin` time NOT NULL,
  `hora_inicio` time NOT NULL,
  `motivo` varchar(255) NOT NULL,
  `motivo_cancelacion` varchar(255) DEFAULT NULL,
  `consultorio_id` bigint(20) NOT NULL,
  `especialidad_id` bigint(20) NOT NULL,
  `horario_id` bigint(20) NOT NULL,
  `medico_id` bigint(20) NOT NULL,
  `paciente_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKii45k48batxulockxh03nhobf` (`consultorio_id`),
  KEY `FKapkrvku8ylaqff5yx6gmv7hfb` (`especialidad_id`),
  KEY `FKcy6ypt36j9kdknw4y9ei23c5e` (`horario_id`),
  KEY `FKbfl57ey8hx1a3ubtrpy8x9ll` (`medico_id`),
  KEY `FKnqrsxxcuysfcxiekvixm7h8r1` (`paciente_id`),
  CONSTRAINT `FKapkrvku8ylaqff5yx6gmv7hfb` FOREIGN KEY (`especialidad_id`) REFERENCES `especialidades` (`id`),
  CONSTRAINT `FKbfl57ey8hx1a3ubtrpy8x9ll` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`),
  CONSTRAINT `FKcy6ypt36j9kdknw4y9ei23c5e` FOREIGN KEY (`horario_id`) REFERENCES `horarios` (`id`),
  CONSTRAINT `FKii45k48batxulockxh03nhobf` FOREIGN KEY (`consultorio_id`) REFERENCES `consultorios` (`id`),
  CONSTRAINT `FKnqrsxxcuysfcxiekvixm7h8r1` FOREIGN KEY (`paciente_id`) REFERENCES `pacientes` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `consultorios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `area` varchar(100) DEFAULT NULL,
  `codigo` varchar(20) NOT NULL,
  `estado` varchar(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `piso` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `especialidades` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `descripcion` varchar(255) DEFAULT NULL,
  `estado` bit(1) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `horarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dia_semana` enum('DOMINGO','JUEVES','LUNES','MARTES','MIERCOLES','SABADO','VIERNES') NOT NULL,
  `duracion_cita_minutos` int(11) NOT NULL,
  `estado` bit(1) NOT NULL,
  `hora_fin` time NOT NULL,
  `hora_inicio` time NOT NULL,
  `consultorio_id` bigint(20) DEFAULT NULL,
  `medico_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq0ubf55mt2bex575021f8k7t5` (`consultorio_id`),
  KEY `FKa9wvgewv3fu467ilft631q81f` (`medico_id`),
  CONSTRAINT `FKa9wvgewv3fu467ilft631q81f` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`),
  CONSTRAINT `FKq0ubf55mt2bex575021f8k7t5` FOREIGN KEY (`consultorio_id`) REFERENCES `consultorios` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medico_especialidad` (
  `medico_id` bigint(20) NOT NULL,
  `especialidad_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK4kjfod2iy8vssovqex14tc28o` (`medico_id`,`especialidad_id`),
  KEY `FKboaq99cmv18eo1g28x5iq4x73` (`especialidad_id`),
  CONSTRAINT `FKboaq99cmv18eo1g28x5iq4x73` FOREIGN KEY (`especialidad_id`) REFERENCES `especialidades` (`id`),
  CONSTRAINT `FKnf8mqlsi536nkry595045nk0d` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `medicos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apellido` varchar(100) NOT NULL,
  `cmp` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `estado` bit(1) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pacientes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `apellido` varchar(100) NOT NULL,
  `dni` varchar(8) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `estado` bit(1) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuario_roles` (
  `usuario_id` bigint(20) NOT NULL,
  `rol_id` bigint(20) NOT NULL,
  PRIMARY KEY (`usuario_id`,`rol_id`),
  KEY `FKbt9i9yrb9ug88xnh82n9m60pr` (`rol_id`),
  CONSTRAINT `FKbt9i9yrb9ug88xnh82n9m60pr` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKuu9tea04xb29m2km5lwe46ua` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuarios` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `token_version` int(11) NOT NULL,
  `username` varchar(60) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vacaciones_permisos` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `estado` varchar(20) NOT NULL,
  `fecha_fin` date NOT NULL,
  `fecha_inicio` date NOT NULL,
  `motivo` varchar(255) NOT NULL,
  `medico_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2v63s7g578ht9yyikntt2oi1d` (`medico_id`),
  CONSTRAINT `FK2v63s7g578ht9yyikntt2oi1d` FOREIGN KEY (`medico_id`) REFERENCES `medicos` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

