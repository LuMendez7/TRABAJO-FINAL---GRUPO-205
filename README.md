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

El sistema permitirá centralizar la información relacionada con libros, ejemplares, usuarios, préstamos y devoluciones, reemplazando registros manuales o información distribuida en diferentes medios.

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

### Lector

Es la persona que utiliza los servicios de la biblioteca y tendrá acceso a las funciones de consulta habilitadas por el sistema.

Sus principales necesidades son:

* Consultar el catálogo de libros.
* Buscar libros por diferentes criterios.
* Conocer la disponibilidad de ejemplares.
* Consultar sus préstamos activos.
* Conocer las fechas previstas de devolución.

El lector no tendrá acceso a las funciones administrativas, como registrar o modificar libros, usuarios, ejemplares o préstamos.

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

# Propuesta de valor

BiblioGest busca simplificar y centralizar tareas que actualmente se realizan mediante registros en papel y planillas de cálculo dentro de la biblioteca escolar tomada como referencia.

La propuesta de valor no consiste solamente en digitalizar la información existente, sino en facilitar el acceso y la consulta de esa información. El bibliotecario podrá gestionar desde un mismo sistema los libros, ejemplares, usuarios, préstamos y devoluciones, reduciendo la necesidad de consultar diferentes registros para realizar las tareas habituales.

Al mismo tiempo, los lectores podrán consultar el catálogo, buscar libros y conocer la disponibilidad de los materiales sin depender exclusivamente de la intervención del bibliotecario.

De esta manera, BiblioGest busca aportar valor principalmente mediante la centralización de la información, la simplificación de las tareas de gestión y una mayor autonomía de los lectores para consultar los recursos disponibles en la biblioteca.

## Análisis de alternativas y diferenciación

Actualmente, en el contexto tomado como referencia, la gestión de la biblioteca se realiza principalmente mediante registros en papel y una planilla de Excel utilizada para el catálogo. Estas herramientas constituyen la alternativa actual frente al sistema propuesto.

También existen sistemas de gestión bibliotecaria que ofrecen funcionalidades más amplias. Sin embargo, BiblioGest se plantea como una solución enfocada específicamente en las necesidades relevadas para una biblioteca escolar y con un alcance adecuado a los recursos y tiempos disponibles para el proyecto.

### Alternativas identificadas

**Alternativa indirecta:** el método utilizado actualmente en la biblioteca tomada como referencia, basado en registros impresos y una planilla de Excel para el catálogo. Esta alternativa permite realizar las tareas básicas, pero requiere la intervención del bibliotecario para consultar la información y no centraliza todos los procesos en una única aplicación.

**Competencia directa:** existen sistemas de gestión bibliotecaria que permiten administrar catálogos, usuarios y circulación de materiales. Para esta etapa del proyecto se los considera como referencia general, sin realizar todavía una comparación comercial exhaustiva, ya que BiblioGest se encuentra orientado específicamente al contexto y necesidades relevadas en una biblioteca escolar.

Los principales aspectos de diferenciación de BiblioGest serán:

* Centralización del catálogo, usuarios, ejemplares, préstamos y devoluciones en una única aplicación.
* Acceso web al catálogo por parte de los lectores.
* Consulta de disponibilidad de los materiales sin necesidad de solicitarla directamente al bibliotecario.
* Interfaz orientada a las funcionalidades necesarias para una biblioteca escolar.
* Separación de funciones mediante roles de Bibliotecario / Administrador y Lector.
* Alcance acotado a las necesidades identificadas, evitando incorporar funcionalidades que no resulten necesarias para el MVP.

La propuesta no busca competir por cantidad de funcionalidades con sistemas bibliotecarios de mayor escala, sino ofrecer una solución sencilla y adecuada al contexto relevado.

## Viabilidad del proyecto

Para determinar si BiblioGest puede desarrollarse dentro del contexto del Trabajo Final Integrador, se analiza su viabilidad desde tres aspectos principales: técnico, operativo y temporal.

### Viabilidad técnica

El proyecto se considera técnicamente viable debido a que el equipo utilizará tecnologías abordadas durante la carrera y herramientas sobre las que posee experiencia previa.

El backend será desarrollado con Java y Spring Boot, el frontend con React y TypeScript y la información será almacenada en una base de datos relacional MySQL.

El alcance definido para el MVP no requiere inicialmente integraciones con sistemas externos ni infraestructura de alta complejidad, lo que permite concentrar el desarrollo en las funcionalidades principales del sistema.

### Viabilidad operativa

BiblioGest responde a necesidades identificadas a partir del funcionamiento real de una biblioteca escolar tomada como referencia.

Las funcionalidades propuestas buscan adaptarse a tareas que actualmente forman parte del trabajo cotidiano, como la consulta del catálogo, la búsqueda de materiales y el registro y seguimiento de préstamos y devoluciones.

La aplicación buscará ofrecer una interfaz sencilla que pueda ser utilizada tanto por el personal bibliotecario para las tareas administrativas como por los lectores para consultar los materiales disponibles.

