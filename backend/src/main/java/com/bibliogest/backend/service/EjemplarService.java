package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import org.springframework.stereotype.Service;

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
}