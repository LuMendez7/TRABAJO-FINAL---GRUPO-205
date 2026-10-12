package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}