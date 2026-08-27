# BiblioGest - Sistema Web de Gestión de Biblioteca

## Trabajo Final Integrador

Proyecto desarrollado en el marco del Trabajo Final Integrador de la Tecnicatura Universitaria en Programación.

## Integrantes

* Lautaro Lopez
* Luciano David Mendez
* Alejandro Pereyra

## Tutor

* Sergio Andrés Antonini

---

## Descripción del proyecto

BiblioGest es una aplicación web destinada a facilitar la gestión administrativa y operativa de una biblioteca.

El sistema permitirá centralizar la información relacionada con libros, ejemplares, usuarios, préstamos, devoluciones y reservas, reemplazando registros manuales o información distribuida en diferentes medios.

La propuesta busca desarrollar una herramienta sencilla y accesible que pueda ser utilizada tanto por el personal de una biblioteca como por sus lectores.

---

## Problemática

En bibliotecas pequeñas, escolares o institucionales es habitual encontrar procesos de gestión que se realizan mediante planillas, registros manuales o herramientas independientes.

Esto puede dificultar tareas como conocer rápidamente la disponibilidad de un libro, controlar qué ejemplares se encuentran prestados, identificar préstamos vencidos, mantener actualizado el catálogo o consultar el historial de préstamos de un usuario.

La ausencia de una herramienta centralizada puede generar duplicación de información, errores administrativos y dificultades para realizar el seguimiento de los materiales bibliográficos.

---

## Solución propuesta

Se propone desarrollar BiblioGest, una aplicación web que permita centralizar los principales procesos de gestión de una biblioteca.

El personal bibliotecario podrá administrar usuarios, libros, autores, categorías y ejemplares, además de registrar préstamos y devoluciones.

Los lectores podrán consultar el catálogo, realizar búsquedas y conocer la disponibilidad de los materiales.

El sistema contará con diferentes roles y permisos de acceso según el tipo de usuario.

---

## Objetivo general

Desarrollar una aplicación web que permita digitalizar, organizar y centralizar los principales procesos de administración y circulación de materiales dentro de una biblioteca.

---

## Objetivos específicos

* Gestionar los usuarios registrados en la biblioteca.
* Gestionar libros, autores y categorías.
* Registrar los distintos ejemplares disponibles.
* Registrar préstamos y devoluciones.
* Controlar la disponibilidad de los ejemplares.
* Detectar préstamos vencidos.
* Permitir la búsqueda de materiales dentro del catálogo.
* Implementar diferentes permisos según el rol del usuario.
* Mantener la información centralizada en una base de datos.
* Proporcionar una interfaz web sencilla y accesible.

---

## Alcance del proyecto

El sistema estará orientado inicialmente a la gestión de una única biblioteca.

Se implementarán dos tipos principales de usuarios:

### Bibliotecario / Administrador

Podrá:

* Gestionar usuarios.
* Registrar libros.
* Modificar información de libros.
* Administrar autores.
* Administrar categorías.
* Registrar ejemplares.
* Registrar préstamos.
* Registrar devoluciones.
* Consultar préstamos activos.
* Consultar préstamos vencidos.
* Consultar disponibilidad de ejemplares.

### Lector

Podrá:

* Iniciar sesión.
* Consultar el catálogo.
* Buscar libros.
* Consultar autores y categorías.
* Consultar la disponibilidad de los materiales.
* Consultar sus préstamos.

---

## Producto Mínimo Viable (MVP)

Para considerar funcional la primera versión del sistema se implementarán los siguientes módulos:

* Autenticación de usuarios.
* Gestión de usuarios.
* Gestión de libros.
* Gestión de autores.
* Gestión de categorías.
* Gestión de ejemplares.
* Gestión de préstamos.
* Gestión de devoluciones.
* Consulta del catálogo.
* Búsqueda de materiales.
* Control de disponibilidad.

Una vez completadas estas funcionalidades podrán incorporarse características adicionales.

---

## Funcionalidades adicionales

En caso de disponer del tiempo necesario podrán implementarse:

* Reservas de libros.
* Historial de préstamos.
* Estadísticas básicas.
* Listado de libros más prestados.
* Alertas visuales para préstamos vencidos.
* Panel administrativo.

Estas funcionalidades no forman parte del núcleo obligatorio del sistema.

