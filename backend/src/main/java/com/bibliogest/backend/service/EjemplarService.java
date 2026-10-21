package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.exception.ApiException;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.repository.PrestamoRepository;
import jakarta.persistence.LockTimeoutException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class EjemplarService {

    private static final Set<String> ESTADOS_FISICOS = Set.of("BUENO", "REGULAR", "DANADO");

    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public EjemplarService(EjemplarRepository ejemplarRepository,
                           LibroRepository libroRepository,
                           PrestamoRepository prestamoRepository) {
        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public List<Ejemplar> listar() {
        return ejemplarRepository.findAll();
    }

    public Ejemplar buscarPorId(Long id) {
        return ejemplarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ejemplar no encontrado"));
    }

    @Transactional
    public Ejemplar crear(Ejemplar datos) {
        if (datos == null || datos.getLibro() == null) {
            throw ApiException.validacion("El libro es obligatorio", "libro");
        }
        Long idLibro = ValidacionEntrada.id(datos.getLibro().getIdLibro(), "libro");
        String codigo = ValidacionEntrada.obligatorio(datos.getCodigoInventario(),
                "codigoInventario", 50);
        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> ApiException.noEncontrado("Libro no encontrado"));
        if (!Boolean.TRUE.equals(libro.getActivo())) {
            throw ApiException.conflicto("El libro esta inactivo", "libro");
        }
        if (ejemplarRepository.existsByCodigoInventario(codigo)) {
            throw ApiException.conflicto("El codigo de inventario ya existe", "codigoInventario");
        }
        String ubicacion = ValidacionEntrada.obligatorio(datos.getUbicacion(), "ubicacion", 120);
        String estadoFisico = validarEstadoFisico(datos.getEstadoFisico());
        Ejemplar nuevo = new Ejemplar();
        nuevo.setLibro(libro);
        nuevo.setCodigoInventario(codigo);
        nuevo.setUbicacion(ubicacion);
        nuevo.setEstadoFisico(estadoFisico);
        nuevo.setEstado("DISPONIBLE");
        nuevo.setActivo(true);
        return ejemplarRepository.saveAndFlush(nuevo);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Ejemplar actualizar(Long id, Ejemplar datos) {
        Ejemplar ejemplar = bloquearEjemplar(id);
        if (prestamoRepository.existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNull(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede editar un ejemplar con prestamo abierto");
        }
        if (datos == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos del ejemplar");
        }
        if (datos.isEstadoEnviado() && !Objects.equals(datos.getEstado(), ejemplar.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado de circulacion no se modifica aqui");
        }
        if (datos.isActivoEnviado() && !Objects.equals(datos.getActivo(), ejemplar.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado activo no se modifica aqui");
        }
        if (datos.getLibro() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el libro");
        }

        Long idLibro = ValidacionEntrada.id(datos.getLibro().getIdLibro(), "libro");
        String codigo = ValidacionEntrada.obligatorio(datos.getCodigoInventario(),
                "codigoInventario", 50);

        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Libro no encontrado"));
        if (!Boolean.TRUE.equals(libro.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El libro esta inactivo");
        }

        if (ejemplarRepository.existsByCodigoInventarioAndIdEjemplarNot(codigo, id)) {
            throw ApiException.conflicto("El codigo de inventario ya existe", "codigoInventario");
        }
        String ubicacion = ValidacionEntrada.obligatorio(datos.getUbicacion(), "ubicacion", 120);
        String estadoFisico = validarEstadoFisico(datos.getEstadoFisico());

        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario(codigo);
        ejemplar.setUbicacion(ubicacion);
        ejemplar.setEstadoFisico(estadoFisico);
        return ejemplarRepository.saveAndFlush(ejemplar);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Ejemplar desactivar(Long id) {
        Ejemplar ejemplar = bloquearEjemplar(id);
        if (prestamoRepository.existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNull(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede desactivar un ejemplar con prestamo abierto");
        }
        if (!Boolean.TRUE.equals(ejemplar.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El ejemplar ya se encuentra inactivo");
        }

        ejemplar.setActivo(false);
        return ejemplarRepository.save(ejemplar);
    }

    public List<Ejemplar> listarActivos() {
        return ejemplarRepository.findByActivoTrueAndLibro_ActivoTrue();
    }

    private String validarEstadoFisico(String valor) {
        if (valor == null || !ESTADOS_FISICOS.contains(valor)) {
            throw ApiException.validacion("El estado fisico debe ser BUENO, REGULAR o DANADO", "estadoFisico");
        }
        return valor;
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
