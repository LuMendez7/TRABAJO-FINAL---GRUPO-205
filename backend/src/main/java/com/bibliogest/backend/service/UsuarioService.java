
package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
    }

    @Transactional
    public Usuario crear(Usuario usuario) {

        validarEmail(usuario.getEmail(), null);
        validarPassword(usuario.getPassword());

        // Los usuarios registrados desde el sistema
        // siempre se crean con el rol LECTOR.
        usuario.setIdUsuario(null);
        usuario.setRol("LECTOR");
        usuario.setActivo(true);

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {

        Usuario usuario = buscarPorId(id);

        validarEmail(datos.getEmail(), id);

        usuario.setNombre(datos.getNombre());
        usuario.setApellido(datos.getApellido());
        usuario.setEmail(datos.getEmail().trim());

        // No modificamos el rol ni el estado activo.
        // Esos campos no deben cambiarse mediante
        // una actualización general del usuario.

        if (datos.getPassword() != null &&
                !datos.getPassword().isBlank()) {

            validarPassword(datos.getPassword());

            usuario.setPassword(
                    passwordEncoder.encode(datos.getPassword())
            );
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

        // Protegemos la cuenta administrativa.
        if ("BIBLIOTECARIO".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se puede desactivar una cuenta bibliotecaria desde esta operación");
        }

        usuario.setActivo(false);

        return usuarioRepository.save(usuario);
    }

    private void validarPassword(String password) {

        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres");
        }
    }

    private void validarEmail(String email, Long idActual) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico es obligatorio");
        }

        String emailNormalizado = email.trim();

        boolean duplicado = usuarioRepository.findAll()
                .stream()
                .anyMatch(usuario ->
                        usuario.getEmail() != null &&
                                usuario.getEmail().equalsIgnoreCase(emailNormalizado) &&
                                (idActual == null ||
                                        !usuario.getIdUsuario().equals(idActual))
                );

        if (duplicado) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese correo electrónico");
        }
    }
}
