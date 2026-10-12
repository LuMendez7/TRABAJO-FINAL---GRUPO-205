package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario crear(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {

        Usuario usuario = buscarPorId(id);

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setEmail(datos.getEmail());
        usuario.setRol(datos.getRol());

        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(datos.getPassword());
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desactivar(Long id) {

        Usuario usuario = buscarPorId(id);

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya se encuentra inactivo");
        }

        usuario.setActivo(false);

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }
}