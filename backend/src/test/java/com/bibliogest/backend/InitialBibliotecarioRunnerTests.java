package com.bibliogest.backend;

import com.bibliogest.backend.config.InitialBibliotecarioRunner;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bibliogest_bootstrap;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "bibliogest.bootstrap.enabled=true",
        "BIBLIOGEST_BOOTSTRAP_EMAIL= Admin@Ejemplo.com ",
        "BIBLIOGEST_BOOTSTRAP_PASSWORD=claveDePrueba123",
        "BIBLIOGEST_BOOTSTRAP_NOMBRE= Ana ",
        "BIBLIOGEST_BOOTSTRAP_APELLIDO= Gomez "
})
@ActiveProfiles("test")
class InitialBibliotecarioRunnerTests {

    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private InitialBibliotecarioRunner runner;

    @Test
    void creaUnaSolaCuentaConBcryptYDatosNormalizados() throws Exception {
        Usuario usuario = usuarios.findByEmail("admin@ejemplo.com").orElseThrow();
        assertEquals(1, usuarios.count());
        assertEquals("BIBLIOTECARIO", usuario.getRol());
        assertTrue(usuario.getActivo());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("Gomez", usuario.getApellido());
        assertNotEquals("claveDePrueba123", usuario.getPassword());
        assertTrue(passwordEncoder.matches("claveDePrueba123", usuario.getPassword()));

        String hashOriginal = usuario.getPassword();
        runner.run(null);
        assertEquals(1, usuarios.count());
        assertEquals(hashOriginal, usuarios.findByEmail("admin@ejemplo.com")
                .orElseThrow().getPassword());
    }
}
