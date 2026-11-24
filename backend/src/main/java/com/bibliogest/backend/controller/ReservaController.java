package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Reserva;
import com.bibliogest.backend.service.ReservaService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    public record Solicitud(Long idLibro) {}

    public record Rechazo(String motivo) {}

    public record Retiro(Integer diasPrestamo) {}

    @GetMapping
    public List<Reserva> listar() {
        return servicio.listar();
    }

    @GetMapping("/mis-reservas")
    public List<Reserva> misReservas(Authentication auth) {
        return servicio.listarMisReservas(auth.getName());
    }

    @PostMapping("/solicitar")
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva solicitar(
            Authentication auth,
            @RequestBody Solicitud datos) {

        return servicio.solicitar(
            auth.getName(), datos.idLibro());
    }

    @PutMapping("/{id}/aprobar")
    public Reserva aprobar(@PathVariable Long id) {
        return servicio.aprobar(id);
    }

    @PutMapping("/{id}/rechazar")
    public Reserva rechazar(
            @PathVariable Long id,
            @RequestBody(required = false) Rechazo datos) {

        return servicio.rechazar(
            id, datos == null ? null : datos.motivo());
    }

    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(
            @PathVariable Long id,
            Authentication auth) {

        boolean administrador = auth.getAuthorities()
            .stream()
            .anyMatch(a -> a.getAuthority()
                .equals("ROLE_BIBLIOTECARIO"));

        return servicio.cancelar(
            id, auth.getName(), administrador);
    }

    @PutMapping("/{id}/retirar")
    public Reserva retirar(
            @PathVariable Long id,
            @RequestBody Retiro datos) {

        return servicio.retirar(
            id, datos.diasPrestamo());
    }
}