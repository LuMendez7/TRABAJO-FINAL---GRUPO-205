package com.bibliogest.backend.service;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

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

    public List<Usuario> listarPendientes() {
        return usuarioRepository.findByEstadoCuenta("PENDIENTE");
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));
    }

    // Alta directa realizada por el bibliotecario.
    @Transactional
    public Usuario crear(Usuario usuario) {
        return guardarNuevoUsuario(usuario, true, "APROBADO");
    }

    // Registro público: requiere aprobación.
    @Transactional
    public Usuario registrar(Usuario usuario) {
        return guardarNuevoUsuario(usuario, false, "PENDIENTE");
    }

    private Usuario guardarNuevoUsuario(
            Usuario datos,
            boolean activo,
            String estadoCuenta) {

        validarDatosPersonales(datos);
        validarEmail(datos.getEmail(), null);
        validarPassword(datos.getPassword());

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setEmail(normalizarEmail(datos.getEmail()));

        usuario.setRol("LECTOR");
        usuario.setActivo(activo);
        usuario.setEstadoCuenta(estadoCuenta);

        usuario.setPassword(
                passwordEncoder.encode(datos.getPassword())
        );

        return usuarioRepository.save(usuario);
    }

    // Aprobar una solicitud pendiente.
    @Transactional
    public Usuario aprobar(Long id) {

        Usuario usuario = buscarPorId(id);

        validarSolicitudPendiente(usuario);

        usuario.setEstadoCuenta("APROBADO");
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    // Rechazar una solicitud pendiente.
    @Transactional
    public Usuario rechazar(Long id) {

        Usuario usuario = buscarPorId(id);

        validarSolicitudPendiente(usuario);

        usuario.setEstadoCuenta("RECHAZADO");
        usuario.setActivo(false);

        return usuarioRepository.save(usuario);
    }

    private void validarSolicitudPendiente(Usuario usuario) {

        if (!"LECTOR".equals(usuario.getRol()) ||
                !"PENDIENTE".equals(usuario.getEstadoCuenta())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario no tiene una solicitud pendiente");
        }
    }

    // Actualización de datos de un usuario existente.
    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {

        Usuario usuario = buscarPorId(id);

        validarDatosPersonales(datos);
        validarEmail(datos.getEmail(), id);

        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellido(datos.getApellido().trim());
        usuario.setEmail(normalizarEmail(datos.getEmail()));

        // No se modifica el rol ni el estado de la cuenta
        // mediante una actualización general.

        if (datos.getPassword() != null &&
                !datos.getPassword().isBlank()) {

            validarPassword(datos.getPassword());

            usuario.setPassword(
                    passwordEncoder.encode(datos.getPassword())
            );
        }

        return usuarioRepository.save(usuario);
    }

    // Baja lógica del usuario.
    @Transactional
    public Usuario desactivar(Long id) {

        Usuario usuario = buscarPorId(id);

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya se encuentra inactivo");
        }

        if ("BIBLIOTECARIO".equals(usuario.getRol())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No se puede desactivar una cuenta bibliotecaria desde esta operación");
        }

        usuario.setActivo(false);
        usuario.setEstadoCuenta("INACTIVO");

        return usuarioRepository.save(usuario);
    }

    private void validarDatosPersonales(Usuario usuario) {

        if (usuario.getNombre() == null ||
                usuario.getNombre().isBlank() ||
                usuario.getNombre().trim().length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio y admite hasta 100 caracteres");
        }

        if (usuario.getApellido() == null ||
                usuario.getApellido().isBlank() ||
                usuario.getApellido().trim().length() > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El apellido es obligatorio y admite hasta 100 caracteres");
        }
    }

    private void validarPassword(String password) {

        if (password == null ||
                password.isBlank() ||
                password.length() < 8) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 8 caracteres");
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña supera la longitud máxima permitida");
        }
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarEmail(String email, Long idActual) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico es obligatorio");
        }

        String emailNormalizado = normalizarEmail(email);

        if (emailNormalizado.length() > 150 ||
                !emailNormalizado.matches(
                        "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo electrónico no es válido");
        }

        boolean duplicado = usuarioRepository.findAll()
                .stream()
                .anyMatch(usuario ->
                        usuario.getEmail() != null &&
                                usuario.getEmail()
                                        .equalsIgnoreCase(emailNormalizado) &&
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