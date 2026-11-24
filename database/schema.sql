-- BiblioGest - Esquema final (MySQL 8.0)
-- Solo estructura. Ejecutar exclusivamente en una base nueva y vacia.
-- No incluye datos ni elimina tablas existentes.

CREATE DATABASE IF NOT EXISTS bibliogest CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE bibliogest;

CREATE TABLE `autor` (
  `id_autor` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  PRIMARY KEY (`id_autor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `categoria` (
  `id_categoria` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `libro` (
  `id_libro` bigint NOT NULL AUTO_INCREMENT,
  `titulo` varchar(200) NOT NULL,
  `isbn` varchar(20) DEFAULT NULL,
  `editorial` varchar(150) DEFAULT NULL,
  `anio_publicacion` int DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `id_categoria` bigint NOT NULL,
  PRIMARY KEY (`id_libro`),
  KEY `fk_libro_categoria` (`id_categoria`),
  CONSTRAINT `fk_libro_categoria` FOREIGN KEY (`id_categoria`) REFERENCES `categoria` (`id_categoria`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `libro_autor` (
  `id_libro` bigint NOT NULL,
  `id_autor` bigint NOT NULL,
  PRIMARY KEY (`id_libro`,`id_autor`),
  KEY `fk_libro_autor_autor` (`id_autor`),
  CONSTRAINT `fk_libro_autor_autor` FOREIGN KEY (`id_autor`) REFERENCES `autor` (`id_autor`),
  CONSTRAINT `fk_libro_autor_libro` FOREIGN KEY (`id_libro`) REFERENCES `libro` (`id_libro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ejemplar` (
  `id_ejemplar` bigint NOT NULL AUTO_INCREMENT,
  `id_libro` bigint NOT NULL,
  `codigo_inventario` varchar(50) NOT NULL,
  `estado` varchar(30) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_ejemplar`),
  UNIQUE KEY `codigo_inventario` (`codigo_inventario`),
  KEY `fk_ejemplar_libro` (`id_libro`),
  CONSTRAINT `fk_ejemplar_libro` FOREIGN KEY (`id_libro`) REFERENCES `libro` (`id_libro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `usuario` (
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `rol` varchar(30) NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `estado_cuenta` varchar(20) NOT NULL DEFAULT 'APROBADO',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `prestamo` (
  `id_prestamo` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `id_ejemplar` bigint NOT NULL,
  `fecha_prestamo` date NOT NULL,
  `fecha_devolucion_prevista` date NOT NULL,
  `fecha_devolucion_real` date DEFAULT NULL,
  `estado` varchar(30) NOT NULL,
  PRIMARY KEY (`id_prestamo`),
  KEY `fk_prestamo_usuario` (`id_usuario`),
  KEY `fk_prestamo_ejemplar` (`id_ejemplar`),
  CONSTRAINT `fk_prestamo_ejemplar` FOREIGN KEY (`id_ejemplar`) REFERENCES `ejemplar` (`id_ejemplar`),
  CONSTRAINT `fk_prestamo_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ficha_bibliografica` (
  `id_libro` bigint NOT NULL,
  `descripcion` text,
  `portada` mediumblob,
  `tipo_imagen` varchar(30) DEFAULT NULL,
  `portada_eliminada` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_actualizacion` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_libro`),
  CONSTRAINT `fk_ficha_bibliografica_libro` FOREIGN KEY (`id_libro`) REFERENCES `libro` (`id_libro`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reserva` (
  `id_reserva` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `id_libro` bigint NOT NULL,
  `id_ejemplar` bigint DEFAULT NULL,
  `id_prestamo` bigint DEFAULT NULL,
  `fecha_solicitud` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_aprobacion` datetime DEFAULT NULL,
  `fecha_vencimiento` datetime DEFAULT NULL,
  `fecha_resolucion` datetime DEFAULT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'PENDIENTE',
  `observaciones` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id_reserva`),
  UNIQUE KEY `uk_reserva_prestamo` (`id_prestamo`),
  KEY `idx_reserva_usuario_estado` (`id_usuario`,`estado`),
  KEY `idx_reserva_libro_estado` (`id_libro`,`estado`),
  KEY `idx_reserva_ejemplar_estado` (`id_ejemplar`,`estado`),
  KEY `idx_reserva_vencimiento` (`estado`,`fecha_vencimiento`),
  CONSTRAINT `fk_reserva_ejemplar` FOREIGN KEY (`id_ejemplar`) REFERENCES `ejemplar` (`id_ejemplar`),
  CONSTRAINT `fk_reserva_libro` FOREIGN KEY (`id_libro`) REFERENCES `libro` (`id_libro`),
  CONSTRAINT `fk_reserva_prestamo` FOREIGN KEY (`id_prestamo`) REFERENCES `prestamo` (`id_prestamo`),
  CONSTRAINT `fk_reserva_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`),
  CONSTRAINT `chk_reserva_estado` CHECK ((`estado` in (_utf8mb4'PENDIENTE',_utf8mb4'APROBADA',_utf8mb4'RECHAZADA',_utf8mb4'CANCELADA',_utf8mb4'VENCIDA',_utf8mb4'RETIRADA')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notificacion` (
  `id_notificacion` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `titulo` varchar(150) NOT NULL,
  `mensaje` varchar(1000) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `leida` tinyint(1) NOT NULL DEFAULT '0',
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_lectura` datetime DEFAULT NULL,
  `entidad_referencia` varchar(50) DEFAULT NULL,
  `id_referencia` bigint DEFAULT NULL,
  `clave_evento` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id_notificacion`),
  UNIQUE KEY `uk_notificacion_evento` (`id_usuario`,`clave_evento`),
  KEY `idx_notificacion_usuario_leida` (`id_usuario`,`leida`),
  KEY `idx_notificacion_usuario_fecha` (`id_usuario`,`fecha_creacion`),
  CONSTRAINT `fk_notificacion_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
