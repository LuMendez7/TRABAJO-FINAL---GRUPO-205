# Verificación del MVP de BiblioGest (F09)

## Entorno y comandos ejecutados

- Windows PowerShell; Java Temurin 17.0.20.1, Maven 3.9.16, Node.js v24.19.0 y npm 11.17.0.
- Backend: suite `test` ejecutada con Maven instalado desde `backend`; 70 tests aprobados, 0 fallidos y 0 errores. Usa H2 2.4.240 en memoria con perfil `test`; no prueba DDL ni bloqueos de InnoDB.
- Se intentó `mvnw.cmd test -Dtest=FlujosIntegracionF09Tests`, pero el wrapper falló antes de iniciar Maven con `icm : No se puede indizar en una matriz nula`. La suite completa se ejecutó luego con Maven instalado y disponible localmente.
- Frontend: `npm.cmd ci` terminó correctamente; `npm.cmd run lint` aprobó; `VITE_API_URL=https://api.ejemplo.test npm.cmd run build` aprobó. La URL parametrizada aparece en el bundle generado.
- `git diff --check`: PASÓ después de las ediciones finales; no reportó errores de whitespace.
- MySQL 8.0.46/InnoDB: se creó `bibliogest_test` desde el esquema actual, se verificaron siete tablas InnoDB y Spring Boot inició conectado a esa base con `ddl-auto=validate`. El bootstrap creó el primer bibliotecario con contraseña BCrypt y la autenticación HTTP Basic funcionó.
- En verificaciones manuales, React se conectó al backend y MySQL. Se aprobaron los inicios de sesión BIBLIOTECARIO/LECTOR, la gestión de usuarios, T09, T10, T13, T18, las restricciones administrativas del LECTOR y los flujos de préstamo/devolución. GET `/api/usuarios` como LECTOR devolvió 403; GET `/api/prestamos/mis-prestamos` devolvió 200.
- MySQL/InnoDB: dos solicitudes concurrentes de préstamo dieron HTTP 201 y HTTP 409, con un único préstamo abierto. No fue una prueba de estrés de carga. La devolución dejó el préstamo `DEVUELTO` y el ejemplar `DISPONIBLE`.
- Migración F06 probada en la base aislada `bibliogest_migracion_test`, creada con el esquema pre-F06: el script agregó `ubicacion` y `estado_fisico`; se conservaron 1 libro, 2 ejemplares, 2 préstamos y 2 relaciones. Se conservaron los préstamos activo y devuelto, y los campos nuevos quedaron `NULL` en los registros históricos. La migración no es idempotente.
- T28: URL parametrizada con `VITE_API_URL`, rutas SPA, historial Atrás/Adelante y navegación con Tab, Shift+Tab, Enter y Espacio aprobados manualmente. Se corrigieron estáticamente seis tarjetas interactivas para acceso por teclado.
- Prueba manual de recarga en Chrome: se observó que tanto BIBLIOTECARIO como LECTOR pierden la autenticación al actualizar con F5 y deben iniciar sesión nuevamente. La autenticación HTTP Basic y el estado React permanecen en memoria; el MVP no exige persistir la sesión. Se acepta como limitación conocida. No se almacenan contraseñas ni cabeceras Basic en `localStorage` o `sessionStorage`.

## Matriz de aceptación T01–T28

