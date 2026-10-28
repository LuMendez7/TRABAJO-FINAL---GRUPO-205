package com.bibliogest.backend;

import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.entity.Prestamo;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.repository.PrestamoRepository;
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
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:bibliogest_f09;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class FlujosIntegracionF09Tests {

    private static final String PASSWORD = "Clave-Ficticia-F09";

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy filtrosSeguridad;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CategoriaRepository categorias;
    @Autowired private LibroRepository libros;
    @Autowired private EjemplarRepository ejemplares;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private PasswordEncoder passwordEncoder;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtrosSeguridad).build();
    }

    @Test
    void visitanteNoPuedeCrearLibros() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void fechaPrevistaMalformadaNoCreaPrestamoNiCambiaEjemplar() throws Exception {
        Usuario bibliotecario = nuevoUsuario("BIBLIOTECARIO", UUID.randomUUID() + "@ejemplo.test");
        Usuario lector = nuevoUsuario("LECTOR", UUID.randomUUID() + "@ejemplo.test");
        Categoria categoria = new Categoria();
        categoria.setNombre("F09 fecha " + UUID.randomUUID());
        categorias.saveAndFlush(categoria);

        Libro libro = new Libro();
        libro.setTitulo("Libro fecha F09 " + UUID.randomUUID());
        libro.setCategoria(categoria);
        libros.saveAndFlush(libro);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario("F09-FECHA-" + UUID.randomUUID());
        ejemplar.setEstado("DISPONIBLE");
        ejemplares.saveAndFlush(ejemplar);

        String cuerpo = "{\"usuario\":{\"idUsuario\":" + lector.getIdUsuario()
                + "},\"ejemplar\":{\"idEjemplar\":" + ejemplar.getIdEjemplar()
                + "},\"fechaDevolucionPrevista\":\"no-es-fecha\"}";
        mvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", basic(bibliotecario.getEmail(), PASSWORD))
                        .content(cuerpo))
                .andExpect(status().isBadRequest());

        assertTrue(prestamos.findAll().stream()
                .noneMatch(prestamo -> ejemplar.getIdEjemplar().equals(
                        prestamo.getEjemplar().getIdEjemplar())));
        assertEquals("DISPONIBLE", ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow().getEstado());
    }

    @Test
    void altaNormalizaCorreoPermiteLoginYBajaConservaPrestamoHistorico() throws Exception {
        Usuario bibliotecario = nuevoUsuario("BIBLIOTECARIO", UUID.randomUUID() + "@ejemplo.test");
        String email = UUID.randomUUID() + "@ejemplo.test";
        String correoMayusculasConEspacios = " " + email.toUpperCase() + " ";
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", basic(bibliotecario.getEmail(), PASSWORD))
                        .content("{\"nombre\":\"Ana\",\"apellido\":\"Ejemplo\",\"email\":\""
                                + correoMayusculasConEspacios + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());

        Usuario lector = usuarios.findByEmail(email).orElseThrow();
        assertEquals(email, lector.getEmail());
        mvc.perform(get("/api/auth/me").header("Authorization", basic(correoMayusculasConEspacios, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("LECTOR"));

        long usuariosAntesDelDuplicado = usuarios.count();
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", basic(bibliotecario.getEmail(), PASSWORD))
                        .content("{\"nombre\":\"Otra\",\"apellido\":\"Persona\",\"email\":\""
                                + correoMayusculasConEspacios + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isConflict());
        assertEquals(usuariosAntesDelDuplicado, usuarios.count());

        Categoria categoria = new Categoria();
        categoria.setNombre("F09 " + UUID.randomUUID());
        categorias.saveAndFlush(categoria);

        Libro libro = new Libro();
        libro.setTitulo("Libro F09 " + UUID.randomUUID());
        libro.setCategoria(categoria);
        libros.saveAndFlush(libro);

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario("F09-" + UUID.randomUUID());
        ejemplar.setEstado("PRESTADO");
        ejemplares.saveAndFlush(ejemplar);

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(lector);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucionPrevista(LocalDate.now().plusDays(7));
        prestamo.setEstado("ACTIVO");
        Prestamo historico = prestamos.saveAndFlush(prestamo);

        mvc.perform(put("/api/usuarios/" + lector.getIdUsuario() + "/desactivar")
                        .header("Authorization", basic(bibliotecario.getEmail(), PASSWORD)))
                .andExpect(status().isOk());

        mvc.perform(get("/api/auth/me").header("Authorization", basic(email, PASSWORD)))
                .andExpect(status().isUnauthorized());

        Usuario lectorInactivo = usuarios.findById(lector.getIdUsuario()).orElseThrow();
        assertFalse(lectorInactivo.getActivo());
        Prestamo conservado = prestamos.findById(historico.getIdPrestamo()).orElseThrow();
        assertEquals(lector.getIdUsuario(), conservado.getUsuario().getIdUsuario());
        assertEquals(ejemplar.getIdEjemplar(), conservado.getEjemplar().getIdEjemplar());
        assertEquals("ACTIVO", conservado.getEstado());
        assertEquals(1, prestamos.findByUsuario_Email(email).size());
    }

    private Usuario nuevoUsuario(String rol, String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario");
        usuario.setApellido("F09");
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(PASSWORD));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarios.saveAndFlush(usuario);
    }

    private String basic(String email, String password) {
        String valor = email + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }
}
