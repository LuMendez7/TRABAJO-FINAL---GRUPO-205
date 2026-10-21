package com.bibliogest.backend;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Ejemplar;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.entity.Prestamo;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.repository.PrestamoRepository;
import com.bibliogest.backend.repository.UsuarioRepository;
import com.bibliogest.backend.service.EjemplarService;
import com.bibliogest.backend.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:bibliogest_f05;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class GestionLibrosF05Tests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private AutorRepository autores;
    @Autowired private CategoriaRepository categorias;
    @Autowired private LibroRepository libros;
    @Autowired private EjemplarRepository ejemplares;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private EjemplarService ejemplarService;
    @Autowired private PrestamoService prestamoService;
    @Autowired private JdbcTemplate jdbc;

    private MockMvc mvc;
    private Categoria categoria;
    private Autor primero;
    private Autor segundo;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        String sufijo = UUID.randomUUID().toString();
        categoria = new Categoria();
        categoria.setNombre("Categoria " + sufijo);
        categoria = categorias.saveAndFlush(categoria);
        primero = autor("Primero " + sufijo);
        segundo = autor("Segundo " + sufijo);
    }

    @Test
    void creaConDosAutoresYEditorialOpcional() throws Exception {
        long id = crear("Nuevo", " Editorial libre ", primero.getIdAutor(), segundo.getIdAutor());
        assertEquals(Set.of(primero.getIdAutor(), segundo.getIdAutor()), autoresDe(id));
        assertEquals("Editorial libre", libros.findById(id).orElseThrow().getEditorial());
        assertTrue(libros.findById(id).orElseThrow().getActivo());

        long sinEditorial = crear("Sin editorial", "   ", primero.getIdAutor());
        assertNull(libros.findById(sinEditorial).orElseThrow().getEditorial());
    }

    @Test
    void editaSinPerderAutoresYActualizaSinDuplicados() throws Exception {
        long id = crear("Inicial", null, primero.getIdAutor(), segundo.getIdAutor());
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(" Editado ", " Nueva editorial ", primero.getIdAutor(),
                                segundo.getIdAutor(), segundo.getIdAutor())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Editado"))
                .andExpect(jsonPath("$.autores.length()").value(2));
        assertEquals(Set.of(primero.getIdAutor(), segundo.getIdAutor()), autoresDe(id));
        assertEquals("Nueva editorial", libros.findById(id).orElseThrow().getEditorial());

        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Solo primero", null, primero.getIdAutor())))
                .andExpect(status().isOk());
        assertEquals(Set.of(primero.getIdAutor()), autoresDe(id));
    }

    @Test
    void rechazaCamposInvalidosYSinAutoresSinCambios() throws Exception {
        long id = crear("Conservado", null, primero.getIdAutor());
        for (String cuerpo : List.of(
                cuerpo("   ", null, primero.getIdAutor()),
                cuerpo("Título", null),
                cuerpo("Título", "x".repeat(151), primero.getIdAutor()),
                "{\"titulo\":\"Título\",\"categoria\":{\"idCategoria\":"
                        + categoria.getIdCategoria() + "},\"autores\":[{\"idAutor\":0}]}",
                "{\"titulo\":\"Título\",\"categoria\":{\"idCategoria\":"
                        + categoria.getIdCategoria() + "},\"autores\":[{\"idAutor\":"
                        + Long.MAX_VALUE + "}]}"
        )) {
            mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(result -> assertTrue(result.getResponse().getStatus() == 400
                            || result.getResponse().getStatus() == 404));
        }
        assertEquals("Conservado", libros.findById(id).orElseThrow().getTitulo());
        assertEquals(Set.of(primero.getIdAutor()), autoresDe(id));
    }

    @Test
    void activoOmitidoVerdaderoFalsoYNuloEnPostYPut() throws Exception {
        String base = cuerpo("Presencia", null, primero.getIdAutor());
        long id = crear("Predeterminado", null, primero.getIdAutor());
        assertTrue(libros.findById(id).orElseThrow().getActivo());

        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(conActivo(base, "true")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.activoEnviado").doesNotExist());
        for (String valor : List.of("false", "null")) {
            mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                            .content(conActivo(base, valor)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("activo"));
        }

        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON).content(base))
                .andExpect(status().isOk());
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(conActivo(base, "true")))
                .andExpect(status().isOk());
        for (String valor : List.of("false", "null")) {
            mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                            .content(conActivo(base, valor)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("activo"));
        }
        assertTrue(libros.findById(id).orElseThrow().getActivo());

        mvc.perform(delete("/api/libros/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON).content(base))
                .andExpect(status().isOk());
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(conActivo(base, "false")))
                .andExpect(status().isOk());
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(conActivo(base, "true")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/libros/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(conActivo(base, "null")))
                .andExpect(status().isBadRequest());
        assertFalse(libros.findById(id).orElseThrow().getActivo());
    }

    @Test
    void bajaLogicaExcluyeCatalogoPeroConservaReferencias() throws Exception {
        long id = crear("Historico", null, primero.getIdAutor());
        Ejemplar ejemplar = ejemplar(id);
        Usuario usuario = usuario();
        Prestamo prestamo = prestamoService.crear(solicitud(usuario, ejemplar));
        prestamoService.devolver(prestamo.getIdPrestamo());

        mvc.perform(delete("/api/libros/{id}", id)).andExpect(status().isNoContent());
        assertFalse(libros.findById(id).orElseThrow().getActivo());
        assertTrue(ejemplares.existsById(ejemplar.getIdEjemplar()));
        assertTrue(prestamos.existsById(prestamo.getIdPrestamo()));
        assertEquals(id, prestamos.findById(prestamo.getIdPrestamo()).orElseThrow()
                .getEjemplar().getLibro().getIdLibro());
        assertEquals(Set.of(primero.getIdAutor()), autoresDe(id));
        mvc.perform(get("/api/libros/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idLibro == " + id + ")]").isEmpty());
        mvc.perform(get("/api/libros/{id}", id)).andExpect(status().isOk());
        mvc.perform(delete("/api/libros/{id}", id)).andExpect(status().isConflict());
        mvc.perform(delete("/api/libros/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());

        Ejemplar nuevo = new Ejemplar();
        Libro referencia = new Libro();
        referencia.setIdLibro(id);
        nuevo.setLibro(referencia);
        nuevo.setCodigoInventario(UUID.randomUUID().toString());
        assertEquals(409, assertThrows(com.bibliogest.backend.exception.ApiException.class,
                () -> ejemplarService.crear(nuevo)).getStatus().value());
        ResponseStatusException rechazo = assertThrows(ResponseStatusException.class,
                () -> prestamoService.crear(solicitud(usuario, ejemplar)));
        assertEquals(409, rechazo.getStatusCode().value());
    }

    @Test
    void noDesactivaConPrestamoAbierto() throws Exception {
        long id = crear("En circulación", null, primero.getIdAutor());
        Ejemplar ejemplar = ejemplar(id);
        Prestamo prestamo = prestamoService.crear(solicitud(usuario(), ejemplar));
        mvc.perform(delete("/api/libros/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
        assertTrue(libros.findById(id).orElseThrow().getActivo());
        assertTrue(prestamos.existsById(prestamo.getIdPrestamo()));
        assertEquals("PRESTADO", ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow().getEstado());
    }

    private Autor autor(String nombre) {
        Autor autor = new Autor();
        autor.setNombre(nombre);
        autor.setApellido("Apellido");
        return autores.saveAndFlush(autor);
    }

    private long crear(String titulo, String editorial, Long... idsAutores) throws Exception {
        String json = cuerpo(titulo, editorial, idsAutores);
        long antes = libros.count();
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        return libros.findAll().stream().filter(libro -> titulo.equals(libro.getTitulo()))
                .filter(libro -> libro.getIdLibro() > antes).map(Libro::getIdLibro).findFirst().orElseThrow();
    }

    private String cuerpo(String titulo, String editorial, Long... idsAutores) {
        String relaciones = java.util.Arrays.stream(idsAutores)
                .map(id -> "{\"idAutor\":" + id + "}").reduce((a, b) -> a + "," + b).orElse("");
        return "{\"titulo\":\"" + titulo + "\",\"editorial\":\""
                + (editorial == null ? "" : editorial) + "\",\"categoria\":{\"idCategoria\":"
                + categoria.getIdCategoria() + "},\"autores\":[" + relaciones + "]}";
    }

    private String conActivo(String json, String valor) {
        return json.substring(0, json.length() - 1) + ",\"activo\":" + valor + "}";
    }

    private Set<Long> autoresDe(long id) {
        return Set.copyOf(jdbc.queryForList("select id_autor from libro_autor where id_libro = ?",
                Long.class, id));
    }

    private Ejemplar ejemplar(long idLibro) {
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libros.findById(idLibro).orElseThrow());
        ejemplar.setCodigoInventario(UUID.randomUUID().toString());
        ejemplar.setEstado("DISPONIBLE");
        ejemplar.setActivo(true);
        return ejemplares.saveAndFlush(ejemplar);
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Lector");
        usuario.setApellido("Prueba");
        usuario.setEmail(UUID.randomUUID() + "@ejemplo.test");
        usuario.setPassword("hash-ficticio");
        usuario.setRol("LECTOR");
        usuario.setActivo(true);
        return usuarios.saveAndFlush(usuario);
    }

    private Prestamo solicitud(Usuario usuario, Ejemplar ejemplar) {
        Prestamo solicitud = new Prestamo();
        solicitud.setUsuario(usuario);
        solicitud.setEjemplar(ejemplar);
        solicitud.setFechaDevolucionPrevista(LocalDate.now(
                ZoneId.of("America/Argentina/Buenos_Aires")).plusDays(7));
        return solicitud;
    }
}
