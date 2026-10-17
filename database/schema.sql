-- =====================================================
-- BiblioGest - Esquema actual de base de datos
-- Basado en la estructura exportada de MySQL (2026-10-17)
-- Uso: crear una base de datos NUEVA y VACÍA.
-- No ejecutar sobre una base existente con información.
-- =====================================================

CREATE DATABASE IF NOT EXISTS bibliogest
    CHARACTER SET utf8mb4;
USE bibliogest;

CREATE TABLE categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE autor (
    id_autor BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_autor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE libro (
    id_libro BIGINT NOT NULL AUTO_INCREMENT,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(20) DEFAULT NULL,
    editorial VARCHAR(150) DEFAULT NULL,
    anio_publicacion INT DEFAULT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    id_categoria BIGINT NOT NULL,
    PRIMARY KEY (id_libro),
    KEY fk_libro_categoria (id_categoria),
    CONSTRAINT fk_libro_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE libro_autor (
    id_libro BIGINT NOT NULL,
    id_autor BIGINT NOT NULL,
    PRIMARY KEY (id_libro, id_autor),
    KEY fk_libro_autor_autor (id_autor),
    CONSTRAINT fk_libro_autor_libro
        FOREIGN KEY (id_libro) REFERENCES libro(id_libro),
    CONSTRAINT fk_libro_autor_autor
        FOREIGN KEY (id_autor) REFERENCES autor(id_autor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ejemplar (
    id_ejemplar BIGINT NOT NULL AUTO_INCREMENT,
    id_libro BIGINT NOT NULL,
    codigo_inventario VARCHAR(50) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_ejemplar),
    UNIQUE KEY codigo_inventario (codigo_inventario),
    KEY fk_ejemplar_libro (id_libro),
    CONSTRAINT fk_ejemplar_libro
        FOREIGN KEY (id_libro) REFERENCES libro(id_libro)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE usuario (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prestamo (
    id_prestamo BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario BIGINT NOT NULL,
    id_ejemplar BIGINT NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_prevista DATE NOT NULL,
    fecha_devolucion_real DATE DEFAULT NULL,
    estado VARCHAR(30) NOT NULL,
    PRIMARY KEY (id_prestamo),
    KEY fk_prestamo_usuario (id_usuario),
    KEY fk_prestamo_ejemplar (id_ejemplar),
    CONSTRAINT fk_prestamo_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_prestamo_ejemplar
        FOREIGN KEY (id_ejemplar) REFERENCES ejemplar(id_ejemplar)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Este script define solamente tablas y relaciones.
-- No incluye datos de prueba ni cuentas de usuario.
-- Reglas como impedir préstamos duplicados se validan en el backend.
