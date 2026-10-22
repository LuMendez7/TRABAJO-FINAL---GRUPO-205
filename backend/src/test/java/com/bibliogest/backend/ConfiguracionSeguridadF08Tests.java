package com.bibliogest.backend;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bibliogest_f08;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "BIBLIOGEST_CORS_ALLOWED_ORIGINS=https://bibliogest.ejemplo.test,http://localhost:5174"
})
@ActiveProfiles("test")
class ConfiguracionSeguridadF08Tests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy filtrosSeguridad;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PasswordEncoder passwordEncoder;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtrosSeguridad).build();
    }

    @Test
    void preflightAceptaSoloOrigenesConfigurados() throws Exception {
        mvc.perform(preflight("https://bibliogest.ejemplo.test"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://bibliogest.ejemplo.test"));
        mvc.perform(preflight("http://localhost:5174"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5174"));
        mvc.perform(preflight("http://localhost:5173"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        mvc.perform(preflight("https://desconocido.ejemplo.test"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void conservaCatalogoPublicoYPermisosAdministrativos() throws Exception {
        Usuario lector = usuario("LECTOR");
        Usuario bibliotecario = usuario("BIBLIOTECARIO");

        mvc.perform(get("/api/libros/activos"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/libros").header("Authorization", basic(lector)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/libros").header("Authorization", basic(bibliotecario)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/prestamos/mis-prestamos").header("Authorization", basic(lector)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder preflight(String origin) {
        return options("/api/libros")
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type");
    }

    private Usuario usuario(String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Prueba");
        usuario.setApellido("F08");
        usuario.setEmail(UUID.randomUUID() + "@example.test");
        usuario.setPassword(passwordEncoder.encode("Clave-Ficticia-F08"));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarios.saveAndFlush(usuario);
    }

    private String basic(Usuario usuario) {
        String valor = usuario.getEmail() + ":Clave-Ficticia-F08";
        return "Basic " + Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }
}
