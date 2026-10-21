package com.bibliogest.backend;

import com.bibliogest.backend.config.InitialBibliotecarioRunner;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InitialBibliotecarioValidationTests {

    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final MockEnvironment environment = new MockEnvironment();
    private final InitialBibliotecarioRunner runner =
            new InitialBibliotecarioRunner(usuarios, new BCryptPasswordEncoder(), environment);

    @Test
    void bibliotecarioExistenteNoRequiereVariablesNiModificaCuentas() throws Exception {
        when(usuarios.existsByRol("BIBLIOTECARIO")).thenReturn(true);
        runner.run(null);
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void correoEnUsoAbortaSinSobrescribir() {
        variablesValidas();
        when(usuarios.existsByEmailIgnoreCase("admin@ejemplo.com")).thenReturn(true);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> runner.run(null));
        assertTrue(error.getMessage().contains("ya pertenece"));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void variablesFaltantesAbortanSinMostrarSecretos() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> runner.run(null));
        assertTrue(error.getMessage().contains("BIBLIOGEST_BOOTSTRAP_EMAIL"));
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void passwordCortaAbortaSinCrearUsuario() {
        variablesValidas();
        environment.setProperty("BIBLIOGEST_BOOTSTRAP_PASSWORD", "corta12");
        rechazaSinGuardar("BIBLIOGEST_BOOTSTRAP_PASSWORD");
    }

    @Test
    void emailInvalidoAbortaSinCrearUsuario() {
        variablesValidas();
        environment.setProperty("BIBLIOGEST_BOOTSTRAP_EMAIL", "correo-invalido");
        rechazaSinGuardar("BIBLIOGEST_BOOTSTRAP_EMAIL");
    }

    @Test
    void nombreVacioOSoloEspaciosAbortaSinCrearUsuario() {
        for (String nombre : new String[] {"", "   "}) {
            variablesValidas();
            environment.setProperty("BIBLIOGEST_BOOTSTRAP_NOMBRE", nombre);
            rechazaSinGuardar("BIBLIOGEST_BOOTSTRAP_NOMBRE");
        }
    }

    @Test
    void apellidoVacioOSoloEspaciosAbortaSinCrearUsuario() {
        for (String apellido : new String[] {"", "   "}) {
            variablesValidas();
            environment.setProperty("BIBLIOGEST_BOOTSTRAP_APELLIDO", apellido);
            rechazaSinGuardar("BIBLIOGEST_BOOTSTRAP_APELLIDO");
        }
    }

    private void rechazaSinGuardar(String variable) {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> runner.run(null));
        assertTrue(error.getMessage().contains(variable));
        verify(usuarios, never()).saveAndFlush(any());
    }

    private void variablesValidas() {
        environment.setProperty("BIBLIOGEST_BOOTSTRAP_EMAIL", " Admin@Ejemplo.com ");
        environment.setProperty("BIBLIOGEST_BOOTSTRAP_PASSWORD", "claveDePrueba123");
        environment.setProperty("BIBLIOGEST_BOOTSTRAP_NOMBRE", "Ana");
        environment.setProperty("BIBLIOGEST_BOOTSTRAP_APELLIDO", "Gomez");
    }
}
