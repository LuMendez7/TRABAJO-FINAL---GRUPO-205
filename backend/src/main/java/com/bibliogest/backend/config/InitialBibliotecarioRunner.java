package com.bibliogest.backend.config;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "bibliogest.bootstrap.enabled", havingValue = "true")
public class InitialBibliotecarioRunner implements ApplicationRunner {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public InitialBibliotecarioRunner(UsuarioRepository usuarios,
                                      PasswordEncoder passwordEncoder,
                                      Environment environment) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.existsByRol("BIBLIOTECARIO")) {
            return;
        }

        String email = required("BIBLIOGEST_BOOTSTRAP_EMAIL")
                .toLowerCase(Locale.ROOT);
        String password = requiredPassword();
        String nombre = required("BIBLIOGEST_BOOTSTRAP_NOMBRE");
        String apellido = required("BIBLIOGEST_BOOTSTRAP_APELLIDO");

        if (email.length() > 150 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalStateException("BIBLIOGEST_BOOTSTRAP_EMAIL invalido");
        }
        if (nombre.length() > 100 || apellido.length() > 100) {
            throw new IllegalStateException("Nombre o apellido de bootstrap excede 100 caracteres");
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("El correo de bootstrap ya pertenece a otra cuenta");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setRol("BIBLIOTECARIO");
        usuario.setActivo(true);
        usuarios.saveAndFlush(usuario);
    }

    private String required(String key) {
        String value = environment.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta la variable " + key);
        }
        return value.trim();
    }

    private String requiredPassword() {
        String password = environment.getProperty("BIBLIOGEST_BOOTSTRAP_PASSWORD");
        if (password == null || password.length() < 8) {
            throw new IllegalStateException("BIBLIOGEST_BOOTSTRAP_PASSWORD debe tener al menos 8 caracteres");
        }
        return password;
    }
}
