# Módulos del Sistema - BiblioGest

## Introducción

BiblioGest se organizará en diferentes módulos, cada uno con una responsabilidad específica dentro del sistema. Esta división permitirá mantener el proyecto ordenado y facilitar su desarrollo, mantenimiento y futuras modificaciones.

## 1. Módulo de Usuarios

Permitirá administrar los usuarios que utilizarán el sistema.

Funciones principales:

- Registrar usuarios.
- Consultar usuarios registrados.
- Modificar datos de los usuarios.
- Realizar baja lógica de usuarios.
- Diferenciar permisos según el rol del usuario.

## 2. Módulo de Catálogo

Permitirá administrar la información correspondiente a los libros de la biblioteca.

Funciones principales:

- Registrar libros.
- Modificar información de libros.
- Consultar libros.
- Buscar libros por título, autor o categoría.
- Consultar la disponibilidad de los libros.
- Organizar los libros por categorías y autores.

## 3. Módulo de Ejemplares

Permitirá controlar los ejemplares físicos disponibles de cada libro.

Funciones principales:

- Registrar ejemplares.
- Asociar cada ejemplar con un libro.
- Identificar cada ejemplar mediante un código de inventario.
- Consultar el estado de un ejemplar.
- Indicar si un ejemplar se encuentra disponible o prestado.

## 4. Módulo de Préstamos y Devoluciones

Permitirá gestionar la circulación de los ejemplares de la biblioteca.

Funciones principales:

- Registrar un préstamo.
- Asociar el préstamo con un usuario y un ejemplar.
- Registrar la fecha del préstamo.
- Registrar la fecha prevista de devolución.
- Registrar la devolución del ejemplar.
- Consultar préstamos activos.
- Consultar préstamos finalizados.

## 5. Módulo de Autenticación y Roles

Permitirá controlar el acceso al sistema.

Funciones principales:

- Iniciar sesión.
- Validar las credenciales del usuario.
- Identificar el rol del usuario.
- Restringir funcionalidades según los permisos correspondientes.

## 6. Módulo de Consulta del Catálogo

Permitirá que los lectores puedan consultar de manera sencilla el material disponible en la biblioteca.

Funciones principales:

- Visualizar el catálogo.
- Buscar libros.
- Consultar información básica de cada libro.
- Consultar si existen ejemplares disponibles.

## Organización general

Los módulos estarán relacionados entre sí, pero cada uno tendrá responsabilidades específicas. Esta organización busca evitar que todas las funcionalidades se concentren en un único componente y facilitar el desarrollo progresivo del sistema.
