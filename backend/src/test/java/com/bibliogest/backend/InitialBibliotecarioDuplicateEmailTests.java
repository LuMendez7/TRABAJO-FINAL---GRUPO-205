package com.bibliogest.backend;

import com.bibliogest.backend.config.InitialBibliotecarioRunner;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bibliogest_duplicate;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "bibliogest.bootstrap.enabled=false"
})
@ActiveProfiles("test")
class InitialBibliotecarioDuplicateEmailTests {

    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void emailRegistradoConOtrasMayusculasAbortaSinCrearBibliotecario() {
        Usuario lector = new Usuario();
        lector.setNombre("Lector");
        lector.setApellido("Existente");
        lector.setEmail("AdMiN@Ejemplo.com");
        lector.setPassword(passwordEncoder.encode("claveFicticia123"));
        lector.setRol("LECTOR");
        lector.setActivo(true);
        usuarios.saveAndFlush(lector);

        MockEnvironment environment = new MockEnvironment()
                .withProperty("BIBLIOGEST_BOOTSTRAP_EMAIL", " admin@ejemplo.com ")
                .withProperty("BIBLIOGEST_BOOTSTRAP_PASSWORD", "otraClaveFicticia123")
                .withProperty("BIBLIOGEST_BOOTSTRAP_NOMBRE", "Ana")
                .withProperty("BIBLIOGEST_BOOTSTRAP_APELLIDO", "Gomez");
        InitialBibliotecarioRunner runner =
                new InitialBibliotecarioRunner(usuarios, passwordEncoder, environment);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> runner.run(null));
        assertTrue(error.getMessage().contains("ya pertenece"));
        assertEquals(1, usuarios.count());
        assertEquals("LECTOR", usuarios.findById(lector.getIdUsuario()).orElseThrow().getRol());
        assertFalse(usuarios.existsByRol("BIBLIOTECARIO"));
    }
}
