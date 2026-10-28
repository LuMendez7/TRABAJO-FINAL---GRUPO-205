# Backend

## Requisitos y comandos

- Java 17.
- Maven mediante `mvnw.cmd` en Windows o `./mvnw` en Linux/macOS.
- MySQL 8 o superior para una base nueva: el esquema define `utf8mb4_0900_ai_ci` en `usuario.email`.

Desde esta carpeta, ejecutar las pruebas automatizadas con `mvnw.cmd test` (Windows) o `./mvnw test` (Linux/macOS). El perfil de pruebas configura H2 en memoria y no accede a una base MySQL personal. Para arrancar la API localmente, preparar primero el esquema descrito en `../database/README.md` y usar `mvnw.cmd spring-boot:run`.

## Arranque desde una base vacia

Se necesitan Java 17, Maven (o `mvnw.cmd` en Windows) y MySQL. Crear la base y
las tablas con `database/schema.sql` siguiendo `database/README.md`. El usuario
MySQL configurado en `application.properties` debe poder acceder a `bibliogest`;
su clave se entrega por la variable `DB_PASSWORD`. La aplicacion conserva
`spring.jpa.hibernate.ddl-auto=validate`: no crea ni modifica tablas.

Para generar el primer bibliotecario, definir en el entorno del proceso:

- `BIBLIOGEST_BOOTSTRAP_ENABLED=true`
- `BIBLIOGEST_BOOTSTRAP_EMAIL` (correo de la nueva cuenta)
- `BIBLIOGEST_BOOTSTRAP_PASSWORD` (al menos ocho caracteres)
- `BIBLIOGEST_BOOTSTRAP_NOMBRE`
- `BIBLIOGEST_BOOTSTRAP_APELLIDO`

Ejemplo en PowerShell, con datos ficticios y solo para la sesion actual:

```powershell
$env:DB_PASSWORD = 'clave-mysql-ficticia'
$env:BIBLIOGEST_BOOTSTRAP_ENABLED = 'true'
$env:BIBLIOGEST_BOOTSTRAP_EMAIL = 'bibliotecario@ejemplo.test'
$env:BIBLIOGEST_BOOTSTRAP_PASSWORD = 'clave-ficticia-123'
$env:BIBLIOGEST_BOOTSTRAP_NOMBRE = 'Ana'
$env:BIBLIOGEST_BOOTSTRAP_APELLIDO = 'Ejemplo'
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Al terminar el primer arranque, usar
`$env:BIBLIOGEST_BOOTSTRAP_ENABLED = 'false'` en esa sesion. Sustituir los
valores ficticios por credenciales propias sin guardarlas en archivos del repo.

Iniciar con `mvnw.cmd spring-boot:run` desde `backend` en Windows, o
`./mvnw spring-boot:run` en Linux/macOS. El arranque falla si falta un dato,
el correo es invalido o ya pertenece a otra cuenta. La clave se guarda con
BCrypt. Si existe cualquier cuenta `BIBLIOTECARIO`, no se crea ni modifica
ninguna, aunque el flag permanezca activo. Tras el primer arranque, deshabilitar
el flag o quitarlo (su valor predeterminado es `false`).

Verificar `GET http://localhost:8080/api/auth/me` con HTTP Basic usando la
cuenta creada y comprobar que la respuesta indique `BIBLIOTECARIO`. No registrar
el encabezado Authorization ni la clave en archivos o historiales compartidos.

Las pruebas usan H2 en memoria solo bajo el perfil `test` y se ejecutan desde
`backend` con `mvnw.cmd test` (Windows) o `./mvnw test`. No usan la MySQL personal.
H2 no acredita el comportamiento de bloqueo concurrente de InnoDB.

## Orígenes CORS

El backend acepta por defecto solo `http://localhost:5173` y
`http://localhost:5174`. Para otro entorno, definí la variable
`BIBLIOGEST_CORS_ALLOWED_ORIGINS` como lista CSV de orígenes exactos, sin
barra final ni comodines. Ejemplo ficticio en PowerShell:

```powershell
$env:BIBLIOGEST_CORS_ALLOWED_ORIGINS = 'https://bibliogest.ejemplo.test,https://bibliogest-alterno.ejemplo.test'
```

La configuración CORS reside en Spring Security y solo acepta los métodos
GET, POST, PUT, DELETE y OPTIONS, con los encabezados Authorization y
Content-Type. CORS no reemplaza los permisos por rol. La URL pública del
frontend y la API deben usar HTTPS antes de cualquier despliegue remoto;
HTTP Basic queda limitado al desarrollo local del MVP. No guardes credenciales
ni encabezados Authorization en archivos del repositorio o registros.

En esta carpeta se alojará la API REST desarrollada con Java y Spring Boot.

## Endpoints y roles

La API usa HTTP Basic. Los lectores se crean como `LECTOR`; no hay registro público de cuentas privilegiadas. Las contraseñas se guardan con BCrypt.

| Operación | Endpoint | Permiso |
| --- | --- | --- |
| Catálogo activo y ejemplares activos | `GET /api/libros/activos`, `GET /api/ejemplares/activos` | Público |
| Consultar autores/categorías | `GET /api/autores/**`, `GET /api/categorias/**` | Público |
| Identidad de la sesión | `GET /api/auth/me` | Autenticado |
| Préstamos propios | `GET /api/prestamos/mis-prestamos` | Lector autenticado; solo devuelve su usuario |
| Administración de usuarios, autores, categorías, libros y ejemplares | `/api/usuarios`, `/api/autores`, `/api/categorias`, `/api/libros`, `/api/ejemplares` | Bibliotecario |
| Alta y consulta administrativa, devolución | `/api/prestamos`, `/api/prestamos/{id}/devolver` | Bibliotecario |

Las operaciones REST de altas, ediciones, bajas y devoluciones requieren el rol `BIBLIOTECARIO`; un lector no obtiene permisos por ocultarse o mostrarse una pantalla en el frontend. El catálogo público omite libros inactivos. La editorial se guarda en `Libro`, sin endpoint ni módulo independiente. Los préstamos abiertos y sus fechas históricas se conservan.

La suite automatizada de esta revisión se ejecutó sobre H2. Además, en MySQL 8.0.46/InnoDB se creó `bibliogest_test` desde `schema.sql`, se validó el mapeo con `ddl-auto=validate`, se verificaron el bootstrap y el login HTTP Basic, la concurrencia funcional de préstamos y la devolución. La migración F06 se probó separadamente en `bibliogest_migracion_test`. Las verificaciones y sus límites están en `../docs/VERIFICACION_MVP.md`; la prueba HTTP 201/409 no es una prueba de estrés.
