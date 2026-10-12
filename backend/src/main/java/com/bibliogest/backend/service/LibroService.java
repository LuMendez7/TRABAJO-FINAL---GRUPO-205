package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional
    public Libro actualizar(Long id, Libro datos) {

        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Libro no encontrado"));

        Categoria categoria = categoriaRepository.findById(
                        datos.getCategoria().getIdCategoria())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));

        Set<Autor> autores = new HashSet<>();

        for (Autor autorRecibido : datos.getAutores()) {

            Autor autor = autorRepository.findById(autorRecibido.getIdAutor())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Autor no encontrado"));

            autores.add(autor);
        }

        libro.setTitulo(datos.getTitulo());
        libro.setIsbn(datos.getIsbn());
        libro.setEditorial(datos.getEditorial());
        libro.setAnioPublicacion(datos.getAnioPublicacion());
        libro.setActivo(datos.getActivo());
        libro.setCategoria(categoria);
        libro.setAutores(autores);

        return libroRepository.save(libro);
    }

    @Transactional
    public void eliminar(Long id) {

        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Libro no encontrado"));

        libroRepository.delete(libro);
    }

    public List<Libro> listarActivos() {
        return libroRepository.findByActivoTrue();
    }
}