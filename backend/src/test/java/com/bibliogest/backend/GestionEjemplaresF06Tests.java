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
import com.bibliogest.backend.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:bibliogest_f06;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class GestionEjemplaresF06Tests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private CategoriaRepository categorias;
    @Autowired private LibroRepository libros;
    @Autowired private EjemplarRepository ejemplares;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private PrestamoService prestamoService;

    private MockMvc mvc;
    private Libro libro;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria " + UUID.randomUUID());
        categoria = categorias.saveAndFlush(categoria);
        libro = new Libro();
        libro.setTitulo("Libro " + UUID.randomUUID());
        libro.setCategoria(categoria);
        libro = libros.saveAndFlush(libro);
    }

    @Test
    void altaFisicaNormalizaYServidorAsignaCirculacion() throws Exception {
        String codigo = UUID.randomUUID().toString();
        mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                        .content(json(codigo, " Estante A ", "BUENO", ",\"estado\":\"PRESTADO\",\"activo\":false")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ubicacion").value("Estante A"))
                .andExpect(jsonPath("$.estadoFisico").value("BUENO"))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$.activo").value(true));
        Ejemplar guardado = porCodigo(codigo);
        assertEquals("Estante A", guardado.getUbicacion());
        assertEquals("BUENO", guardado.getEstadoFisico());
        assertEquals("DISPONIBLE", guardado.getEstado());
        assertTrue(guardado.getActivo());
    }

    @Test
    void rechazaUbicacionYEstadoFisicoInvalidosSinGuardar() throws Exception {
        long antes = ejemplares.count();
        for (String ubicacion : new String[] {"", "   ", "X".repeat(121)}) {
            mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                            .content(json(UUID.randomUUID().toString(), ubicacion, "BUENO", "")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("ubicacion"));
        }
        for (String estado : new String[] {"", "ROTO", "DANADO "}) {
            mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                            .content(json(UUID.randomUUID().toString(), "Estante", estado, "")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campo").value("estadoFisico"));
        }
        mvc.perform(post("/api/ejemplares").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libro\":{\"idLibro\":" + libro.getIdLibro()
                                + "},\"codigoInventario\":\"SIN-CAMPOS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("ubicacion"));
        assertEquals(antes, ejemplares.count());
    }

    @Test
    void edicionValidaPersisteFisicoSinAlterarCirculacionYDuplicadoDa409() throws Exception {
        Ejemplar primero = guardarDirecto("A", "BUENO");
        Ejemplar segundo = guardarDirecto("B", "REGULAR");
        String nuevoCodigo = UUID.randomUUID().toString();
        mvc.perform(put("/api/ejemplares/{id}", primero.getIdEjemplar())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(nuevoCodigo, " Sala 2 ", "DANADO", "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacion").value("Sala 2"))
                .andExpect(jsonPath("$.estadoFisico").value("DANADO"));
        Ejemplar guardado = ejemplares.findById(primero.getIdEjemplar()).orElseThrow();
        assertEquals(nuevoCodigo, guardado.getCodigoInventario());
        assertEquals("DISPONIBLE", guardado.getEstado());
        assertTrue(guardado.getActivo());

        mvc.perform(put("/api/ejemplares/{id}", primero.getIdEjemplar())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(segundo.getCodigoInventario(), "Otra sala", "BUENO", "")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campo").value("codigoInventario"));
        assertEquals(nuevoCodigo, ejemplares.findById(primero.getIdEjemplar()).orElseThrow().getCodigoInventario());
    }

    @Test
    void edicionRechazaDatosFisicosInvalidosYCambiosDeCirculacion() throws Exception {
        Ejemplar ejemplar = guardarDirecto("Estante", "BUENO");
        long id = ejemplar.getIdEjemplar();
        mvc.perform(put("/api/ejemplares/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("COD-NUEVO", "  ", "REGULAR", "")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campo").value("ubicacion"));
        mvc.perform(put("/api/ejemplares/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("COD-NUEVO", "Sala", "MALO", "")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campo").value("estadoFisico"));
        mvc.perform(put("/api/ejemplares/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("COD-NUEVO", "Sala", "BUENO", ",\"estado\":\"PRESTADO\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/ejemplares/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("COD-NUEVO", "Sala", "BUENO", ",\"activo\":false")))
                .andExpect(status().isBadRequest());
        assertEquals(ejemplar.getCodigoInventario(), ejemplares.findById(id).orElseThrow().getCodigoInventario());
    }

    @Test
    void prestamoAbiertoBloqueaEdicionYBajaSinPerderDatos() throws Exception {
        Ejemplar ejemplar = guardarDirecto("Sala 1", "REGULAR");
        Prestamo prestamo = prestamoService.crear(solicitud(usuario(), ejemplar));
        mvc.perform(put("/api/ejemplares/{id}", ejemplar.getIdEjemplar())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("CAMBIO", "Sala 2", "DANADO", "")))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/ejemplares/{id}/desactivar", ejemplar.getIdEjemplar()))
                .andExpect(status().isConflict());
        Ejemplar guardado = ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow();
        assertEquals("Sala 1", guardado.getUbicacion());
        assertEquals("REGULAR", guardado.getEstadoFisico());
        assertTrue(guardado.getActivo());
        assertEquals("PRESTADO", guardado.getEstado());
        assertTrue(prestamos.existsById(prestamo.getIdPrestamo()));
    }

    @Test
    void bajaLogicaConservaPrestamoHistoricoYNoPermiteNuevoPrestamo() throws Exception {
        Ejemplar ejemplar = guardarDirecto("Sala histórica", "BUENO");
        Usuario usuario = usuario();
        Prestamo prestamo = prestamoService.crear(solicitud(usuario, ejemplar));
        prestamoService.devolver(prestamo.getIdPrestamo());
        mvc.perform(put("/api/ejemplares/{id}/desactivar", ejemplar.getIdEjemplar()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        Ejemplar guardado = ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow();
        assertEquals("Sala histórica", guardado.getUbicacion());
        assertEquals("BUENO", guardado.getEstadoFisico());
        assertTrue(prestamos.existsById(prestamo.getIdPrestamo()));
        assertEquals(ejemplar.getIdEjemplar(), prestamos.findById(prestamo.getIdPrestamo())
                .orElseThrow().getEjemplar().getIdEjemplar());
        mvc.perform(get("/api/ejemplares/activos"))
                .andExpect(jsonPath("$[?(@.idEjemplar == " + ejemplar.getIdEjemplar() + ")]").isEmpty());
        mvc.perform(get("/api/ejemplares/{id}", ejemplar.getIdEjemplar()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ubicacion").value("Sala histórica"));
        assertEquals(409, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> prestamoService.crear(solicitud(usuario, ejemplar))).getStatusCode().value());
        mvc.perform(put("/api/ejemplares/{id}/desactivar", ejemplar.getIdEjemplar()))
                .andExpect(status().isConflict());
    }

    @Test
    void legadoNuloSeConsultaSinInventarValoresYEdicionExigeDatos() throws Exception {
        Ejemplar legado = guardarDirecto(null, null);
        mvc.perform(get("/api/ejemplares/{id}", legado.getIdEjemplar()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacion").doesNotExist())
                .andExpect(jsonPath("$.estadoFisico").doesNotExist());
        mvc.perform(put("/api/ejemplares/{id}", legado.getIdEjemplar())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(legado.getCodigoInventario(), null, null, "")))
                .andExpect(status().isBadRequest());
        assertNull(ejemplares.findById(legado.getIdEjemplar()).orElseThrow().getUbicacion());
        mvc.perform(put("/api/ejemplares/{id}", legado.getIdEjemplar())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(legado.getCodigoInventario(), "Sala nueva", "REGULAR", "")))
                .andExpect(status().isOk());
        assertEquals("Sala nueva", ejemplares.findById(legado.getIdEjemplar()).orElseThrow().getUbicacion());
    }

    @Test
    void activosPublicosOmitenEjemplarDeLibroInactivo() throws Exception {
        Ejemplar ejemplar = guardarDirecto("Estante", "BUENO");
        libro.setActivo(false);
        libros.saveAndFlush(libro);
        mvc.perform(get("/api/ejemplares/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idEjemplar == " + ejemplar.getIdEjemplar() + ")]").isEmpty());
        mvc.perform(get("/api/ejemplares/{id}", ejemplar.getIdEjemplar()))
                .andExpect(status().isOk());
    }

    @Test
    void migracionAgregaColumnasSinAlterarFilas() throws Exception {
        String url = "jdbc:h2:mem:migracion_f06_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
        try (var conexion = DriverManager.getConnection(url);
             var sentencia = conexion.createStatement()) {
            sentencia.execute("CREATE TABLE ejemplar (id_ejemplar BIGINT PRIMARY KEY, codigo_inventario VARCHAR(50))");
            sentencia.execute("INSERT INTO ejemplar VALUES (1, 'A'), (2, 'B')");
            long antes;
            try (var filas = sentencia.executeQuery("SELECT COUNT(*) FROM ejemplar")) {
                filas.next();
                antes = filas.getLong(1);
            }
            String sql = Files.readString(Path.of("../database/migrations/001_ejemplar_datos_fisicos.sql"));
            for (String paso : sql.split(";")) {
                if (!paso.isBlank()) sentencia.execute(paso);
            }
            try (var filas = sentencia.executeQuery(
                    "SELECT COUNT(*), COUNT(ubicacion), COUNT(estado_fisico) FROM ejemplar")) {
                filas.next();
                assertEquals(antes, filas.getLong(1));
                assertEquals(0, filas.getLong(2));
                assertEquals(0, filas.getLong(3));
            }
        }
    }

    private String json(String codigo, String ubicacion, String estadoFisico, String extras) {
        return "{\"libro\":{\"idLibro\":" + libro.getIdLibro()
                + "},\"codigoInventario\":\"" + codigo + "\",\"ubicacion\":"
                + (ubicacion == null ? "null" : "\"" + ubicacion + "\"")
                + ",\"estadoFisico\":"
                + (estadoFisico == null ? "null" : "\"" + estadoFisico + "\"") + extras + "}";
    }

    private Ejemplar guardarDirecto(String ubicacion, String estadoFisico) {
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario(UUID.randomUUID().toString());
        ejemplar.setUbicacion(ubicacion);
        ejemplar.setEstadoFisico(estadoFisico);
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
        Prestamo datos = new Prestamo();
        datos.setUsuario(usuario);
        datos.setEjemplar(ejemplar);
        datos.setFechaDevolucionPrevista(LocalDate.now(
                ZoneId.of("America/Argentina/Buenos_Aires")).plusDays(7));
        return datos;
    }

    private Ejemplar porCodigo(String codigo) {
        return ejemplares.findAll().stream()
                .filter(ejemplar -> codigo.equals(ejemplar.getCodigoInventario()))
                .findFirst().orElseThrow();
    }
}