| ID | Resultado | Evidencia y alcance comprobado |
| --- | --- | --- |
| T01 | PASÓ | En MySQL 8.0.46/InnoDB se creó `bibliogest_test` con el esquema actual; se verificaron siete tablas y Spring inició con `ddl-auto=validate`. El bootstrap creó un bibliotecario BCrypt y el login HTTP Basic funcionó. |
| T02 | PASÓ | `InitialBibliotecarioRunnerTests` invoca nuevamente el runner y conserva una sola cuenta y el mismo hash en H2; no equivale a reiniciar un proceso MySQL. |
| T03 | PASÓ | `ConfiguracionSeguridadF08Tests` consulta el catálogo público; `GestionLibrosF05Tests` verifica que la baja lógica excluye el libro de la consulta activa. |
| T04 | PASÓ | `FlujosIntegracionF09Tests` obtiene 401 anónimo en POST de libro; `ConfiguracionSeguridadF08Tests` verifica 403 para lector. En MySQL de ensayo, GET `/api/usuarios` con LECTOR también devolvió 403. |
| T05 | PASÓ | `PrestamosVencidosF07Tests` comprueba consulta autenticada y que el lector solo ve sus préstamos; manualmente GET `/api/prestamos/mis-prestamos` con LECTOR devolvió 200. |
| T06 | PASÓ | `FlujosIntegracionF09Tests` da de alta un lector con email mayúsculo y espacios, confirma normalización y login Basic. |
| T07 | PASÓ | `FlujosIntegracionF09Tests` vuelve a enviar el email con mayúsculas/espacios; recibe 409 y no aumenta el número de usuarios. |
| T08 | PASÓ | `FlujosIntegracionF09Tests` desactiva el lector, confirma 401 posterior y conserva usuario, préstamo abierto y referencias históricas. |
| T09 | PASÓ | Desde React, el BIBLIOTECARIO creó y editó autores; los cambios se comprobaron en la interfaz. |
| T10 | PASÓ | Desde React, el BIBLIOTECARIO creó y editó categorías; los cambios se comprobaron en la interfaz. |
| T11 | PASÓ | `ValidacionesRestF03Tests` verifica 409 al borrar autor/categoría asociados y conserva sus vínculos. |
| T12 | PASÓ | `GestionLibrosF05Tests` prueba libro con dos autores y editorial textual; el esquema revisado no tiene tabla editorial. |
| T13 | PASÓ | Desde React se editaron libros, incluidos sus datos y asociaciones; operación aprobada manualmente. |
| T14 | PASÓ | `ValidacionesRestF03Tests` rechaza título/autores inválidos y verifica que no haya alta parcial. |
| T15 | PASÓ | `GestionLibrosF05Tests` prueba baja lógica, ausencia en catálogo y conservación de referencias. |
| T16 | PASÓ | `GestionLibrosF05Tests` rechaza baja con préstamo abierto sin cambiar el registro. |
| T17 | PASÓ | `GestionEjemplaresF06Tests` prueba alta física, estado de circulación disponible y activo en H2. |
| T18 | PASÓ | Desde React se editaron ejemplares, incluidos sus datos físicos; la persistencia quedó comprobada. |
| T19 | PASÓ | `GestionEjemplaresF06Tests` comprueba que préstamo abierto bloquea edición/baja y conserva datos. |
| T20 | PASÓ | En MySQL/InnoDB, dos solicitudes concurrentes dieron HTTP 201 y HTTP 409; quedó un único préstamo abierto. Es una verificación de concurrencia funcional, no una prueba de estrés. |
| T21 | PASÓ | `CirculacionF02Tests` cubre fecha pasada y campos faltantes; `FlujosIntegracionF09Tests` confirma 400 para fecha malformada y ejemplar aún disponible. |
| T22 | PASÓ | `CirculacionF02Tests` acepta hoy/mañana y comprueba fecha del servidor en H2. |
| T23 | PASÓ | `CirculacionF02Tests` verifica devolución, fecha efectiva y liberación del ejemplar en H2. La devolución desde React fue aprobada; en MySQL el préstamo quedó `DEVUELTO` y el ejemplar `DISPONIBLE`. |
| T24 | PASÓ | `CirculacionF02Tests` verifica conflicto en segunda devolución y preservación del historial. |
| T25 | PASÓ | `PrestamosVencidosF07Tests` comprueba vencimiento estricto para ayer/hoy/futuro y consultas sin mutación. En React se visualizó la pantalla; no se confirmó manualmente un préstamo realmente vencido. |
| T26 | PASÓ | `PrestamosVencidosF07Tests` comprueba que un préstamo devuelto fuera de término no permanece vencido. |
| T27 | PASÓ | Suite Maven: 70 aprobadas. `npm.cmd run lint` y `npm.cmd run build` aprobados después de `npm.cmd ci`. |
| T28 | PASÓ | CORS permitido/no permitido pasó en pruebas; `VITE_API_URL` parametrizada, rutas SPA e historial Atrás/Adelante se verificaron en navegador. Tab, Shift+Tab, Enter y Espacio permiten operar los controles comprobados. Permisos: GET `/api/usuarios` como LECTOR dio 403 y GET `/api/prestamos/mis-prestamos` dio 200. F5 requiere nuevo login para ambos roles; limitación aceptada por el MVP. |

## Limitaciones para auditoría

La suite automatizada de backend usa H2; no acredita pruebas de estrés ni garantiza el comportamiento bajo cargas de concurrencia altas. La verificación T25 automatizada cubre fechas vencidas, pero en la prueba manual solo se confirmó la visualización de la pantalla: no se verificó necesariamente un préstamo realmente vencido en MySQL. La pérdida de autenticación al actualizar con F5 es conocida y aceptada; no se persiste la contraseña ni la cabecera Basic en `localStorage` o `sessionStorage`. La configuración local y CORS probados no constituyen validación de despliegue HTTPS en producción. La migración F06 se aplica una sola vez y no es idempotente.
