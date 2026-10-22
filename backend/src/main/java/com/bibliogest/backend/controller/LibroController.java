package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.service.LibroService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    @GetMapping
    public List<Libro> listar() {
        return libroService.listar();
    }

    @GetMapping("/{id}")
    public Libro buscarPorId(@PathVariable Long id) {
        return libroService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crear(@RequestBody Libro libro) {
        return libroService.crear(libro);
    }

    @PutMapping("/{id}")
    public Libro actualizar(
            @PathVariable Long id,
            @RequestBody Libro libro) {

        return libroService.actualizar(id, libro);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
    }

    @GetMapping("/activos")
    public List<Libro> listarActivos() {
        return libroService.listarActivos();
    }
}
