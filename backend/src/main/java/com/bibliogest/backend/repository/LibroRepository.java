package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    List<Libro> findByActivoTrue();

    boolean existsByCategoria_IdCategoria(Long idCategoria);

    boolean existsByAutores_IdAutor(Long idAutor);
}
