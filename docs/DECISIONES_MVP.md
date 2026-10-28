# Decisiones cerradas del MVP

- La editorial es texto en `Libro`; no existe entidad, tabla ni módulo de editoriales.
- `Ejemplar.estado` controla circulación; `estadoFisico`, `ubicacion` y `activo` representan conservación, ubicación y baja lógica por separado.
- El vencimiento se deriva de la fecha prevista, la fecha del servidor y la ausencia de devolución; consultar no muta préstamos.
- El MVP no incluye multas, reservas, notificaciones ni despliegue público. Las validaciones no realizadas están en [VERIFICACION_MVP.md](VERIFICACION_MVP.md).
