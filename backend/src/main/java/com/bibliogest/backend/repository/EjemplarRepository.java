package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Ejemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface EjemplarRepository extends JpaRepository<Ejemplar, Long> {

    List<Ejemplar> findByActivoTrue();

    List<Ejemplar> findByActivoTrueAndLibro_ActivoTrue();

    boolean existsByCodigoInventario(String codigoInventario);

    boolean existsByCodigoInventarioAndIdEjemplarNot(String codigoInventario, Long idEjemplar);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Ejemplar e where e.idEjemplar = :id")
    Optional<Ejemplar> findByIdForUpdate(@Param("id") Long id);
}