---

## Fuera de alcance

En la primera versión no se contempla:

* Aplicación móvil nativa.
* Pagos electrónicos.
* Integración con otras bibliotecas.
* Chat entre usuarios.
* Sistemas de recomendación mediante inteligencia artificial.
* Integraciones con servicios externos complejos.

Estas funcionalidades podrían considerarse como mejoras futuras.

---

# Tecnologías

## Backend

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Spring Validation
* Spring Security
* Maven

## Frontend

* HTML5
* CSS3
* TypeScript
* React
* Vite

## Base de datos

* MySQL

## Herramientas de desarrollo

* IntelliJ IDEA
* Visual Studio Code
* MySQL Workbench
* Postman
* Git
* GitHub

## Plataforma y despliegue

El sistema será desarrollado como una aplicación web.

Para el despliegue se analizará la utilización de servicios como:

* Vercel para el frontend.
* Render o Railway para el backend.
* Un servicio de base de datos MySQL en la nube.

La plataforma definitiva será seleccionada durante la etapa de implementación de acuerdo con la disponibilidad y compatibilidad de los servicios.

---

# Arquitectura propuesta

El sistema utilizará una arquitectura cliente-servidor.

```text
Frontend Web
     |
     | HTTP / REST
     v
Backend - API REST
     |
     | JPA / Hibernate
     v
Base de Datos MySQL
```

El frontend consumirá los servicios proporcionados por una API REST desarrollada con Spring Boot.

---

# Entidades principales

Inicialmente se consideran las siguientes entidades:

* Usuario
* Libro
* Autor
* Categoría
* Ejemplar
* Préstamo

En una etapa posterior se evaluará la incorporación de:

* Reserva

El modelo definitivo será establecido durante la etapa de diseño de base de datos.

---

# Plan de trabajo

## Etapa 1 - Definición y planificación

* Definición de la problemática.
* Definición del alcance.
* Selección del stack tecnológico.
* Creación del repositorio único.
* Elaboración de la propuesta.

## Etapa 2 - Diseño y arquitectura

* Diseño de la base de datos.
* Elaboración del diagrama entidad-relación.
* Definición de entidades y relaciones.
* Definición de módulos.
* Diseño general de la arquitectura.

## Etapa 3 - Backend

* Configuración de Spring Boot.
* Conexión con la base de datos.
* Desarrollo de entidades.
* Desarrollo de repositorios.
* Desarrollo de servicios.
* Desarrollo de controladores REST.
* Implementación de validaciones.
* Implementación de autenticación y autorización.

## Etapa 4 - Frontend

* Creación del proyecto con React y TypeScript.
* Diseño de las interfaces.
* Desarrollo del catálogo.
* Desarrollo de pantallas administrativas.
* Integración del frontend con la API REST.

## Etapa 5 - Integración y pruebas

* Pruebas de endpoints.
* Pruebas de funcionalidades.
* Corrección de errores.
* Validación de reglas de negocio.

## Etapa 6 - Despliegue

* Configuración del entorno de producción.
* Despliegue del frontend.
* Despliegue del backend.
* Configuración de la base de datos online.

## Etapa 7 - Documentación y presentación

* Elaboración del informe final.
* Actualización de documentación técnica.
* Elaboración del video explicativo.
* Preparación de la defensa oral.

---

# Organización del repositorio

El proyecto se organizará dentro de un único repositorio.

```text
TRABAJO-FINAL---GRUPO-205/
│
├── backend/
│
├── frontend/
│
├── database/
│
├── docs/
│
└── README.md
```

### backend

Contendrá la API REST desarrollada con Java y Spring Boot.

### frontend

Contendrá la aplicación web desarrollada con React y TypeScript.

### database

Contendrá scripts, modelos y documentación correspondiente a la base de datos.

### docs

Contendrá diagramas, documentación técnica, informes y material relacionado con las diferentes entregas.

---

# Estado del proyecto

Actualmente el proyecto se encuentra en la etapa inicial de definición de la problemática, alcance, tecnologías y planificación.

---

## Repositorio del proyecto

Todo el desarrollo, documentación, base de datos, frontend y backend del Trabajo Final Integrador se mantendrá centralizado en este único repositorio de GitHub.