### Viabilidad temporal

El proyecto se considera viable dentro del período disponible para el Trabajo Final Integrador debido a que se definió un Producto Mínimo Viable con las funcionalidades esenciales.

Las características de mayor complejidad o que no resultan indispensables para resolver la problemática inicial, como reservas, notificaciones automáticas, estadísticas avanzadas, recomendaciones e integraciones externas, fueron excluidas del MVP y podrán evaluarse como mejoras futuras.

Esta delimitación permite priorizar el desarrollo de las funciones principales y disminuir el riesgo de que un alcance excesivo impida completar el proyecto dentro de los plazos establecidos.

## Riesgos y estrategias de mitigación

Durante el desarrollo de BiblioGest pueden presentarse diferentes situaciones que afecten el avance del proyecto. Por este motivo, se identifican inicialmente los siguientes riesgos y posibles acciones para reducir su impacto.

### Alcance excesivo

**Riesgo:** incorporar nuevas funcionalidades durante el desarrollo puede aumentar la complejidad del proyecto y dificultar el cumplimiento de los plazos establecidos.

**Mitigación:** priorizar las funcionalidades definidas en el MVP y dejar las características adicionales para futuras versiones.

### Dificultades técnicas

**Riesgo:** pueden surgir problemas durante la integración entre el frontend, el backend y la base de datos.

**Mitigación:** desarrollar e integrar el sistema de manera progresiva, realizando pruebas durante cada etapa y utilizando principalmente tecnologías conocidas por el equipo.

### Errores o pérdida de información

**Riesgo:** una incorrecta gestión de los datos podría producir inconsistencias en la información de libros, usuarios, ejemplares o préstamos.

**Mitigación:** implementar validaciones, restricciones en la base de datos y pruebas sobre las principales operaciones del sistema.

### Organización del equipo

**Riesgo:** una distribución poco clara de las tareas o retrasos individuales pueden afectar el avance general del proyecto.

**Mitigación:** dividir el desarrollo en tareas concretas, utilizar GitHub para centralizar el trabajo y realizar un seguimiento periódico del progreso del equipo.

### Datos iniciales del catálogo

**Riesgo:** la información proveniente de registros existentes puede contener datos incompletos, duplicados o con formatos diferentes al momento de incorporarla al sistema.

**Mitigación:** revisar y normalizar los datos antes de incorporarlos a la base de datos de BiblioGest.

## Criterios de éxito

Se considerará que el MVP de BiblioGest cumple con su propósito cuando las funcionalidades principales definidas para resolver la problemática puedan utilizarse correctamente.

Los criterios de éxito iniciales serán:

* El bibliotecario puede registrar, consultar y modificar la información de los libros del catálogo.
* El bibliotecario puede registrar y administrar los ejemplares asociados a cada libro.
* El bibliotecario puede registrar usuarios de la biblioteca.
* El sistema permite registrar un préstamo únicamente cuando el ejemplar se encuentra disponible.
* El sistema permite registrar la devolución de un ejemplar y actualizar nuevamente su disponibilidad.
* El bibliotecario puede consultar los préstamos registrados y conocer qué usuario posee cada ejemplar prestado.
* Los lectores pueden consultar el catálogo sin depender de la intervención directa del bibliotecario.
* Los lectores pueden buscar libros por los criterios definidos en el sistema.
* Los lectores pueden conocer si existen ejemplares disponibles de un determinado libro.
* Los usuarios autenticados pueden acceder únicamente a las funcionalidades correspondientes a su rol.
* La información principal de libros, usuarios, ejemplares y préstamos se encuentra centralizada en el sistema.
* Las funcionalidades principales del MVP pueden utilizarse sin errores que impidan completar las operaciones previstas.

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

El desarrollo de BiblioGest se organizará de manera progresiva mediante etapas. La planificación temporal podrá ajustarse de acuerdo con las fechas académicas y el avance real del equipo, priorizando en todo momento las funcionalidades definidas para el MVP.

La estimación inicial es la siguiente:

| Etapa | Tiempo estimado | Entregable principal |
|---|---|---|
| 1. Definición y planificación | 1 semana | Problemática, objetivos, alcance, actores, stack y propuesta definidos |
| 2. Diseño y arquitectura | 1 semana | Modelo de datos, diagrama entidad-relación y arquitectura general |
| 3. Desarrollo del backend | 3 semanas | API REST funcional con acceso a datos, validaciones y seguridad |
| 4. Desarrollo del frontend | 3 semanas | Interfaces principales e integración con la API |
| 5. Integración y pruebas | 2 semanas | MVP integrado, probado y con correcciones realizadas |
| 6. Despliegue | 1 semana | Aplicación disponible en un entorno de prueba o producción |
| 7. Documentación y presentación | 1 semana | Documentación final, video y material para la defensa |

La planificación contempla aproximadamente 12 semanas de trabajo. Las etapas podrán superponerse parcialmente cuando resulte conveniente, especialmente durante el desarrollo del backend y frontend.

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
