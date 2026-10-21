-- Aplicar una sola vez a instalaciones existentes, luego de respaldo y verificación del esquema.
-- Las filas anteriores conservarán NULL en ambas columnas.
ALTER TABLE ejemplar
    ADD COLUMN ubicacion VARCHAR(120) NULL;

ALTER TABLE ejemplar
    ADD COLUMN estado_fisico VARCHAR(20) NULL;
