package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Notificacion;
import com.bibliogest.backend.service.NotificacionService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService servicio;

    public NotificacionController(
            NotificacionService servicio) {

        this.servicio = servicio;
    }

    @GetMapping
    public List<Notificacion> listar(Authentication auth) {

        return servicio.listar(auth.getName());
    }

    @GetMapping("/no-leidas")
    public Map<String, Long> contarNoLeidas(
            Authentication auth) {

        return Map.of(
            "cantidad",
            servicio.contarNoLeidas(auth.getName()));
    }

    @PutMapping("/{id}/leer")
    public Notificacion marcarComoLeida(
            @PathVariable Long id,
            Authentication auth) {

        return servicio.marcarComoLeida(
            id, auth.getName());
    }

    @PutMapping("/leer-todas")
    public Map<String, Integer> marcarTodasComoLeidas(
            Authentication auth) {

        return Map.of(
            "actualizadas",
            servicio.marcarTodasComoLeidas(auth.getName()));
    }
}