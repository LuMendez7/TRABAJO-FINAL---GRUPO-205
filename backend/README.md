# Backend

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

En esta carpeta se alojará la API REST desarrollada con Java y Spring Boot.
