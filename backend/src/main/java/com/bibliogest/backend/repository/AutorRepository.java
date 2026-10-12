package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}