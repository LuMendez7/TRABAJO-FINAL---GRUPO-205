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
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:bibliogest_f07;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class PrestamosVencidosF07Tests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy filtrosSeguridad;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private CategoriaRepository categorias;
    @Autowired private LibroRepository libros;
    @Autowired private EjemplarRepository ejemplares;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private PasswordEncoder passwordEncoder;

    private Usuario lector;
    private Usuario otroLector;
    private Usuario bibliotecario;
    private Prestamo vencido;
    private Prestamo venceHoy;
    private Prestamo venceFuturo;
    private Prestamo devueltoTarde;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtrosSeguridad).build();
        String sufijo = UUID.randomUUID().toString();
        lector = usuario("lector-" + sufijo, "LECTOR");
        otroLector = usuario("otro-" + sufijo, "LECTOR");
        bibliotecario = usuario("bibliotecario-" + sufijo, "BIBLIOTECARIO");

        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria F07 " + sufijo);
        categoria = categorias.saveAndFlush(categoria);
        Libro libro = new Libro();
        libro.setTitulo("Libro F07 " + sufijo);
        libro.setCategoria(categoria);
        libro = libros.saveAndFlush(libro);

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        vencido = prestamo(lector, ejemplar(libro, "VENCIDO-" + sufijo), hoy.minusDays(1), null);
        venceHoy = prestamo(lector, ejemplar(libro, "HOY-" + sufijo), hoy, null);
        venceFuturo = prestamo(otroLector, ejemplar(libro, "FUTURO-" + sufijo), hoy.plusDays(1), null);
        devueltoTarde = prestamo(lector, ejemplar(libro, "DEVUELTO-" + sufijo), hoy.minusDays(1), hoy);
    }

    @Test
    void calculaIndicadorEstrictoYLoIncluyeEnConsultasSinAlterarDatos() throws Exception {
        long cantidad = prestamos.count();
        LocalDate vencimientoGuardado = vencido.getFechaDevolucionPrevista();
        mvc.perform(get("/api/prestamos").header("Authorization", basic(bibliotecario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idPrestamo == " + vencido.getIdPrestamo() + ")].vencido").value(true))
                .andExpect(jsonPath("$[?(@.idPrestamo == " + venceHoy.getIdPrestamo() + ")].vencido").value(false))
                .andExpect(jsonPath("$[?(@.idPrestamo == " + venceFuturo.getIdPrestamo() + ")].vencido").value(false))
                .andExpect(jsonPath("$[?(@.idPrestamo == " + devueltoTarde.getIdPrestamo() + ")].vencido").value(false));
        assertEquals(cantidad, prestamos.count());
        assertNull(prestamos.findById(vencido.getIdPrestamo()).orElseThrow().getFechaDevolucionReal());
        assertEquals(vencimientoGuardado,
                prestamos.findById(vencido.getIdPrestamo()).orElseThrow().getFechaDevolucionPrevista());
    }

    @Test
    void lectorSoloConsultaSusPrestamosYBibliotecarioConservaPermisoDeListado() throws Exception {
        mvc.perform(get("/api/prestamos/mis-prestamos").header("Authorization", basic(lector)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idPrestamo == " + vencido.getIdPrestamo() + ")].vencido").value(true))
                .andExpect(jsonPath("$[?(@.idPrestamo == " + venceFuturo.getIdPrestamo() + ")]").isEmpty());
        mvc.perform(get("/api/prestamos").header("Authorization", basic(lector)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/prestamos").header("Authorization", basic(bibliotecario)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/prestamos/mis-prestamos"))
                .andExpect(status().isUnauthorized());
    }

    private Usuario usuario(String identificador, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario");
        usuario.setApellido(identificador);
        usuario.setEmail(identificador + "@example.test");
        usuario.setPassword(passwordEncoder.encode("Prueba-F07-123"));
        usuario.setRol(rol);
        usuario.setActivo(true);
        return usuarios.saveAndFlush(usuario);
    }

    private Ejemplar ejemplar(Libro libro, String codigo) {
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario(codigo);
        ejemplar.setEstado("PRESTADO");
        ejemplar.setActivo(true);
        return ejemplares.saveAndFlush(ejemplar);
    }

    private Prestamo prestamo(Usuario usuario, Ejemplar ejemplar, LocalDate prevista, LocalDate real) {
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setEjemplar(ejemplar);
        prestamo.setFechaPrestamo(prevista.minusDays(7));
        prestamo.setFechaDevolucionPrevista(prevista);
        prestamo.setFechaDevolucionReal(real);
        prestamo.setEstado(real == null ? "ACTIVO" : "DEVUELTO");
        return prestamos.saveAndFlush(prestamo);
    }

    private String basic(Usuario usuario) {
        String valor = usuario.getEmail() + ":Prueba-F07-123";
        return "Basic " + Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }
}
