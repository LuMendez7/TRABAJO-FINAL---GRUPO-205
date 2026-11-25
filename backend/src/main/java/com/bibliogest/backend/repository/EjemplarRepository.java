package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Ejemplar;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EjemplarRepository
        extends JpaRepository<Ejemplar, Long> {

    @Override
    @Query("SELECT DISTINCT e FROM Ejemplar e JOIN FETCH e.libro l JOIN FETCH l.categoria LEFT JOIN FETCH l.autores")
    List<Ejemplar> findAll();

    @Query("SELECT DISTINCT e FROM Ejemplar e JOIN FETCH e.libro l JOIN FETCH l.categoria LEFT JOIN FETCH l.autores WHERE e.activo = true")
    List<Ejemplar> findByActivoTrue();

    List<Ejemplar>
        findByLibro_IdLibroAndActivoTrueAndEstadoOrderByIdEjemplarAsc(
            Long idLibro,
            String estado
        );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Ejemplar e WHERE e.idEjemplar = :id")
    Optional<Ejemplar> buscarConBloqueo(@Param("id") Long id);
}
