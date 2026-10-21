package com.bibliogest.backend;

import tools.jackson.databind.ObjectMapper;
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
import com.bibliogest.backend.service.EjemplarService;
import com.bibliogest.backend.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:bibliogest_f02;MODE=MySQL;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class CirculacionF02Tests {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");

    @Autowired private CategoriaRepository categorias;
    @Autowired private LibroRepository libros;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private EjemplarRepository ejemplares;
    @Autowired private PrestamoRepository prestamos;
    @Autowired private PrestamoService prestamoService;
    @Autowired private EjemplarService ejemplarService;

    private Usuario usuario;
    private Libro libro;
    private Ejemplar ejemplar;

    @BeforeEach
    void preparar() {
        String sufijo = UUID.randomUUID().toString();
        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria " + sufijo);
        categoria = categorias.saveAndFlush(categoria);

        libro = new Libro();
        libro.setTitulo("Libro de prueba");
        libro.setCategoria(categoria);
        libro.setActivo(true);
        libro = libros.saveAndFlush(libro);

        usuario = new Usuario();
        usuario.setNombre("Lector");
        usuario.setApellido("De prueba");
        usuario.setEmail(sufijo + "@ejemplo.test");
        usuario.setPassword("hash-ficticio");
        usuario.setRol("LECTOR");
        usuario.setActivo(true);
        usuario = usuarios.saveAndFlush(usuario);

        ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setCodigoInventario(sufijo);
        ejemplar.setEstado("DISPONIBLE");
        ejemplar.setActivo(true);
        ejemplar = ejemplares.saveAndFlush(ejemplar);
    }

    @Test
    void prestamoValidoIgnoraCamposDeCirculacionDelCliente() {
        Prestamo datos = solicitud(LocalDate.now(ZONA));
        datos.setEstado("DEVUELTO");
        datos.setFechaPrestamo(LocalDate.of(2000, 1, 1));
        datos.setFechaDevolucionReal(LocalDate.of(2000, 1, 2));

        Prestamo creado = prestamoService.crear(datos);
        Prestamo guardado = prestamos.findById(creado.getIdPrestamo()).orElseThrow();
        assertEquals("ACTIVO", guardado.getEstado());
        assertEquals(LocalDate.now(ZONA), guardado.getFechaPrestamo());
        assertNull(guardado.getFechaDevolucionReal());
        assertEquals("PRESTADO", estadoEjemplar());
    }

    @Test
    void fechaAyerRechazadaSinCambios() {
        rechazaAlta(solicitud(LocalDate.now(ZONA).minusDays(1)), HttpStatus.BAD_REQUEST);
    }

    @Test
    void fechasHoyYMananaSonValidas() {
        LocalDate hoy = LocalDate.now(ZONA);
        Prestamo primero = prestamoService.crear(solicitud(hoy));
        assertEquals(hoy, primero.getFechaPrestamo());
        prestamoService.devolver(primero.getIdPrestamo());
        Prestamo segundo = prestamoService.crear(solicitud(hoy.plusDays(1)));
        assertEquals(hoy.plusDays(1), segundo.getFechaDevolucionPrevista());
    }

    @Test
    void camposNulosRechazadosSinGuardar() {
        rechazaAlta(null, HttpStatus.BAD_REQUEST);
        Prestamo sinUsuario = solicitud(LocalDate.now(ZONA));
        sinUsuario.setUsuario(null);
        rechazaAlta(sinUsuario, HttpStatus.BAD_REQUEST);
        Prestamo sinEjemplar = solicitud(LocalDate.now(ZONA));
        sinEjemplar.setEjemplar(null);
        rechazaAlta(sinEjemplar, HttpStatus.BAD_REQUEST);
        rechazaAlta(solicitud(null), HttpStatus.BAD_REQUEST);
        Prestamo sinIdUsuario = solicitud(LocalDate.now(ZONA));
        sinIdUsuario.getUsuario().setIdUsuario(null);
        rechazaAlta(sinIdUsuario, HttpStatus.BAD_REQUEST);
        Prestamo sinIdEjemplar = solicitud(LocalDate.now(ZONA));
        sinIdEjemplar.getEjemplar().setIdEjemplar(null);
        rechazaAlta(sinIdEjemplar, HttpStatus.BAD_REQUEST);
    }

    @Test
    void idsInexistentesDevuelven404() {
        Prestamo usuarioInexistente = solicitud(LocalDate.now(ZONA));
        usuarioInexistente.getUsuario().setIdUsuario(Long.MAX_VALUE);
        rechazaAlta(usuarioInexistente, HttpStatus.NOT_FOUND);
        Prestamo ejemplarInexistente = solicitud(LocalDate.now(ZONA));
        ejemplarInexistente.getEjemplar().setIdEjemplar(Long.MAX_VALUE);
        rechazaAlta(ejemplarInexistente, HttpStatus.NOT_FOUND);
    }

    @Test
    void entidadesInactivasNoPermitenPrestamo() {
        usuario.setActivo(false);
        usuarios.saveAndFlush(usuario);
        rechazaAlta(solicitud(LocalDate.now(ZONA)), HttpStatus.CONFLICT);
        usuario.setActivo(true);
        usuarios.saveAndFlush(usuario);

        libro.setActivo(false);
        libros.saveAndFlush(libro);
        rechazaAlta(solicitud(LocalDate.now(ZONA)), HttpStatus.CONFLICT);
        libro.setActivo(true);
        libros.saveAndFlush(libro);

        ejemplar.setActivo(false);
        ejemplares.saveAndFlush(ejemplar);
        rechazaAlta(solicitud(LocalDate.now(ZONA)), HttpStatus.CONFLICT);
    }

    @Test
    void prestamoDuplicadoSecuencialNoCambiaBase() {
        Prestamo primero = prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        long cantidad = prestamos.count();
        assertEstado(HttpStatus.CONFLICT,
                () -> prestamoService.crear(solicitud(LocalDate.now(ZONA))));
        assertEquals(cantidad, prestamos.count());
        assertNull(prestamos.findById(primero.getIdPrestamo()).orElseThrow().getFechaDevolucionReal());
        assertEquals("PRESTADO", estadoEjemplar());
    }

    @Test
    void prestamoAbiertoBloqueaAltaAunqueEstadoDigaDisponible() {
        prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        ejemplar = ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow();
        ejemplar.setEstado("DISPONIBLE");
        ejemplares.saveAndFlush(ejemplar);
        long cantidad = prestamos.count();
        assertEstado(HttpStatus.CONFLICT,
                () -> prestamoService.crear(solicitud(LocalDate.now(ZONA))));
        assertEquals(cantidad, prestamos.count());
    }

    @Test
    void dosAltasSimultaneasDejanUnSoloPrestamo() throws Exception {
        long idEjemplar = ejemplar.getIdEjemplar();
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> primera = hilos.submit(() -> intentarAlta(salida));
            Future<Integer> segunda = hilos.submit(() -> intentarAlta(salida));
            salida.countDown();
            int a = primera.get(15, TimeUnit.SECONDS);
            int b = segunda.get(15, TimeUnit.SECONDS);
            assertEquals(1, (a == 201 ? 1 : 0) + (b == 201 ? 1 : 0));
            assertEquals(1, (a == 409 ? 1 : 0) + (b == 409 ? 1 : 0));
            assertEquals(1, prestamos.findAll().stream()
                    .filter(p -> p.getEjemplar().getIdEjemplar() == idEjemplar
                            && p.getFechaDevolucionReal() == null).count());
            assertEquals("PRESTADO", estadoEjemplar());
        } finally {
            hilos.shutdownNow();
        }
    }

    @Test
    void devolverCierraPrestamoYLiberarEjemplar() {
        Prestamo creado = prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        Prestamo devuelto = prestamoService.devolver(creado.getIdPrestamo());
        assertEquals("DEVUELTO", devuelto.getEstado());
        assertEquals(LocalDate.now(ZONA), devuelto.getFechaDevolucionReal());
        assertEquals("DISPONIBLE", estadoEjemplar());
        assertEquals("DEVUELTO", prestamos.findById(creado.getIdPrestamo()).orElseThrow().getEstado());
    }

    @Test
    void dobleDevolucionNoAlteraHistorial() {
        Prestamo creado = prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        Prestamo devuelto = prestamoService.devolver(creado.getIdPrestamo());
        LocalDate fecha = devuelto.getFechaDevolucionReal();
        assertEstado(HttpStatus.CONFLICT, () -> prestamoService.devolver(creado.getIdPrestamo()));
        assertEquals(fecha, prestamos.findById(creado.getIdPrestamo())
                .orElseThrow().getFechaDevolucionReal());
        assertEquals("DISPONIBLE", estadoEjemplar());
    }

    @Test
    void dosDevolucionesSimultaneasSoloCierranUnaVez() throws Exception {
        Prestamo creado = prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> primera = hilos.submit(() -> intentarDevolucion(salida, creado.getIdPrestamo()));
            Future<Integer> segunda = hilos.submit(() -> intentarDevolucion(salida, creado.getIdPrestamo()));
            salida.countDown();
            int a = primera.get(15, TimeUnit.SECONDS);
            int b = segunda.get(15, TimeUnit.SECONDS);
            assertEquals(1, (a == 200 ? 1 : 0) + (b == 200 ? 1 : 0));
            assertEquals(1, (a == 409 ? 1 : 0) + (b == 409 ? 1 : 0));
            assertEquals("DEVUELTO", prestamos.findById(creado.getIdPrestamo()).orElseThrow().getEstado());
            assertEquals("DISPONIBLE", estadoEjemplar());
        } finally {
            hilos.shutdownNow();
        }
    }

    @Test
    void otroPrestamoAbiertoImpideLiberarEjemplar() {
        Prestamo primero = prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        Prestamo incoherente = solicitud(LocalDate.now(ZONA));
        incoherente.setUsuario(usuario);
        incoherente.setEjemplar(ejemplar);
        incoherente.setFechaPrestamo(LocalDate.now(ZONA));
        incoherente.setEstado("ACTIVO");
        prestamos.saveAndFlush(incoherente);

        assertEstado(HttpStatus.CONFLICT, () -> prestamoService.devolver(primero.getIdPrestamo()));
        assertNull(prestamos.findById(primero.getIdPrestamo())
                .orElseThrow().getFechaDevolucionReal());
        assertEquals("PRESTADO", estadoEjemplar());
    }

    @Test
    void edicionNoAceptaEstadoNiActivoForzados() {
        Ejemplar datos = datosEdicion();
        datos.setEstado("PRESTADO");
        assertEstado(HttpStatus.BAD_REQUEST,
                () -> ejemplarService.actualizar(ejemplar.getIdEjemplar(), datos));
        datos.setEstado("DISPONIBLE");
        datos.setActivo(false);
        assertEstado(HttpStatus.BAD_REQUEST,
                () -> ejemplarService.actualizar(ejemplar.getIdEjemplar(), datos));
        assertEquals("DISPONIBLE", estadoEjemplar());
        assertTrue(ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow().getActivo());
    }

    @Test
    void jsonDistingueCamposOmitidosDeCamposEnviados() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Ejemplar omitidos = mapper.readValue("{}", Ejemplar.class);
        assertFalse(omitidos.isEstadoEnviado());
        assertFalse(omitidos.isActivoEnviado());

        Ejemplar enviados = mapper.readValue("{\"estado\":null,\"activo\":false}", Ejemplar.class);
        assertTrue(enviados.isEstadoEnviado());
        assertTrue(enviados.isActivoEnviado());
        assertFalse(mapper.writeValueAsString(enviados).contains("Enviado"));
    }

    @Test
    void edicionPermitidaConservaEstadoYActivo() {
        Ejemplar datos = datosEdicion();
        datos.setCodigoInventario("nuevo-" + UUID.randomUUID());
        datos.setEstado("DISPONIBLE");
        datos.setActivo(true);
        Ejemplar actualizado = ejemplarService.actualizar(ejemplar.getIdEjemplar(), datos);
        assertEquals(datos.getCodigoInventario(), actualizado.getCodigoInventario());
        assertEquals("DISPONIBLE", estadoEjemplar());
        assertTrue(actualizado.getActivo());
    }

    @Test
    void edicionConLibroInactivoNoCambiaEjemplar() {
        libro.setActivo(false);
        libros.saveAndFlush(libro);
        Ejemplar datos = datosEdicion();
        datos.setCodigoInventario("cambio-prohibido");
        assertEstado(HttpStatus.CONFLICT,
                () -> ejemplarService.actualizar(ejemplar.getIdEjemplar(), datos));
        assertEquals(ejemplar.getCodigoInventario(),
                ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow().getCodigoInventario());
    }

    @Test
    void edicionYDesactivacionConPrestamoAbiertoNoCambianBase() {
        Prestamo creado = prestamoService.crear(solicitud(LocalDate.now(ZONA)));
        Ejemplar datos = datosEdicion();
        datos.setCodigoInventario("cambio-prohibido");
        datos.setEstado("DISPONIBLE");
        assertEstado(HttpStatus.CONFLICT,
                () -> ejemplarService.actualizar(ejemplar.getIdEjemplar(), datos));
        assertEstado(HttpStatus.CONFLICT,
                () -> ejemplarService.desactivar(ejemplar.getIdEjemplar()));
        Ejemplar guardado = ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow();
        assertEquals(ejemplar.getCodigoInventario(), guardado.getCodigoInventario());
        assertTrue(guardado.getActivo());
        assertEquals("PRESTADO", guardado.getEstado());
        assertNull(prestamos.findById(creado.getIdPrestamo())
                .orElseThrow().getFechaDevolucionReal());
    }

    @Test
    void desactivarSinPrestamoEsBajaLogica() {
        Ejemplar desactivado = ejemplarService.desactivar(ejemplar.getIdEjemplar());
        assertFalse(desactivado.getActivo());
        assertFalse(ejemplarService.listarActivos().stream()
                .anyMatch(e -> e.getIdEjemplar().equals(ejemplar.getIdEjemplar())));
        assertEstado(HttpStatus.CONFLICT,
                () -> ejemplarService.desactivar(ejemplar.getIdEjemplar()));
    }

    private Prestamo solicitud(LocalDate fecha) {
        Prestamo datos = new Prestamo();
        Usuario referenciaUsuario = new Usuario();
        referenciaUsuario.setIdUsuario(usuario.getIdUsuario());
        Ejemplar referenciaEjemplar = new Ejemplar();
        referenciaEjemplar.setIdEjemplar(ejemplar.getIdEjemplar());
        datos.setUsuario(referenciaUsuario);
        datos.setEjemplar(referenciaEjemplar);
        datos.setFechaDevolucionPrevista(fecha);
        return datos;
    }

    private Ejemplar datosEdicion() {
        Ejemplar datos = new Ejemplar();
        Libro referenciaLibro = new Libro();
        referenciaLibro.setIdLibro(libro.getIdLibro());
        datos.setLibro(referenciaLibro);
        datos.setCodigoInventario(ejemplar.getCodigoInventario());
        datos.setUbicacion("Estante A");
        datos.setEstadoFisico("BUENO");
        return datos;
    }

    private void rechazaAlta(Prestamo datos, HttpStatus estado) {
        long cantidad = prestamos.count();
        assertEstado(estado, () -> prestamoService.crear(datos));
        assertEquals(cantidad, prestamos.count());
        assertEquals("DISPONIBLE", estadoEjemplar());
    }

    private String estadoEjemplar() {
        return ejemplares.findById(ejemplar.getIdEjemplar()).orElseThrow().getEstado();
    }

    private void assertEstado(HttpStatus esperado, Runnable accion) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, accion::run);
        assertEquals(esperado, error.getStatusCode());
    }

    private int intentarAlta(CountDownLatch salida) throws InterruptedException {
        salida.await();
        try {
            prestamoService.crear(solicitud(LocalDate.now(ZONA)));
            return 201;
        } catch (ResponseStatusException ex) {
            return ex.getStatusCode().value();
        }
    }

    private int intentarDevolucion(CountDownLatch salida, Long id) throws InterruptedException {
        salida.await();
        try {
            prestamoService.devolver(id);
            return 200;
        } catch (ResponseStatusException ex) {
            return ex.getStatusCode().value();
        }
    }
}
