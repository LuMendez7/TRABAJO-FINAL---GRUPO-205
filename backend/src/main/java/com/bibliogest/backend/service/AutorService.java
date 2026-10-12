package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.repository.AutorRepository;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Autor no encontrado"));
    }

    public Autor crear(Autor autor) {
        return autorRepository.save(autor);
    }
}