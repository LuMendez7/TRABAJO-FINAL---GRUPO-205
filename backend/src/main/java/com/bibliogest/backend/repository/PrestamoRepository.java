
package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuario_Email(String email);

    boolean existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNull(Long idEjemplar);

    boolean existsByEjemplar_IdEjemplarAndFechaDevolucionRealIsNullAndIdPrestamoNot(
            Long idEjemplar, Long idPrestamo);

    @Query("select p.ejemplar.idEjemplar from Prestamo p where p.idPrestamo = :id")
    Optional<Long> findEjemplarIdByPrestamoId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prestamo p where p.idPrestamo = :id")
    Optional<Prestamo> findByIdForUpdate(@Param("id") Long id);

}
