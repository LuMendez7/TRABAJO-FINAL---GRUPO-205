package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.service.EjemplarService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ejemplares")
public class EjemplarController {

    private final EjemplarService ejemplarService;

    public EjemplarController(EjemplarService ejemplarService) {
        this.ejemplarService = ejemplarService;
    }

    @GetMapping
    public List<Ejemplar> listar() {
        return ejemplarService.listar();
    }

    @GetMapping("/{id}")
    public Ejemplar buscarPorId(@PathVariable Long id) {
        return ejemplarService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Ejemplar crear(@RequestBody Ejemplar ejemplar) {
        return ejemplarService.crear(ejemplar);
    }

    @PutMapping("/{id}")
    public Ejemplar actualizar(
            @PathVariable Long id,
            @RequestBody Ejemplar ejemplar) {

        return ejemplarService.actualizar(id, ejemplar);
    }

    @PutMapping("/{id}/desactivar")
    public Ejemplar desactivar(@PathVariable Long id) {
        return ejemplarService.desactivar(id);
    }
}