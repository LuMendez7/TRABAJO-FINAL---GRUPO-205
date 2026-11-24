package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Reserva;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository
        extends JpaRepository<Reserva, Long> {

    List<Reserva> findAllByOrderByFechaSolicitudDesc();

    List<Reserva>
        findByUsuario_EmailOrderByFechaSolicitudDesc(String email);

    List<Reserva>
        findByEstadoOrderByFechaSolicitudAsc(String estado);

    long countByUsuario_IdUsuarioAndEstadoIn(
        Long idUsuario,
        Collection<String> estados
    );

    boolean existsByUsuario_IdUsuarioAndLibro_IdLibroAndEstadoIn(
        Long idUsuario,
        Long idLibro,
        Collection<String> estados
    );

    boolean existsByEjemplar_IdEjemplarAndEstado(
        Long idEjemplar,
        String estado
    );

    List<Reserva> findByEstadoAndFechaVencimientoLessThanEqual(
        String estado,
        LocalDateTime fecha
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reserva r WHERE r.idReserva = :id")
    Optional<Reserva> buscarConBloqueo(@Param("id") Long id);
}