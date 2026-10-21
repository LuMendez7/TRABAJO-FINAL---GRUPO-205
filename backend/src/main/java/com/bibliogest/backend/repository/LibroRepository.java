package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Libro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    List<Libro> findByActivoTrue();

    boolean existsByCategoria_IdCategoria(Long idCategoria);

    boolean existsByAutores_IdAutor(Long idAutor);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Libro l where l.idLibro = :idLibro")
    Optional<Libro> findByIdForUpdate(@Param("idLibro") Long idLibro);

    @Query("select e.idEjemplar from Ejemplar e where e.libro.idLibro = :idLibro order by e.idEjemplar")
    List<Long> findEjemplarIdsByLibroId(@Param("idLibro") Long idLibro);
}
