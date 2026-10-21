package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Prestamo;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.PrestamoRepository;
import com.bibliogest.backend.repository.UsuarioRepository;
import jakarta.persistence.LockTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class PrestamoService {

    private static final Logger log = LoggerFactory.getLogger(PrestamoService.class);
    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjemplarRepository ejemplarRepository;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           UsuarioRepository usuarioRepository,
                           EjemplarRepository ejemplarRepository) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ejemplarRepository = ejemplarRepository;
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    public Prestamo buscarPorId(Long id) {
        return prestamoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestamo no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Prestamo> listarMisPrestamos(String email) {
        return prestamoRepository.findByUsuario_Email(email);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Prestamo crear(Prestamo datos) {
        if (datos == null || datos.getUsuario() == null
                || datos.getUsuario().getIdUsuario() == null
                || datos.getEjemplar() == null
                || datos.getEjemplar().getIdEjemplar() == null
                || datos.getFechaDevolucionPrevista() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos del prestamo");
        }

        LocalDate hoy = LocalDate.now(ZONA);
        if (datos.getFechaDevolucionPrevista().isBefore(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fecha prevista anterior a hoy");
        }

        Usuario usuario = usuarioRepository.findById(datos.getUsuario().getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Long idEjemplar = datos.getEjemplar().getIdEjemplar();
        Ejemplar ejemplar = bloquearEjemplar(idEjemplar);

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario esta inactivo");
        }
        if (ejemplar.getLibro() == null
                || !Boolean.TRUE.equals(ejemplar.getLibro().getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El libro esta inactivo");
        }
        if (!Boolean.TRUE.equals(ejemplar.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ejemplar esta inactivo");
        }
        if (!"DISPONIBLE".equals(ejemplar.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ejemplar no esta disponible");
        }
        if (prestamoRepository.existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNull(idEjemplar)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El ejemplar tiene un prestamo abierto");
        }

        Prestamo nuevo = new Prestamo();
        nuevo.setUsuario(usuario);
        nuevo.setEjemplar(ejemplar);
        nuevo.setFechaPrestamo(hoy);
        nuevo.setFechaDevolucionPrevista(datos.getFechaDevolucionPrevista());
        nuevo.setFechaDevolucionReal(null);
        nuevo.setEstado("ACTIVO");
        ejemplar.setEstado("PRESTADO");
        ejemplarRepository.save(ejemplar);
        return prestamoRepository.save(nuevo);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Prestamo devolver(Long id) {
        Long idEjemplar = prestamoRepository.findEjemplarIdByPrestamoId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prestamo no encontrado"));

        // Orden de bloqueo compartido con alta y gestion: ejemplar, luego prestamo.
        Ejemplar ejemplar = bloquearEjemplar(idEjemplar);
        Prestamo prestamo;
        try {
            prestamo = prestamoRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Prestamo no encontrado"));
        } catch (PessimisticLockingFailureException | LockTimeoutException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prestamo ocupado", ex);
        }

        if (prestamo.getFechaDevolucionReal() != null
                || !"ACTIVO".equals(prestamo.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El prestamo ya fue devuelto");
        }
        if (prestamoRepository.existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNullAndIdPrestamoNot(
                idEjemplar, id)) {
            log.warn("Devolucion bloqueada: otro prestamo abierto para ejemplar {} y prestamo {}",
                    idEjemplar, id);
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El ejemplar tiene otro prestamo abierto");
        }

        prestamo.setFechaDevolucionReal(LocalDate.now(ZONA));
        prestamo.setEstado("DEVUELTO");
        ejemplar.setEstado("DISPONIBLE");
        ejemplarRepository.save(ejemplar);
        return prestamoRepository.save(prestamo);
    }

    private Ejemplar bloquearEjemplar(Long id) {
        try {
            return ejemplarRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Ejemplar no encontrado"));
        } catch (PessimisticLockingFailureException | LockTimeoutException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ejemplar ocupado", ex);
        }
    }
}
