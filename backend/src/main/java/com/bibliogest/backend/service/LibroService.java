package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;

    public LibroService(
            LibroRepository libroRepository,
            CategoriaRepository categoriaRepository,
            AutorRepository autorRepository) {

        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro buscarPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));
    }

    public Libro crear(Libro libro) {

        Long idCategoria = libro.getCategoria().getIdCategoria();

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        libro.setCategoria(categoria);

        Set<Autor> autoresEncontrados = new HashSet<>();

        for (Autor autor : libro.getAutores()) {

            Autor autorEncontrado = autorRepository.findById(autor.getIdAutor())
                    .orElseThrow(() -> new RuntimeException("Autor no encontrado"));

            autoresEncontrados.add(autorEncontrado);
        }

        libro.setAutores(autoresEncontrados);

        return libroRepository.save(libro);
    }
}