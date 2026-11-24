package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Notificacion;
import com.bibliogest.backend.repository.NotificacionRepository;
import com.bibliogest.backend.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    private final NotificacionRepository notificaciones;
    private final UsuarioRepository usuarios;

    public NotificacionService(
            NotificacionRepository notificaciones,
            UsuarioRepository usuarios) {

        this.notificaciones = notificaciones;
        this.usuarios = usuarios;
    }

    private Long obtenerIdUsuario(String email) {

        return usuarios.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado"))
            .getIdUsuario();
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listar(String email) {

        return notificaciones
            .findByIdUsuarioOrderByFechaCreacionDescIdNotificacionDesc(
                obtenerIdUsuario(email));
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(String email) {

        return notificaciones.countByIdUsuarioAndLeidaFalse(
            obtenerIdUsuario(email));
    }

    @Transactional
    public Notificacion marcarComoLeida(
            Long id, String email) {

        Long idUsuario = obtenerIdUsuario(email);

        Notificacion notificacion = notificaciones
            .findByIdNotificacionAndIdUsuario(id, idUsuario)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Notificacion no encontrada"));

        if (!Boolean.TRUE.equals(notificacion.getLeida())) {

            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
        }

        return notificaciones.save(notificacion);
    }

    @Transactional
    public int marcarTodasComoLeidas(String email) {

        return notificaciones.marcarTodasComoLeidas(
            obtenerIdUsuario(email),
            LocalDateTime.now());
    }

    @Transactional
    public void generar(
            Long idUsuario,
            String titulo,
            String mensaje,
            String tipo,
            String entidad,
            Long idReferencia,
            String claveEvento) {

        if (claveEvento != null &&
            notificaciones.existsByIdUsuarioAndClaveEvento(
                idUsuario, claveEvento)) {

            return;
        }

        Notificacion notificacion = new Notificacion();

        notificacion.setIdUsuario(idUsuario);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setEntidadReferencia(entidad);
        notificacion.setIdReferencia(idReferencia);
        notificacion.setClaveEvento(claveEvento);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());

        notificaciones.save(notificacion);
    }
}