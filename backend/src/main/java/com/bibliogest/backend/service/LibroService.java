package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.exception.ApiException;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.LibroRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final AutorRepository autorRepository;

    public LibroService(LibroRepository libroRepository,
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Libro no encontrado"));
    }

    @Transactional
    public Libro crear(Libro datos) {
        Campos campos = validar(datos);
        Libro nuevo = new Libro();
        aplicar(nuevo, campos);
        if (datos.getActivo() != null) {
            nuevo.setActivo(datos.getActivo());
        }
        return libroRepository.saveAndFlush(nuevo);
    }

    @Transactional
    public Libro actualizar(Long id, Libro datos) {
        Libro libro = buscarPorId(id);
        Campos campos = validar(datos);
        aplicar(libro, campos);
        if (datos.getActivo() != null) {
            libro.setActivo(datos.getActivo());
        }
        return libroRepository.saveAndFlush(libro);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro = buscarPorId(id);
        libroRepository.delete(libro);
        libroRepository.flush();
    }

    public List<Libro> listarActivos() {
        return libroRepository.findByActivoTrue();
    }

    private Campos validar(Libro datos) {
        if (datos == null) {
            throw ApiException.validacion("Faltan datos del libro", null);
        }
        String titulo = ValidacionEntrada.obligatorio(datos.getTitulo(), "titulo", 200);
        String isbn = ValidacionEntrada.opcional(datos.getIsbn(), "isbn", 20);
        String editorial = ValidacionEntrada.opcional(datos.getEditorial(), "editorial", 150);
        ValidacionEntrada.anio(datos.getAnioPublicacion());

        if (datos.getCategoria() == null) {
            throw ApiException.validacion("La categoria es obligatoria", "categoria");
        }
        Long idCategoria = ValidacionEntrada.id(datos.getCategoria().getIdCategoria(), "categoria");
        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> ApiException.noEncontrado("Categoria no encontrada"));

        if (datos.getAutores() == null || datos.getAutores().isEmpty()) {
            throw ApiException.validacion("Se requiere al menos un autor", "autores");
        }
        Set<Long> ids = new LinkedHashSet<>();
        for (Autor recibido : datos.getAutores()) {
            ids.add(ValidacionEntrada.id(recibido == null ? null : recibido.getIdAutor(), "autores"));
        }
        Set<Autor> autores = new HashSet<>();
        for (Long idAutor : ids) {
            autores.add(autorRepository.findById(idAutor)
                    .orElseThrow(() -> ApiException.noEncontrado("Autor no encontrado")));
        }
        return new Campos(titulo, isbn, editorial, datos.getAnioPublicacion(), categoria, autores);
    }

    private void aplicar(Libro libro, Campos campos) {
        libro.setTitulo(campos.titulo());
        libro.setIsbn(campos.isbn());
        libro.setEditorial(campos.editorial());
        libro.setAnioPublicacion(campos.anio());
        libro.setCategoria(campos.categoria());
        libro.setAutores(campos.autores());
    }

    private record Campos(String titulo, String isbn, String editorial, Integer anio,
                          Categoria categoria, Set<Autor> autores) {
    }
}
