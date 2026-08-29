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

El sistema permitirá centralizar la información relacionada con libros, ejemplares, usuarios, préstamos, devoluciones, reemplazando registros manuales o información distribuida en diferentes medios.

La propuesta busca desarrollar una herramienta sencilla y accesible que pueda ser utilizada tanto por el personal de una biblioteca como por sus lectores.

---

## Problemática

En el relevamiento inicial se tomó como referencia el funcionamiento real de una biblioteca escolar en la que se desempeña uno de los integrantes del equipo.

Actualmente, los préstamos y las devoluciones de libros se registran de forma manual en hojas A4 impresas. El catálogo de la biblioteca fue digitalizado previamente en una planilla de Excel, pero para el trabajo cotidiano también se utiliza una versión impresa.

Cuando un alumno o docente consulta por un determinado libro, el bibliotecario debe buscar la información en los registros disponibles para comprobar si el material forma parte del catálogo y verificar su situación. De la misma manera, el seguimiento de los préstamos se realiza consultando manualmente el registro para conocer cuándo una persona retiró un libro y cuándo fue devuelto.

Esta forma de trabajo dificulta principalmente la búsqueda rápida de los materiales y hace que las consultas dependan de la intervención del bibliotecario. Actualmente, los alumnos y docentes no cuentan con una herramienta que les permita consultar por su cuenta el catálogo de la biblioteca.

A partir de esta problemática surge la necesidad de centralizar la información en un sistema web que facilite la gestión de los libros, ejemplares, usuarios, préstamos y devoluciones, y que además permita consultar el catálogo de manera más sencilla.

---

## Solución propuesta

Se propone desarrollar BiblioGest, una aplicación web que permita centralizar los principales procesos de gestión de una biblioteca.

El personal bibliotecario podrá administrar usuarios, libros, autores, categorías y ejemplares, además de registrar préstamos y devoluciones.

Los lectores podrán consultar el catálogo, realizar búsquedas y conocer la disponibilidad de los materiales.

El sistema contará con diferentes roles y permisos de acceso según el tipo de usuario.

---

## Actores involucrados

Para definir las funcionalidades de BiblioGest se identifican los principales actores que intervienen en el funcionamiento de una biblioteca y que tendrán interacción con el sistema.

---

### Bibliotecario / Administrador

Es el principal responsable de la gestión de la biblioteca y tendrá acceso a las funciones administrativas del sistema.

Sus principales necesidades son:

* Registrar y actualizar libros, autores y categorías.
* Administrar los ejemplares físicos disponibles.
* Registrar y gestionar usuarios de la biblioteca.
* Registrar préstamos y devoluciones.
* Consultar qué ejemplares se encuentran disponibles o prestados.
* Identificar préstamos que hayan superado su fecha prevista de devolución.
* Mantener centralizada y organizada la información de la biblioteca.

---

### Lector

Es la persona que utiliza los servicios de la biblioteca y tendrá acceso a las funciones de consulta habilitadas por el sistema.

Sus principales necesidades son:

* Consultar el catálogo de libros.
* Buscar libros por diferentes criterios.
* Conocer la disponibilidad de ejemplares.
* Consultar sus préstamos activos.
* Conocer las fechas previstas de devolución.

El lector no tendrá acceso a las funciones administrativas, como registrar o modificar libros, usuarios, ejemplares o préstamos.

---

### Usuario de referencia

Para el relevamiento inicial del proyecto se cuenta con un usuario real vinculado al contexto de aplicación: uno de los integrantes del equipo se desempeña como bibliotecario en una institución educativa.

Esta experiencia permite conocer de manera directa tareas habituales relacionadas con la organización del material bibliográfico, la consulta de disponibilidad y la gestión de préstamos y devoluciones.

A partir de este contexto se utilizará la experiencia del usuario de referencia para relevar el funcionamiento actual, identificar necesidades concretas y validar las funcionalidades propuestas para BiblioGest.

---

## Objetivo general

Desarrollar una aplicación web para centralizar y facilitar la gestión de una biblioteca escolar, permitiendo administrar libros, ejemplares, usuarios, préstamos y devoluciones, y brindando a los lectores la posibilidad de consultar el catálogo y la disponibilidad de los materiales.

---

## Objetivos específicos

* Centralizar en una única aplicación la información relacionada con libros, autores, categorías, ejemplares, usuarios y préstamos.

* Facilitar el registro y seguimiento de los préstamos y devoluciones realizados en la biblioteca.

* Permitir conocer el estado de los ejemplares para identificar cuáles se encuentran disponibles o prestados.

* Facilitar la búsqueda de libros mediante diferentes criterios, como título, autor o categoría.

