package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.exception.ApiException;
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

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
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
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @Transactional
    public Usuario crear(Usuario datos) {
        if (datos == null) {
            throw ApiException.validacion("Faltan datos del usuario", null);
        }
        String nombre = ValidacionEntrada.obligatorio(datos.getNombre(), "nombre", 100);
        String apellido = ValidacionEntrada.obligatorio(datos.getApellido(), "apellido", 100);
        String email = ValidacionEntrada.email(datos.getEmail());
        ValidacionEntrada.password(datos.getPassword());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflicto("Ya existe un usuario con ese email", "email");
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setEmail(email);
        nuevo.setPassword(passwordEncoder.encode(datos.getPassword()));
        nuevo.setRol("LECTOR");
        nuevo.setActivo(true);
        return usuarioRepository.saveAndFlush(nuevo);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario usuario = buscarPorId(id);
        if (datos == null) {
            throw ApiException.validacion("Faltan datos del usuario", null);
        }
        String nombre = ValidacionEntrada.obligatorio(datos.getNombre(), "nombre", 100);
        String apellido = ValidacionEntrada.obligatorio(datos.getApellido(), "apellido", 100);
        String email = ValidacionEntrada.email(datos.getEmail());
        String password = datos.getPassword();
        if (password != null && !password.isBlank()) {
            ValidacionEntrada.password(password);
        }
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(email, id)) {
            throw ApiException.conflicto("Ya existe un usuario con ese email", "email");
        }

        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        if (password != null && !password.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(password));
        }
        return usuarioRepository.saveAndFlush(usuario);
    }

    @Transactional
    public Usuario desactivar(Long id) {
        Usuario usuario = buscarPorId(id);
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El usuario ya se encuentra inactivo");
        }
        if ("BIBLIOTECARIO".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se puede desactivar una cuenta bibliotecaria desde esta operacion");
        }
        usuario.setActivo(false);
        return usuarioRepository.save(usuario);
    }
}