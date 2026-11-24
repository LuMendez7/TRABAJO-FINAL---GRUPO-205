package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Prestamo;
import com.bibliogest.backend.repository.PrestamoRepository;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class NotificacionScheduler {

    private final PrestamoRepository prestamos;
    private final NotificacionService notificaciones;

    public NotificacionScheduler(
            PrestamoRepository prestamos,
            NotificacionService notificaciones) {

        this.prestamos = prestamos;
        this.notificaciones = notificaciones;
    }

    @Scheduled(
        fixedDelay = 300000,
        initialDelay = 30000
    )
    @Transactional
    public void revisarPrestamos() {

        LocalDate hoy = LocalDate.now();

        System.out.println("[NOTIFICACIONES] Ejecutando revision. Fecha: " + hoy);

        System.out.println("[NOTIFICACIONES] Prestamos activos: " +
            prestamos.findByEstado("ACTIVO").size());

        for (Prestamo prestamo :
                prestamos.findByEstado("ACTIVO")) {

            LocalDate vencimiento =
                prestamo.getFechaDevolucionPrevista();

            if (vencimiento == null) {
                continue;
            }

            String libro = prestamo.getEjemplar()
                .getLibro().getTitulo();

            String fecha = vencimiento.format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            Long id = prestamo.getIdPrestamo();

            Long usuario = prestamo.getUsuario()
                .getIdUsuario();

            System.out.println("[NOTIFICACIONES] Prestamo: " + id +
                " | Usuario: " + usuario +
                " | Vencimiento: " + vencimiento);

            if (vencimiento.isBefore(hoy)) {

                System.out.println("[NOTIFICACIONES] Generando aviso de vencimiento");

                notificaciones.generar(
                    usuario,
                    "Préstamo vencido",
                    "El plazo de devolución del libro "
                        + libro + " venció el " + fecha
                        + ". Acercate a la biblioteca.",
                    "PRESTAMO_VENCIDO",
                    "PRESTAMO",
                    id,
                    "PRESTAMO:" + id + ":VENCIDO");

            } else if (!vencimiento.isAfter(
                    hoy.plusDays(2))) {

                System.out.println("[NOTIFICACIONES] Generando recordatorio");

                notificaciones.generar(
                    usuario,
                    "Próxima devolución",
                    "Recordá devolver el libro "
                        + libro + " hasta el " + fecha + ".",
                    "PRESTAMO_RECORDATORIO",
                    "PRESTAMO",
                    id,
                    "PRESTAMO:" + id + ":RECORDATORIO");
            }
        }
    }
}