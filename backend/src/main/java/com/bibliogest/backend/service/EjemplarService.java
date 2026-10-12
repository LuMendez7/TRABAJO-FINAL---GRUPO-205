package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EjemplarService {

    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;

    public EjemplarService(
            EjemplarRepository ejemplarRepository,
            LibroRepository libroRepository) {

        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
    }

    public List<Ejemplar> listar() {
        return ejemplarRepository.findAll();
    }

    public Ejemplar buscarPorId(Long id) {
        return ejemplarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejemplar no encontrado"));
    }

    public Ejemplar crear(Ejemplar ejemplar) {

        Long idLibro = ejemplar.getLibro().getIdLibro();

        Libro libro = libroRepository.findById(idLibro)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        ejemplar.setLibro(libro);

        return ejemplarRepository.save(ejemplar);
    }

    @Transactional
    public Ejemplar actualizar(Long id, Ejemplar datos) {

        Ejemplar ejemplar = ejemplarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ejemplar no encontrado"));

        Libro libro = libroRepository.findById(datos.getLibro().getIdLibro())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Libro no encontrado"));

        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario(datos.getCodigoInventario());
        ejemplar.setEstado(datos.getEstado());
        ejemplar.setActivo(datos.getActivo());

        return ejemplarRepository.save(ejemplar);
    }

    @Transactional
    public Ejemplar desactivar(Long id) {

        Ejemplar ejemplar = ejemplarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ejemplar no encontrado"));

        if (!ejemplar.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ejemplar ya se encuentra inactivo");
        }

        if ("PRESTADO".equalsIgnoreCase(ejemplar.getEstado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede desactivar un ejemplar que está prestado");
        }

        ejemplar.setActivo(false);

        return ejemplarRepository.save(ejemplar);
    }
}