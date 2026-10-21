package com.bibliogest.backend;

import com.bibliogest.backend.entity.Autor;
import com.bibliogest.backend.entity.Categoria;
import com.bibliogest.backend.entity.Libro;
import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.exception.ApiExceptionHandler;
import com.bibliogest.backend.repository.AutorRepository;
import com.bibliogest.backend.repository.CategoriaRepository;
import com.bibliogest.backend.repository.EjemplarRepository;
import com.bibliogest.backend.repository.LibroRepository;
import com.bibliogest.backend.repository.UsuarioRepository;
import com.bibliogest.backend.security.BibliogestUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:bibliogest_f03;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class ValidacionesRestF03Tests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private AutorRepository autores;
    @Autowired private CategoriaRepository categorias;
    @Autowired private LibroRepository libros;
    @Autowired private EjemplarRepository ejemplares;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private BibliogestUserDetailsService userDetails;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void limitesNombreAutorYApellido() throws Exception {
        for (String valor : new String[] {"", "   ", "x".repeat(101)}) {
            mvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
                            .content(autorJson(valor, "Apellido")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                    .andExpect(jsonPath("$.campo").value("nombre"));
        }
        for (String valor : new String[] {"", "y".repeat(101)}) {
            mvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
                            .content(autorJson("Nombre", valor)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("apellido"));
        }
        for (String valor : new String[] {"x", "x".repeat(100)}) {
            mvc.perform(post("/api/autores").contentType(MediaType.APPLICATION_JSON)
                            .content(autorJson(valor, "Apellido")))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void limitesCategoriaYDescripcionNormalizada() throws Exception {
        for (String valor : new String[] {"", "   ", "x".repeat(101)}) {
            mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                            .content(categoriaJson(valor, " ")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                    .andExpect(jsonPath("$.campo").value("nombre"));
        }
        for (String valor : new String[] {"x", "x".repeat(100)}) {
            mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                            .content(categoriaJson(valor, "   ")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.descripcion").doesNotExist());
        }
        mvc.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content(categoriaJson("Valida", "d".repeat(256))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("descripcion"));
    }

    @Test
    void libroRechazaTituloVacioYTextosExcesivosSinGuardar() throws Exception {
        Categoria categoria = categoria();
        Autor autor = autor();
        long antes = libros.count();
        for (String cuerpo : new String[] {
                libroJson("   ", "isbn", "editorial", categoria.getIdCategoria(), autor.getIdAutor()),
                libroJson("Titulo", "i".repeat(21), "editorial", categoria.getIdCategoria(), autor.getIdAutor()),
                libroJson("Titulo", "isbn", "e".repeat(151), categoria.getIdCategoria(), autor.getIdAutor())
        }) {
            mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"));
        }
        assertEquals(antes, libros.count());
    }

    @Test
    void libroNormalizaTextosYEliminaAutoresRepetidos() throws Exception {
        Categoria categoria = categoria();
        Autor autor = autor();
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\" Titulo \",\"isbn\":\"   \",\"editorial\":\" Editora \","
                                + "\"categoria\":{\"idCategoria\":" + categoria.getIdCategoria() + "},"
                                + "\"autores\":[{\"idAutor\":" + autor.getIdAutor() + "},"
                                + "{\"idAutor\":" + autor.getIdAutor() + "}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Titulo"))
                .andExpect(jsonPath("$.editorial").value("Editora"))
                .andExpect(jsonPath("$.autores.length()").value(1));
        Libro guardado = libros.findAll().stream()
                .filter(libro -> "Titulo".equals(libro.getTitulo())).findFirst().orElseThrow();
        assertNull(guardado.getIsbn());
    }

    @Test
    void referenciasDeLibroInvalidasNoCreanRegistro() throws Exception {
        Categoria categoria = categoria();
        Autor autor = autor();
        long antes = libros.count();
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Libro\",\"autores\":[{\"idAutor\":"
                                + autor.getIdAutor() + "}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("categoria"));
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson("Libro", null, null, Long.MAX_VALUE, autor.getIdAutor())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content(libroJson("Libro", null, null, categoria.getIdCategoria(), Long.MAX_VALUE)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Libro\",\"categoria\":{\"idCategoria\":"
                                + categoria.getIdCategoria() + "},\"autores\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("autores"));
        assertEquals(antes, libros.count());
    }

    @Test
    void emailSeNormalizaYDuplicadoDa409() throws Exception {
        String email = UUID.randomUUID() + "@ejemplo.test";
        String cuerpo = usuarioJson(" Ana ", " Perez ", " " + email.toUpperCase() + " ", "claveFicticia123");
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());
        assertEquals("Ana", usuarios.findByEmail(email).orElseThrow().getNombre());
        assertEquals(email, userDetails.loadUserByUsername(" " + email.toUpperCase() + " ").getUsername());
        long antes = usuarios.count();
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("Otra", "Persona", email, "claveFicticia123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"))
                .andExpect(jsonPath("$.campo").value("email"));
        assertEquals(antes, usuarios.count());
    }

    @Test
    void violacionDelIndiceUniqueSeTraduceA409() {
        String email = UUID.randomUUID() + "@ejemplo.test";
        Usuario primero = usuarioPersistible(email);
        usuarios.saveAndFlush(primero);
        long antes = usuarios.count();

        DataIntegrityViolationException error = assertThrows(DataIntegrityViolationException.class,
                () -> usuarios.saveAndFlush(usuarioPersistible(email)));
        var respuesta = new ApiExceptionHandler().integridad(error);
        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals("CONFLICTO", respuesta.getBody().get("codigo"));
        assertFalse(respuesta.getBody().get("mensaje").contains(email));
        assertEquals(antes, usuarios.count());
    }

    @Test
    void passwordCortaYMayorDe72BytesSeRechazan() throws Exception {
        long antes = usuarios.count();
        for (String password : new String[] {"corta12", "a".repeat(73), "á".repeat(37)}) {
            mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                            .content(usuarioJson("Ana", "Perez", UUID.randomUUID() + "@ejemplo.test", password)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("password"));
        }
        assertEquals(antes, usuarios.count());
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("Ana", "Perez", UUID.randomUUID() + "@ejemplo.test",
                                "a".repeat(72))))
                .andExpect(status().isCreated());
    }

    @Test
    void usuarioRechazaNombresYEmailsInvalidosSinGuardar() throws Exception {
        long antes = usuarios.count();
        for (String nombre : new String[] {"", "   ", "x".repeat(101)}) {
            mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                            .content(usuarioJson(nombre, "Perez", UUID.randomUUID() + "@ejemplo.test",
                                    "claveFicticia123")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("nombre"));
        }
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("Ana", "   ", UUID.randomUUID() + "@ejemplo.test",
                                "claveFicticia123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("apellido"));
        for (String email : new String[] {"sin-arroba", "   ", "a".repeat(145) + "@e.test"}) {
            mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                            .content(usuarioJson("Ana", "Perez", email, "claveFicticia123")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("email"));
        }
        assertEquals(antes, usuarios.count());
    }

    @Test
    void anioPublicacionFueraDeRangoSeRechaza() throws Exception {
        Categoria categoria = categoria();
        Autor autor = autor();
        String base = libroJson("Libro", null, null, categoria.getIdCategoria(), autor.getIdAutor());
        long antes = libros.count();
        for (int anio : new int[] {1449, java.time.LocalDate.now(
                java.time.ZoneId.of("America/Argentina/Buenos_Aires")).getYear() + 1}) {
            String cuerpo = base.substring(0, base.length() - 1) + ",\"anioPublicacion\":" + anio + "}";
            mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("anioPublicacion"));
        }
        assertEquals(antes, libros.count());
    }

    @Test
    void inventarioRechazaCodigoVacioYExcesivoSinGuardar() throws Exception {
        Libro libro = libro(categoria(), autor());
        long antes = ejemplares.count();
        for (String codigo : new String[] {"   ", "x".repeat(51)}) {
            mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                            .content(ejemplarJson(libro.getIdLibro(), codigo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("codigoInventario"));
        }
        assertEquals(antes, ejemplares.count());
    }

    @Test
    void idDeRutaPrevaleceEnEdicion() throws Exception {
        Autor destino = autor();
        Autor otro = autor();
        mvc.perform(put("/api/autores/" + destino.getIdAutor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idAutor\":" + otro.getIdAutor()
                                + ",\"nombre\":\" Nombre nuevo \",\"apellido\":\" Apellido nuevo \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAutor").value(destino.getIdAutor()))
                .andExpect(jsonPath("$.nombre").value("Nombre nuevo"));
        assertEquals("Autor", autores.findById(otro.getIdAutor()).orElseThrow().getNombre());
    }

    @Test
    void usuarioIgnoraRolActivoEIdDelBodyYEdicionVaciaConservaHash() throws Exception {
        String email = UUID.randomUUID() + "@ejemplo.test";
        mvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuario\":9999,\"nombre\":\"Ana\",\"apellido\":\"Perez\","
                                + "\"email\":\"" + email + "\",\"password\":\"claveFicticia123\","
                                + "\"rol\":\"BIBLIOTECARIO\",\"activo\":false}"))
                .andExpect(status().isCreated());
        var guardado = usuarios.findByEmail(email).orElseThrow();
        assertEquals("LECTOR", guardado.getRol());
        assertTrue(guardado.getActivo());
        assertNotEquals(9999, guardado.getIdUsuario());
        String hash = guardado.getPassword();
        mvc.perform(put("/api/usuarios/" + guardado.getIdUsuario())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idUsuario\":9999,\"nombre\":\" Ana \",\"apellido\":\" Perez \","
                                + "\"email\":\" " + email.toUpperCase() + " \",\"password\":\"   \","
                                + "\"rol\":\"BIBLIOTECARIO\",\"activo\":false}"))
                .andExpect(status().isOk());
        var actualizado = usuarios.findById(guardado.getIdUsuario()).orElseThrow();
        assertEquals(hash, actualizado.getPassword());
        assertEquals("LECTOR", actualizado.getRol());
        assertTrue(actualizado.getActivo());
    }

    @Test
    void rutasPorIdInexistenteDevuelven404() throws Exception {
        for (String ruta : new String[] {"/api/autores/", "/api/categorias/", "/api/libros/",
                "/api/ejemplares/", "/api/usuarios/"}) {
            mvc.perform(get(ruta + Long.MAX_VALUE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
        }
    }

    @Test
    void autorYCategoriaRelacionadosNoSeBorran() throws Exception {
        Categoria categoria = categoria();
        Autor autor = autor();
        Libro libro = libro(categoria, autor);
        mvc.perform(delete("/api/autores/" + autor.getIdAutor()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
        mvc.perform(delete("/api/categorias/" + categoria.getIdCategoria()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
        assertTrue(autores.existsById(autor.getIdAutor()));
        assertTrue(categorias.existsById(categoria.getIdCategoria()));
        assertTrue(libros.existsById(libro.getIdLibro()));
    }

    @Test
    void autorYCategoriaSinReferenciasSeBorran() throws Exception {
        Autor autor = autor();
        Categoria categoria = categoria();
        mvc.perform(delete("/api/autores/" + autor.getIdAutor()))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/categorias/" + categoria.getIdCategoria()))
                .andExpect(status().isNoContent());
        assertFalse(autores.existsById(autor.getIdAutor()));
        assertFalse(categorias.existsById(categoria.getIdCategoria()));
    }

    @Test
    void inventarioDuplicadoYLibroInexistenteSeRechazan() throws Exception {
        Libro libro = libro(categoria(), autor());
        String codigo = UUID.randomUUID().toString();
        mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                        .content(ejemplarJson(libro.getIdLibro(), codigo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
        long antes = ejemplares.count();
        mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                        .content(ejemplarJson(libro.getIdLibro(), codigo)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campo").value("codigoInventario"));
        mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                        .content(ejemplarJson(Long.MAX_VALUE, UUID.randomUUID().toString())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("NO_ENCONTRADO"));
        assertEquals(antes, ejemplares.count());
    }

    @Test
    void jsonMalformadoDa400SinExponerDetalles() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{json invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    private Autor autor() {
        Autor autor = new Autor();
        autor.setNombre("Autor");
        autor.setApellido("Prueba");
        return autores.saveAndFlush(autor);
    }

    private Categoria categoria() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria " + UUID.randomUUID());
        return categorias.saveAndFlush(categoria);
    }

    private Libro libro(Categoria categoria, Autor autor) {
        Libro libro = new Libro();
        libro.setTitulo("Libro de prueba");
        libro.setCategoria(categoria);
        libro.setAutores(Set.of(autor));
        return libros.saveAndFlush(libro);
    }

    private String autorJson(String nombre, String apellido) {
        return "{\"nombre\":\"" + nombre + "\",\"apellido\":\"" + apellido + "\"}";
    }

    private String categoriaJson(String nombre, String descripcion) {
        return "{\"nombre\":\"" + nombre + "\",\"descripcion\":\"" + descripcion + "\"}";
    }

    private String libroJson(String titulo, String isbn, String editorial, long categoria, long autor) {
        return "{\"titulo\":\"" + titulo + "\",\"isbn\":" + texto(isbn)
                + ",\"editorial\":" + texto(editorial) + ",\"categoria\":{\"idCategoria\":"
                + categoria + "},\"autores\":[{\"idAutor\":" + autor + "}]}";
    }

    private String ejemplarJson(long libro, String codigo) {
        return "{\"libro\":{\"idLibro\":" + libro + "},\"codigoInventario\":\"" + codigo
                + "\",\"ubicacion\":\"Estante A\",\"estadoFisico\":\"BUENO\"}";
    }

    private String usuarioJson(String nombre, String apellido, String email, String password) {
        return "{\"nombre\":\"" + nombre + "\",\"apellido\":\"" + apellido
                + "\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private String texto(String valor) {
        return valor == null ? "null" : "\"" + valor + "\"";
    }

    private Usuario usuarioPersistible(String email) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Prueba");
        usuario.setApellido("Unicidad");
        usuario.setEmail(email);
        usuario.setPassword("hash-ficticio");
        usuario.setRol("LECTOR");
        usuario.setActivo(true);
        return usuario;
    }
}
