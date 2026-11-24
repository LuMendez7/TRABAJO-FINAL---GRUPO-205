package com.bibliogest.backend.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/fichas")
public class FichaBibliograficaController {

    private final JdbcTemplate jdbc;

    public FichaBibliograficaController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private void verificarLibro(Long id) {
        Integer cantidad = jdbc.queryForObject(
            "SELECT COUNT(*) FROM libro WHERE id_libro = ? AND activo = 1",
            Integer.class, id
        );

        if (cantidad == null || cantidad == 0) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Libro no encontrado"
            );
        }
    }

    @GetMapping("/{id}")
    public Map<String, Object> consultar(@PathVariable Long id) {
        verificarLibro(id);

        var fichas = jdbc.queryForList("""
            SELECT descripcion,
                   portada IS NOT NULL AS tienePortada,
                   portada_eliminada AS portadaEliminada
            FROM ficha_bibliografica
            WHERE id_libro = ?
            """, id);

        if (fichas.isEmpty()) {
            return Map.of(
                "tienePortada", false,
                "portadaEliminada", false
            );
        }

        return fichas.get(0);
    }

    @GetMapping("/{id}/portada")
    public ResponseEntity<byte[]> portada(@PathVariable Long id) {
        verificarLibro(id);

        var filas = jdbc.queryForList("""
            SELECT portada, tipo_imagen
            FROM ficha_bibliografica
            WHERE id_libro = ?
              AND portada IS NOT NULL
              AND portada_eliminada = FALSE
            """, id);

        if (filas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var fila = filas.get(0);

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .contentType(MediaType.parseMediaType(
                (String) fila.get("tipo_imagen")
            ))
            .body((byte[]) fila.get("portada"));
    }

    @PutMapping("/{id}/descripcion")
    public Map<String, Object> guardarDescripcion(
            @PathVariable Long id,
            @RequestBody Map<String, String> datos) {

        verificarLibro(id);

        String descripcion = datos.getOrDefault(
            "descripcion", ""
        );

        if (descripcion == null || descripcion.length() > 5000) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Descripcion invalida o superior a 5000 caracteres"
            );
        }

        jdbc.update("""
            INSERT INTO ficha_bibliografica
                (id_libro, descripcion)
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE descripcion = ?
            """, id, descripcion.trim(), descripcion.trim());

        return consultar(id);
    }

    @PutMapping(
        value = "/{id}/portada",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> guardarPortada(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo)
            throws IOException {

        verificarLibro(id);

        if (archivo.isEmpty() ||
            archivo.getSize() > 5 * 1024 * 1024) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La imagen debe pesar hasta 5 MB"
            );
        }

        byte[] bytes = archivo.getBytes();

        try (var entrada = ImageIO.createImageInputStream(
                new ByteArrayInputStream(bytes))) {

            if (entrada == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Imagen invalida"
                );
            }

            var lectores = ImageIO.getImageReaders(entrada);

            if (!lectores.hasNext()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Imagen invalida"
                );
            }

            var lector = lectores.next();
            String tipo;

            try {
                lector.setInput(entrada);
                String formato = lector.getFormatName();

                if ("JPEG".equalsIgnoreCase(formato)) {
                    tipo = "image/jpeg";
                } else if ("PNG".equalsIgnoreCase(formato)) {
                    tipo = "image/png";
                } else {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Solo se permiten JPG y PNG"
                    );
                }

                if ((long) lector.getWidth(0) *
                    lector.getHeight(0) > 20000000L) {

                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Resolucion de imagen demasiado grande"
                    );
                }

                lector.read(0);

            } finally {
                lector.dispose();
            }

            jdbc.update("""
                INSERT INTO ficha_bibliografica
                    (id_libro, portada, tipo_imagen, portada_eliminada)
                VALUES (?, ?, ?, FALSE)
                ON DUPLICATE KEY UPDATE
                    portada = ?,
                    tipo_imagen = ?,
                    portada_eliminada = FALSE
                """, id, bytes, tipo, bytes, tipo);
        }

        return consultar(id);
    }

    @DeleteMapping("/{id}/portada")
    public Map<String, Object> eliminarPortada(
            @PathVariable Long id) {

        verificarLibro(id);

        jdbc.update("""
            INSERT INTO ficha_bibliografica
                (id_libro, portada_eliminada)
            VALUES (?, TRUE)
            ON DUPLICATE KEY UPDATE
                portada = NULL,
                tipo_imagen = NULL,
                portada_eliminada = TRUE
            """, id);

        return consultar(id);
    }
}