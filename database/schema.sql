-- =====================================================
-- BiblioGest - Sistema Web de Gestión de Biblioteca
-- Script inicial de base de datos
-- =====================================================

CREATE DATABASE IF NOT EXISTS bibliogest;

USE bibliogest;

-- =====================================================
-- TABLA: CATEGORIA
-- =====================================================

CREATE TABLE categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
);

-- =====================================================
-- TABLA: LIBRO
-- =====================================================

CREATE TABLE libro (
    id_libro BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    isbn VARCHAR(20),
    editorial VARCHAR(150),
    anio_publicacion INT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    id_categoria BIGINT NOT NULL,

    CONSTRAINT fk_libro_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria)
);

-- =====================================================
-- TABLA: AUTOR
-- =====================================================

CREATE TABLE autor (
    id_autor BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL
);

-- =====================================================
-- TABLA INTERMEDIA: LIBRO_AUTOR
-- =====================================================

CREATE TABLE libro_autor (
    id_libro BIGINT NOT NULL,
    id_autor BIGINT NOT NULL,

    PRIMARY KEY (id_libro, id_autor),

    CONSTRAINT fk_libro_autor_libro
        FOREIGN KEY (id_libro)
        REFERENCES libro(id_libro),

    CONSTRAINT fk_libro_autor_autor
        FOREIGN KEY (id_autor)
        REFERENCES autor(id_autor)
);

-- =====================================================
-- TABLA: EJEMPLAR
-- =====================================================

CREATE TABLE ejemplar (
    id_ejemplar BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_libro BIGINT NOT NULL,
    codigo_inventario VARCHAR(50) NOT NULL UNIQUE,
    estado VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_ejemplar_libro
        FOREIGN KEY (id_libro)
        REFERENCES libro(id_libro)
);

-- =====================================================
-- TABLA: USUARIO
-- =====================================================

CREATE TABLE usuario (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- =====================================================
-- TABLA: PRESTAMO
-- =====================================================

CREATE TABLE prestamo (
    id_prestamo BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_ejemplar BIGINT NOT NULL,
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_prevista DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado VARCHAR(30) NOT NULL,

    CONSTRAINT fk_prestamo_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario),

    CONSTRAINT fk_prestamo_ejemplar
        FOREIGN KEY (id_ejemplar)
        REFERENCES ejemplar(id_ejemplar)
);
