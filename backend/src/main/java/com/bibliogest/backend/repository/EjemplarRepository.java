package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Ejemplar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

    List<Ejemplar> findByActivoTrue();
}