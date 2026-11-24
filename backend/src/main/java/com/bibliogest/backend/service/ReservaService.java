package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.*;
import com.bibliogest.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.LockModeType;
import java.util.List;

@Service
public class ReservaService {

    @PersistenceContext
    private EntityManager entityManager;

    private final ReservaRepository reservas;
    private final UsuarioRepository usuarios;
    private final LibroRepository libros;
    private final EjemplarRepository ejemplares;
    private final PrestamoRepository prestamos;
    private final NotificacionService notificacionService;

    private static final List<String> ESTADOS_ACTIVOS =
        List.of("PENDIENTE", "APROBADA");

    private static final int MAX_RESERVAS = 3;
    private static final int HORAS_RETIRO = 48;

    public ReservaService(
            ReservaRepository reservas,
            UsuarioRepository usuarios,
            LibroRepository libros,
            EjemplarRepository ejemplares, PrestamoRepository prestamos, NotificacionService notificacionService) {

        this.reservas = reservas;
        this.usuarios = usuarios;
        this.libros = libros;
        this.ejemplares = ejemplares;
        this.prestamos = prestamos;
        this.notificacionService = notificacionService;
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar() {
        return reservas.findAllByOrderByFechaSolicitudDesc();
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarMisReservas(String email) {
        return reservas
            .findByUsuario_EmailOrderByFechaSolicitudDesc(email);
    }

    @Transactional
    public Reserva solicitar(String email, Long idLibro) {

        if (idLibro == null || idLibro <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debe indicar un libro valido");
        }


        Usuario usuario = usuarios.buscarPorEmailConBloqueo(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        validarLector(usuario);

        Libro libro = libros.findById(idLibro)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Libro no encontrado"));

        if (!Boolean.TRUE.equals(libro.getActivo())) {
            throw conflicto("El libro no se encuentra activo");
        }

        if (reservas.countByUsuario_IdUsuarioAndEstadoIn(
                usuario.getIdUsuario(), ESTADOS_ACTIVOS)
                >= MAX_RESERVAS) {

            throw conflicto("Alcanzaste el limite de 3 reservas activas");
        }

        if (reservas
                .existsByUsuario_IdUsuarioAndLibro_IdLibroAndEstadoIn(
                    usuario.getIdUsuario(),
                    idLibro,
                    ESTADOS_ACTIVOS)) {

            throw conflicto("Ya tenes una reserva activa para este libro");
        }

        var disponibles = ejemplares
            .findByLibro_IdLibroAndActivoTrueAndEstadoOrderByIdEjemplarAsc(
                idLibro, "DISPONIBLE");

        if (disponibles.isEmpty()) {
            throw conflicto("No hay ejemplares disponibles");
        }

        Reserva reserva = new Reserva();

        reserva.setUsuario(usuario);
        reserva.setLibro(libro);
        reserva.setEstado("PENDIENTE");
        reserva.setFechaSolicitud(LocalDateTime.now());

        return guardarYNotificar(reserva);
    }

    @Transactional
    public Reserva aprobar(Long id) {

        Reserva reserva = buscarConBloqueo(id);

        exigirEstado(reserva, "PENDIENTE");

        Usuario usuario = usuarios.buscarConBloqueo(
                reserva.getUsuario().getIdUsuario())
            .orElseThrow(() -> conflicto("Usuario no encontrado"));

        validarLector(usuario);

        if (!Boolean.TRUE.equals(reserva.getLibro().getActivo())) {
            throw conflicto("El libro no se encuentra activo");
        }

        var disponibles = ejemplares
            .findByLibro_IdLibroAndActivoTrueAndEstadoOrderByIdEjemplarAsc(
                reserva.getLibro().getIdLibro(),
                "DISPONIBLE");

        Ejemplar seleccionado = null;

        for (Ejemplar candidato : disponibles) {

            Ejemplar actual = ejemplares.buscarConBloqueo(
                    candidato.getIdEjemplar())
                .orElse(null);

            if (actual != null) {
                entityManager.refresh(actual, LockModeType.PESSIMISTIC_WRITE);
            }

            if (actual != null
                    && Boolean.TRUE.equals(actual.getActivo())
                    && "DISPONIBLE".equals(actual.getEstado())
                    && actual.getLibro().getIdLibro().equals(
                        reserva.getLibro().getIdLibro())) {

                seleccionado = actual;
                break;
            }
        }

        if (seleccionado == null) {
            throw conflicto("No hay ejemplares disponibles para aprobar");
        }

        seleccionado.setEstado("RESERVADO");
        ejemplares.save(seleccionado);

        LocalDateTime ahora = LocalDateTime.now();

        reserva.setEjemplar(seleccionado);
        reserva.setEstado("APROBADA");
        reserva.setFechaAprobacion(ahora);
        reserva.setFechaVencimiento(
            ahora.plusHours(HORAS_RETIRO));

        return guardarYNotificar(reserva);
    }

    @Transactional
    public Reserva rechazar(Long id, String motivo) {

        Reserva reserva = buscarConBloqueo(id);

        exigirEstado(reserva, "PENDIENTE");

        reserva.setEstado("RECHAZADA");
        reserva.setObservaciones(validarObservaciones(motivo));
        reserva.setFechaResolucion(LocalDateTime.now());

        return guardarYNotificar(reserva);
    }

    @Transactional
    public Reserva cancelar(
            Long id, String email, boolean administrador) {

        Reserva reserva = buscarConBloqueo(id);

        if (!administrador &&
                !reserva.getUsuario().getEmail()
                    .equalsIgnoreCase(email)) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No podes cancelar reservas de otros lectores");
        }

        if (!ESTADOS_ACTIVOS.contains(reserva.getEstado())) {
            throw conflicto("La reserva ya fue resuelta");
        }

        liberarEjemplar(reserva);

        reserva.setEstado("CANCELADA");
        reserva.setFechaResolucion(LocalDateTime.now());

        return guardarYNotificar(reserva);
    }

    @Transactional
    public void vencerReservas() {

        LocalDateTime ahora = LocalDateTime.now();

        var vencidas = reservas
            .findByEstadoAndFechaVencimientoLessThanEqual(
                "APROBADA", ahora);

        for (Reserva candidata : vencidas) {

            Reserva reserva = buscarConBloqueo(
                candidata.getIdReserva());

            if ("APROBADA".equals(reserva.getEstado())
                    && reserva.getFechaVencimiento() != null
                    && !reserva.getFechaVencimiento().isAfter(ahora)) {

                liberarEjemplar(reserva);

                reserva.setEstado("VENCIDA");
                reserva.setFechaResolucion(ahora);

                guardarYNotificar(reserva);
            }
        }
    }


    @Transactional
    public Reserva retirar(Long id, Integer diasPrestamo) {

        if (diasPrestamo == null ||
                diasPrestamo < 1 || diasPrestamo > 30) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El prestamo debe durar entre 1 y 30 dias");
        }

        Reserva reserva = buscarConBloqueo(id);

        exigirEstado(reserva, "APROBADA");

        LocalDateTime ahora = LocalDateTime.now();

        if (reserva.getFechaVencimiento() == null ||
                !reserva.getFechaVencimiento().isAfter(ahora)) {

            throw conflicto("El plazo de retiro ha vencido");
        }

        Usuario usuario = usuarios.buscarConBloqueo(
                reserva.getUsuario().getIdUsuario())
            .orElseThrow(() -> conflicto("Usuario no encontrado"));

        validarLector(usuario);

        if (!Boolean.TRUE.equals(reserva.getLibro().getActivo())) {
            throw conflicto("El libro no se encuentra activo");
        }

        if (reserva.getEjemplar() == null) {
            throw conflicto("La reserva no tiene ejemplar asignado");
        }

        Ejemplar ejemplar = ejemplares.buscarConBloqueo(
                reserva.getEjemplar().getIdEjemplar())
            .orElseThrow(() -> conflicto("Ejemplar no encontrado"));

        if (!Boolean.TRUE.equals(ejemplar.getActivo()) ||
                !"RESERVADO".equals(ejemplar.getEstado()) ||
                !ejemplar.getLibro().getIdLibro().equals(
                    reserva.getLibro().getIdLibro())) {

            throw conflicto("El ejemplar reservado no esta disponible");
        }

        Prestamo prestamo = new Prestamo();

        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(ahora.toLocalDate());

        prestamo.setFechaDevolucionPrevista(
            ahora.toLocalDate().plusDays(diasPrestamo));

        prestamo.setEstado("ACTIVO");

        ejemplar.setEstado("PRESTADO");
        ejemplares.save(ejemplar);

        prestamo = prestamos.save(prestamo);

        reserva.setPrestamo(prestamo);
        reserva.setEstado("RETIRADA");
        reserva.setFechaResolucion(ahora);

        return guardarYNotificar(reserva);
    }

    private Reserva guardarYNotificar(Reserva reserva) {

        Reserva guardada = reservas.save(reserva);

        String libro = guardada.getLibro().getTitulo();
        String estado = guardada.getEstado();

        String titulo;
        String mensaje;

        switch (estado) {

            case "PENDIENTE" -> {
                titulo = "Reserva solicitada";
                mensaje = "Recibimos tu solicitud para el libro "
                    + libro + ". Aguarda la aprobación.";
            }

            case "APROBADA" -> {
                titulo = "Reserva aprobada";
                mensaje = "El libro " + libro
                    + " está listo para retirar. Plazo: "
                    + guardada.getFechaVencimiento().format(
                        java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm"));
            }

            case "RECHAZADA" -> {
                titulo = "Reserva rechazada";
                mensaje = "Tu reserva del libro " + libro
                    + " fue rechazada.";

                if (guardada.getObservaciones() != null) {
                    mensaje += " Motivo: "
                        + guardada.getObservaciones();
                }
            }

            case "CANCELADA" -> {
                titulo = "Reserva cancelada";
                mensaje = "La reserva del libro "
                    + libro + " fue cancelada.";
            }

            case "VENCIDA" -> {
                titulo = "Reserva vencida";
                mensaje = "Finalizó el plazo para retirar "
                    + libro + ". El ejemplar fue liberado.";
            }

            case "RETIRADA" -> {
                titulo = "Reserva retirada";
                mensaje = "Retiraste correctamente el libro "
                    + libro + ".";

                Prestamo prestamo = guardada.getPrestamo();

                notificacionService.generar(
                    guardada.getUsuario().getIdUsuario(),
                    "Préstamo registrado",
                    "Retiraste " + libro
                        + ". Fecha de devolución: "
                        + prestamo.getFechaDevolucionPrevista()
                            .format(java.time.format.DateTimeFormatter
                                .ofPattern("dd/MM/yyyy")),
                    "PRESTAMO_ACTIVO",
                    "PRESTAMO",
                    prestamo.getIdPrestamo(),
                    "PRESTAMO:" + prestamo.getIdPrestamo()
                        + ":ACTIVO");
            }

            default -> {
                return guardada;
            }
        }

        notificacionService.generar(
            guardada.getUsuario().getIdUsuario(),
            titulo,
            mensaje,
            "RESERVA_" + estado,
            "RESERVA",
            guardada.getIdReserva(),
            "RESERVA:" + guardada.getIdReserva()
                + ":" + estado);

        return guardada;
    }

    private void liberarEjemplar(Reserva reserva) {

        if (reserva.getEjemplar() == null) {
            return;
        }

        Ejemplar ejemplar = ejemplares.buscarConBloqueo(
                reserva.getEjemplar().getIdEjemplar())
            .orElseThrow(() -> conflicto("Ejemplar no encontrado"));

        if ("RESERVADO".equals(ejemplar.getEstado())) {

            ejemplar.setEstado("DISPONIBLE");
            ejemplares.save(ejemplar);
        }
    }

    private Reserva buscarConBloqueo(Long id) {

        Reserva reserva = reservas.buscarConBloqueo(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        entityManager.refresh(reserva, LockModeType.PESSIMISTIC_WRITE);
        return reserva;
    }

    private void exigirEstado(Reserva reserva, String estado) {

        if (!estado.equals(reserva.getEstado())) {
            throw conflicto("La reserva no se encuentra en el estado requerido");
        }
    }

    private void validarLector(Usuario usuario) {

        if (!Boolean.TRUE.equals(usuario.getActivo())
                || !"LECTOR".equals(usuario.getRol())
                || !"APROBADO".equals(usuario.getEstadoCuenta())) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "La cuenta no esta habilitada para realizar reservas");
        }
    }

    private String validarObservaciones(String texto) {

        if (texto == null) {
            return null;
        }

        if (texto.length() > 500) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las observaciones admiten hasta 500 caracteres");
        }

        return texto.trim();
    }

    private ResponseStatusException conflicto(String mensaje) {

        return new ResponseStatusException(
            HttpStatus.CONFLICT, mensaje);
    }
}