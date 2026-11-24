package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.repository.AutorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
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
        return autorRepository.save(autor);
    }

    public Autor actualizar(Long id, Autor datos) {

        Autor autor = buscarPorId(id);

        autor.setNombre(datos.getNombre());
        autor.setApellido(datos.getApellido());

        return autorRepository.save(autor);
    }

    public void eliminar(Long id) {

        Autor autor = buscarPorId(id);

        autorRepository.delete(autor);
    }
}