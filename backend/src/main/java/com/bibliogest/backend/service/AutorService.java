package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AutorService {

    private final AutorRepository autorRepository;
    private final LibroRepository libroRepository;

    public AutorService(AutorRepository autorRepository, LibroRepository libroRepository) {
        this.autorRepository = autorRepository;
        this.libroRepository = libroRepository;
    }

    public List<Autor> listar() {
        return autorRepository.findAll();
    }

    public Autor buscarPorId(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Autor no encontrado"));
    }

    public Autor crear(Autor autor) {
        if (autor == null) {
            throw ApiException.validacion("Faltan datos del autor", null);
        }
        String nombre = ValidacionEntrada.obligatorio(autor.getNombre(), "nombre", 100);
        String apellido = ValidacionEntrada.obligatorio(autor.getApellido(), "apellido", 100);
        Autor nuevo = new Autor();
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        return autorRepository.save(nuevo);
    }

    public Autor actualizar(Long id, Autor datos) {

        Autor autor = buscarPorId(id);

        if (datos == null) {
            throw ApiException.validacion("Faltan datos del autor", null);
        }
        String nombre = ValidacionEntrada.obligatorio(datos.getNombre(), "nombre", 100);
        String apellido = ValidacionEntrada.obligatorio(datos.getApellido(), "apellido", 100);
        autor.setNombre(nombre);
        autor.setApellido(apellido);

        return autorRepository.save(autor);
    }

    @Transactional
    public void eliminar(Long id) {

        Autor autor = buscarPorId(id);
        if (libroRepository.existsByAutores_IdAutor(id)) {
            throw ApiException.conflicto("El autor tiene libros asociados", null);
        }
        autorRepository.delete(autor);
        autorRepository.flush();
    }
}
