package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Override
    @Query("SELECT DISTINCT l FROM Libro l JOIN FETCH l.categoria LEFT JOIN FETCH l.autores")
    List<Libro> findAll();

    @Query("SELECT DISTINCT l FROM Libro l JOIN FETCH l.categoria LEFT JOIN FETCH l.autores WHERE l.activo = true")
    List<Libro> findByActivoTrue();
}