* Permitir que los lectores consulten el catálogo y la disponibilidad de los materiales sin depender exclusivamente de la consulta al bibliotecario.

* Permitir que los lectores registrados consulten la información correspondiente a sus propios préstamos.

* Diferenciar las funcionalidades disponibles según el rol del usuario, separando las tareas administrativas de las funciones de consulta.

---

## Alcance del sistema

BiblioGest será una aplicación web orientada a la gestión de una biblioteca. El sistema contará con dos tipos principales de usuarios: Bibliotecario / Administrador y Lector, cada uno con funcionalidades diferentes según su rol.

### Funcionalidades del Bibliotecario / Administrador

#### Gestión de usuarios

Permitirá registrar nuevos usuarios de la biblioteca, consultar los usuarios existentes y modificar sus datos. Estos usuarios podrán ser asociados posteriormente a los préstamos realizados.

#### Gestión de libros

Permitirá registrar los libros que forman parte del catálogo de la biblioteca, consultar su información y modificar los datos registrados cuando sea necesario.

#### Gestión de autores

Permitirá registrar y consultar autores, asociándolos con los libros correspondientes del catálogo.

#### Gestión de categorías

Permitirá organizar los libros en diferentes categorías para facilitar su clasificación y posterior búsqueda dentro del sistema.

#### Gestión de ejemplares

Permitirá registrar los ejemplares físicos correspondientes a cada libro y consultar su estado para conocer si se encuentran disponibles o prestados.

#### Gestión de préstamos

Permitirá registrar el préstamo de un ejemplar a un usuario, almacenando la fecha en que se realiza y la información necesaria para efectuar su seguimiento. Solo podrán prestarse ejemplares que se encuentren disponibles.

#### Gestión de devoluciones

Permitirá registrar la devolución de un ejemplar previamente prestado. Una vez registrada la devolución, el ejemplar volverá a quedar disponible para futuros préstamos.

#### Control de préstamos

Permitirá consultar los préstamos registrados y conocer qué usuario posee cada ejemplar, la fecha en que fue retirado y si posteriormente fue devuelto.

### Funcionalidades del Lector

#### Consulta del catálogo

Permitirá consultar los libros que forman parte del catálogo de la biblioteca sin necesidad de solicitar la búsqueda directamente al bibliotecario.

#### Búsqueda de libros

Permitirá buscar materiales dentro del catálogo utilizando criterios definidos por el sistema, como título, autor o categoría.

#### Consulta de disponibilidad

Permitirá conocer si un libro posee ejemplares disponibles para préstamo.

#### Consulta de préstamos propios

Los lectores que ingresen al sistema podrán consultar los préstamos asociados a su usuario y la información correspondiente a cada uno de ellos.

### Acceso y roles

El sistema contará con autenticación de usuarios y control de acceso según el rol. El Bibliotecario / Administrador tendrá acceso a las funciones de gestión, mientras que el Lector tendrá acceso únicamente a las funcionalidades de consulta habilitadas.

---

## Producto Mínimo Viable (MVP)

El Producto Mínimo Viable de BiblioGest estará compuesto por las funcionalidades esenciales necesarias para resolver la problemática identificada en la biblioteca tomada como referencia.

El MVP incluirá:

* Autenticación de usuarios y diferenciación de roles.
* Gestión de usuarios.
* Gestión de libros.
* Gestión de autores.
* Gestión de categorías.
* Gestión de ejemplares.
* Registro de préstamos.
* Registro de devoluciones.
* Consulta y control de préstamos.
* Consulta del catálogo.
* Búsqueda de libros.
* Consulta de disponibilidad de ejemplares.
* Consulta de préstamos propios por parte del lector.

Estas funcionalidades permitirán centralizar la información que actualmente se encuentra distribuida entre registros en papel y planillas, facilitar las tareas administrativas del bibliotecario y brindar a los lectores una herramienta para consultar el catálogo y la disponibilidad de los materiales.

---

## Fuera de alcance del MVP

Para mantener un alcance viable durante la primera versión del proyecto, las siguientes funcionalidades no formarán parte del MVP:

* Aplicación móvil nativa.
* Gestión de múltiples bibliotecas o sucursales.
* Integración con sistemas externos.
* Pago de multas o sanciones en línea.
* Notificaciones automáticas por correo electrónico o mensajería.
* Recomendaciones automáticas de libros.
* Estadísticas avanzadas y generación de reportes.
* Lectura o generación de códigos de barras o códigos QR.
* Reservas de libros.

Estas funcionalidades podrán evaluarse como posibles mejoras o ampliaciones futuras una vez que las funciones principales del sistema se encuentren implementadas y validadas.

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
