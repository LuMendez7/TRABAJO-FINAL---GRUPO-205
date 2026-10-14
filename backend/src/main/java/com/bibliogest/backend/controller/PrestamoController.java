package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Prestamo;
import com.bibliogest.backend.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<Prestamo> listar() {
        return prestamoService.listar();
    }

    @GetMapping("/{id}")
    public Prestamo buscarPorId(@PathVariable Long id) {
        return prestamoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo crear(@RequestBody Prestamo prestamo) {
        return prestamoService.crear(prestamo);
    }

    @PutMapping("/{id}/devolver")
    public Prestamo devolver(@PathVariable Long id) {
        return prestamoService.devolver(id);
    }
}