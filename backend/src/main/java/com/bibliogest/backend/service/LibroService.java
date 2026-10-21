package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.exception.ApiException;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.repository.PrestamoRepository;
import jakarta.persistence.LockTimeoutException;
import org.springframework.dao.PessimisticLockingFailureException;
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
    private final EjemplarRepository ejemplarRepository;
    private final PrestamoRepository prestamoRepository;

    public LibroService(LibroRepository libroRepository,
                        CategoriaRepository categoriaRepository,
                        AutorRepository autorRepository,
                        EjemplarRepository ejemplarRepository,
                        PrestamoRepository prestamoRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.autorRepository = autorRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.prestamoRepository = prestamoRepository;
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
        if (datos.isActivoEnviado() && !Boolean.TRUE.equals(datos.getActivo())) {
            throw ApiException.validacion("El libro debe crearse activo", "activo");
        }
        Libro nuevo = new Libro();
        aplicar(nuevo, campos);
        return libroRepository.saveAndFlush(nuevo);
    }

    @Transactional
    public Libro actualizar(Long id, Libro datos) {
        Libro libro = buscarPorId(id);
        if (datos == null) {
            throw ApiException.validacion("Faltan datos del libro", null);
        }
        if (datos.isActivoEnviado()) {
            if (datos.getActivo() == null || !datos.getActivo().equals(libro.getActivo())) {
                throw ApiException.validacion("El estado activo no se modifica aqui", "activo");
            }
        }
        Campos campos = validar(datos);
        aplicar(libro, campos);
        return libroRepository.saveAndFlush(libro);
    }

    @Transactional
    public void eliminar(Long id) {
        Libro libro;
        try {
            libro = libroRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> ApiException.noEncontrado("Libro no encontrado"));
        } catch (PessimisticLockingFailureException | LockTimeoutException ex) {
            throw ApiException.conflicto("Libro ocupado", "libro");
        }
        if (!Boolean.TRUE.equals(libro.getActivo())) {
            throw ApiException.conflicto("El libro ya esta inactivo", "activo");
        }
        // Comparte el orden de bloqueo por ejemplar con altas y devoluciones de prestamos.
        for (Long idEjemplar : libroRepository.findEjemplarIdsByLibroId(id)) {
            try {
                ejemplarRepository.findByIdForUpdate(idEjemplar);
            } catch (PessimisticLockingFailureException | LockTimeoutException ex) {
                throw ApiException.conflicto("Ejemplar ocupado", "libro");
            }
            if (prestamoRepository.existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNull(idEjemplar)) {
                throw ApiException.conflicto("El libro tiene un prestamo abierto", "libro");
            }
        }
        libro.setActivo(false);
        libroRepository.saveAndFlush(libro);
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
