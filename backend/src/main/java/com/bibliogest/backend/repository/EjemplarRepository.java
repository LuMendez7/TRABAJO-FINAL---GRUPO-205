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