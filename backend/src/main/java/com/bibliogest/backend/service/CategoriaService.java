package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final LibroRepository libroRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, LibroRepository libroRepository) {
        this.categoriaRepository = categoriaRepository;
        this.libroRepository = libroRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    public Categoria crear(Categoria categoria) {
        if (categoria == null) {
            throw ApiException.validacion("Faltan datos de la categoria", null);
        }
        String nombre = ValidacionEntrada.obligatorio(categoria.getNombre(), "nombre", 100);
        String descripcion = ValidacionEntrada.opcional(categoria.getDescripcion(), "descripcion", 255);
        Categoria nueva = new Categoria();
        nueva.setNombre(nombre);
        nueva.setDescripcion(descripcion);
        return categoriaRepository.save(nueva);
    }

    public Categoria actualizar(Long id, Categoria datos) {

        Categoria categoria = buscarPorId(id);

        if (datos == null) {
            throw ApiException.validacion("Faltan datos de la categoria", null);
        }
        String nombre = ValidacionEntrada.obligatorio(datos.getNombre(), "nombre", 100);
        String descripcion = ValidacionEntrada.opcional(datos.getDescripcion(), "descripcion", 255);
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {

        Categoria categoria = buscarPorId(id);
        if (libroRepository.existsByCategoria_IdCategoria(id)) {
            throw ApiException.conflicto("La categoria tiene libros asociados", null);
        }
        categoriaRepository.delete(categoria);
        categoriaRepository.flush();
    }
}
