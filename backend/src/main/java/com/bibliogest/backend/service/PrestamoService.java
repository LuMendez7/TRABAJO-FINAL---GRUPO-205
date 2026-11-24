
package com.bibliogest.backend.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Prestamo;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.PrestamoRepository;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjemplarRepository ejemplarRepository;
    private final NotificacionService notificacionService;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            UsuarioRepository usuarioRepository,
            EjemplarRepository ejemplarRepository, NotificacionService notificacionService) {

        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.notificacionService = notificacionService;
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    public Prestamo buscarPorId(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Préstamo no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listarMisPrestamos(String email) {

        return prestamoRepository.findByUsuario_Email(email);
    }

    @Transactional
    public Prestamo crear(Prestamo prestamo) {

        Long idUsuario = prestamo.getUsuario().getIdUsuario();
        Long idEjemplar = prestamo.getEjemplar().getIdEjemplar();

        Usuario usuario = usuarioRepository.buscarConBloqueo(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Ejemplar ejemplar = ejemplarRepository.buscarConBloqueo(idEjemplar)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ejemplar no encontrado"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El usuario está inactivo");
        }

        if (!Boolean.TRUE.equals(ejemplar.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El ejemplar está inactivo");
        }

        if (!"DISPONIBLE".equalsIgnoreCase(ejemplar.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El ejemplar no está disponible");
        }

        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);
        prestamo.setEstado("ACTIVO");

        ejemplar.setEstado("PRESTADO");
        ejemplarRepository.save(ejemplar);

        return guardarYNotificar(prestamo);
    }

    @Transactional
    public Prestamo devolver(Long id) {

        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Préstamo no encontrado"));

        if ("DEVUELTO".equalsIgnoreCase(prestamo.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El préstamo ya fue devuelto");
        }

        prestamo.setFechaDevolucionReal(java.time.LocalDate.now());
        prestamo.setEstado("DEVUELTO");

        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setEstado("DISPONIBLE");

        ejemplarRepository.save(ejemplar);

        return guardarYNotificar(prestamo);
    }

    private Prestamo guardarYNotificar(Prestamo prestamo) {

        Prestamo guardado = prestamoRepository.save(prestamo);

        String libro = guardado.getEjemplar()
            .getLibro().getTitulo();

        String estado = guardado.getEstado();

        String titulo;
        String mensaje;

        if ("ACTIVO".equals(estado)) {

            titulo = "Préstamo registrado";

            mensaje = "Se registró el préstamo del libro "
                + libro + ". Fecha de devolución: "
                + guardado.getFechaDevolucionPrevista()
                    .format(java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy"));

        } else if ("DEVUELTO".equals(estado)) {

            titulo = "Devolución registrada";

            mensaje = "Recibimos correctamente el libro "
                + libro + ". Gracias por tu devolución.";

        } else {
            return guardado;
        }

        notificacionService.generar(
            guardado.getUsuario().getIdUsuario(),
            titulo,
            mensaje,
            "PRESTAMO_" + estado,
            "PRESTAMO",
            guardado.getIdPrestamo(),
            "PRESTAMO:" + guardado.getIdPrestamo()
                + ":" + estado);

        return guardado;
    }
}
