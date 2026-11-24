package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Notificacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    List<Notificacion>
        findByIdUsuarioOrderByFechaCreacionDescIdNotificacionDesc(
            Long idUsuario);

    long countByIdUsuarioAndLeidaFalse(Long idUsuario);

    Optional<Notificacion>
        findByIdNotificacionAndIdUsuario(
            Long idNotificacion, Long idUsuario);

    boolean existsByIdUsuarioAndClaveEvento(
        Long idUsuario, String claveEvento);

    @Modifying
    @Query("""
        UPDATE Notificacion n
        SET n.leida = true,
            n.fechaLectura = :fecha
        WHERE n.idUsuario = :idUsuario
          AND n.leida = false
        """)
    int marcarTodasComoLeidas(
        @Param("idUsuario") Long idUsuario,
        @Param("fecha") LocalDateTime fecha);
}