import { API_BASE_URL } from './apiConfig'
import BuscadorSeleccion from './components/BuscadorSeleccion'
import CentroNotificaciones from './components/CentroNotificaciones'
import Reservas from './components/Reservas'
import FichaLibroModal from './components/FichaLibroModal'
import PortadaLibro from './components/PortadaLibro'
import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { BrowserRouter, useLocation, useNavigate } from 'react-router-dom'
import Login from './components/Login.tsx'
import type { Sesion } from './components/Login.tsx'
import Registro from './components/Registro.tsx'
import SolicitudesPendientes from './components/SolicitudesPendientes.tsx'
import './App.css'
interface Autor {
    idAutor: number
    nombre: string
    apellido: string
}
interface Categoria {
    idCategoria: number
    nombre: string
}
interface Libro {
    idLibro: number
    titulo: string
    isbn: string | null
    editorial: string | null
    anioPublicacion: number | null
    activo: boolean
    categoria: Categoria
    autores: Autor[]
}
interface Ejemplar {
    idEjemplar: number
    codigoInventario: string
    estado: string
    activo: boolean
    libro: Libro
}
interface Usuario {
    idUsuario: number
    nombre: string
    apellido: string
    email: string
    rol: string
    activo: boolean
}
interface Prestamo {
    idPrestamo: number
    fechaPrestamo: string
    fechaDevolucionPrevista: string
    fechaDevolucionReal: string | null
    estado: string
    usuario: Usuario
    ejemplar: {
        idEjemplar: number
        codigoInventario: string
        estado: string
        libro: Libro
    }
}
type Vista = 'inicio' | 'catalogo' | 'prestamos' | 'mis-prestamos' | 'usuarios' | 'login' | 'registro' | 'solicitudes' | 'reservas' | 'mis-reservas'
function AppContenido() {
    const navigate = useNavigate()
    const location = useLocation()
    const rutas: Record<Vista, string> = {
        inicio: '/',
        catalogo: '/catalogo',
        prestamos: '/prestamos',
        'mis-prestamos': '/mis-prestamos',
        usuarios: '/usuarios',
        login: '/login',
        registro: '/registro',
        solicitudes: '/solicitudes',
        reservas: '/reservas',
        'mis-reservas': '/mis-reservas',
    }
    const rutaActual = location.pathname.replace(/\/$/, '') || '/'
    const vista: Vista =
        (Object.entries(rutas).find(([, ruta]) => ruta === rutaActual)?.[0] as Vista | undefined)
        ?? 'inicio'
    const setVista = (nuevaVista: Vista) => {
        navigate(rutas[nuevaVista])
    }
    const [sesion, setSesion] = useState<Sesion | null>(null)
    const [revisionDatos, setRevisionDatos] = useState(0)
    const esBibliotecario = sesion?.rol === 'BIBLIOTECARIO'
    const esLector = sesion?.rol === 'LECTOR'
    // La URL identifica la pantalla. Las rutas administrativas se restringen por rol.
    useEffect(() => {
        if (rutaActual !== rutas[vista] ||
            ((vista === 'prestamos' || vista === 'usuarios' || vista === 'solicitudes' || vista === 'reservas') && !esBibliotecario) ||
            ((vista === 'mis-prestamos' || vista === 'mis-reservas') && !esLector) ||
            ((vista === 'login' || vista === 'registro') && sesion)) {
            navigate('/', { replace: true })
        }
    }, [rutaActual, vista, esBibliotecario, esLector, sesion, navigate])
    const iniciarSesion = (nuevaSesion: Sesion) => {
        setMisPrestamos([])
        setErrorMisPrestamos('')
        setSesion(nuevaSesion)
        setVista('inicio')
    }
    const cerrarSesion = () => {
        setSesion(null)
        setUsuarios([])
        setPrestamos([])
        setMisPrestamos([])
        setErrorMisPrestamos('')
        setMostrarFormulario(false)
        setMostrarFormularioEjemplar(false)
        setMostrarFormularioUsuario(false)
        setMostrarFormularioPrestamo(false)
        setUsuarioEditando(null)
        setVista('inicio')
    }
    const [libros, setLibros] = useState<Libro[]>([])
    const [ejemplares, setEjemplares] = useState<Ejemplar[]>([])
    const [usuarios, setUsuarios] = useState<Usuario[]>([])
    const [prestamos, setPrestamos] = useState<Prestamo[]>([])
    const [misPrestamos, setMisPrestamos] = useState<Prestamo[]>([])
    const [cargandoMisPrestamos, setCargandoMisPrestamos] = useState(false)
    const [errorMisPrestamos, setErrorMisPrestamos] = useState('')
    const [categorias, setCategorias] = useState<Categoria[]>([])
    const [autores, setAutores] = useState<Autor[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState('')
    const [busqueda, setBusqueda] = useState('')
    const [mostrarFormularioUsuario, setMostrarFormularioUsuario] = useState(false)
    const [guardandoUsuario, setGuardandoUsuario] = useState(false)
    const [mensajeUsuario, setMensajeUsuario] = useState('')
    const [nuevoUsuario, setNuevoUsuario] = useState({
        nombre: '',
        apellido: '',
        email: '',
        password: '',
    })
    const [usuarioEditando, setUsuarioEditando] = useState<number | null>(null)
    const [datosEdicionUsuario, setDatosEdicionUsuario] = useState({
        nombre: '',
        apellido: '',
        email: '',
    })
    const [guardandoEdicionUsuario, setGuardandoEdicionUsuario] = useState(false)
    const [desactivandoUsuarioId, setDesactivandoUsuarioId] = useState<number | null>(null)
    const [mostrarFormularioPrestamo, setMostrarFormularioPrestamo] = useState(false)
    const [guardandoPrestamo, setGuardandoPrestamo] = useState(false)
    const [mensajePrestamo, setMensajePrestamo] = useState('')
    const [nuevoPrestamo, setNuevoPrestamo] = useState({
        idUsuario: '',
        idEjemplar: '',
        fechaDevolucionPrevista: '',
    })
    const [mostrarFormulario, setMostrarFormulario] = useState(false)
    const [mostrarFormularioEjemplar, setMostrarFormularioEjemplar] = useState(false)
    const [guardandoEjemplar, setGuardandoEjemplar] = useState(false)
    const [mensajeEjemplar, setMensajeEjemplar] = useState('')
    const [nuevoEjemplar, setNuevoEjemplar] = useState({
        idLibro: '',
        codigoInventario: '',
    })
    const [guardandoLibro, setGuardandoLibro] = useState(false)
    const [mensajeLibro, setMensajeLibro] = useState('')
    const [nuevoLibro, setNuevoLibro] = useState({
        titulo: '',
        isbn: '',
        editorial: '',
        anioPublicacion: '',
        idCategoria: '',
        idAutor: '',
    })
    useEffect(() => {
        let cancelado = false
        const cargarDatos = async () => {
            setCargando(true)
            setError('')
            try {
                const [respuestaLibros, respuestaEjemplares,
                    respuestaCategorias, respuestaAutores] = await Promise.all([
                    fetch(`${API_BASE_URL}/api/libros/activos`),
                    fetch(`${API_BASE_URL}/api/ejemplares/activos`),
                    fetch(`${API_BASE_URL}/api/categorias`),
                    fetch(`${API_BASE_URL}/api/autores`),
                ])
                if (
                    !respuestaLibros.ok ||
                    !respuestaEjemplares.ok ||
                    !respuestaCategorias.ok ||
                    !respuestaAutores.ok
                ) {
                    throw new Error('No se pudieron cargar los datos públicos.')
                }
                const datosLibros: Libro[] = await respuestaLibros.json()
                const datosEjemplares: Ejemplar[] = await respuestaEjemplares.json()
                const datosCategorias: Categoria[] = await respuestaCategorias.json()
                const datosAutores: Autor[] = await respuestaAutores.json()
                if (cancelado) return
                setLibros(datosLibros)
                setEjemplares(datosEjemplares)
                setCategorias(datosCategorias)
                setAutores(datosAutores)
                if (sesion?.rol === 'BIBLIOTECARIO') {
                    const [respuestaUsuarios, respuestaPrestamos] =
                        await Promise.all([
                            fetch(`${API_BASE_URL}/api/usuarios/activos`, {
                                headers: {
                                    Authorization: sesion.autorizacion,
                                },
                            }),
                            fetch(`${API_BASE_URL}/api/prestamos`, {
                                headers: {
                                    Authorization: sesion.autorizacion,
                                },
                            }),
                        ])
                    if (!respuestaUsuarios.ok || !respuestaPrestamos.ok) {
                        throw new Error(
                            'No se pudieron cargar los datos administrativos.'
                        )
                    }
                    const datosUsuarios: Usuario[] = await respuestaUsuarios.json()
                    const datosPrestamos: Prestamo[] = await respuestaPrestamos.json()
                    if (cancelado) return
                    setUsuarios(datosUsuarios)
                    setPrestamos(datosPrestamos)
                } else {
                    setUsuarios([])
                    setPrestamos([])
                }
            } catch (error) {
                if (!cancelado) {
                    setError(
                        error instanceof Error
                            ? error.message
                            : 'No se pudo conectar con el servidor.'
                    )
                }
            } finally {
                if (!cancelado) {
                    setCargando(false)
                }
            }
        }
        void cargarDatos()
        return () => {
            cancelado = true
        }
    }, [sesion, revisionDatos])
    // Solo consulta los préstamos del lector autenticado cuando abre su pantalla.
    useEffect(() => {
        if (vista !== 'mis-prestamos' || !esLector || !sesion) return
        const controlador = new AbortController()
        const cargarMisPrestamos = async () => {
            setCargandoMisPrestamos(true)
            setErrorMisPrestamos('')
            try {
                const respuesta = await fetch(`${API_BASE_URL}/api/prestamos/mis-prestamos`, {
                    headers: { Authorization: sesion.autorizacion },
                    signal: controlador.signal,
                })
                if (!respuesta.ok) {
                    throw new Error(respuesta.status === 401 || respuesta.status === 403
                        ? 'Tu sesión no está autorizada. Cerrá sesión y volvé a ingresar.'
                        : 'No se pudieron cargar tus préstamos.')
                }
                const datos: Prestamo[] = await respuesta.json()
                if (!controlador.signal.aborted) setMisPrestamos(datos)
            } catch (error) {
                if (!controlador.signal.aborted) {
                    setErrorMisPrestamos(error instanceof Error ? error.message : 'Error al consultar los préstamos.')
                }
            } finally {
                if (!controlador.signal.aborted) setCargandoMisPrestamos(false)
            }
        }
        void cargarMisPrestamos()
        return () => controlador.abort()
    }, [vista, esLector, sesion])
    const mostrarMisPrestamos = () => (
        <section className="panel">
            <h2>Mis préstamos</h2>
            <p>Consultá tus préstamos activos y el historial de devoluciones.</p>
            {cargandoMisPrestamos ? (
                <p role="status">Cargando tus préstamos...</p>
            ) : errorMisPrestamos ? (
                <p className="mensaje-error" role="alert">{errorMisPrestamos}</p>
            ) : misPrestamos.length === 0 ? (
                <p className="mis-prestamos-vacio">Todavía no tenés préstamos registrados.</p>
            ) : (
                <div className="tarjetas">
                                {esLector && (
                                    <article className="tarjeta tarjeta-acceso">
                                        <span aria-hidden="true">📑</span>
                                        <h3>Mis reservas</h3>
                                        <p>Consultá tus solicitudes y los libros listos para retirar.</p>
                                        <button
                                            className="boton-principal"
                                            onClick={() => setVista('mis-reservas')}
                                        >
                                            Ver mis reservas
                                        </button>
                                    </article>
                                )}

                    {misPrestamos.map((prestamo) => (
                        <article className="tarjeta" key={prestamo.idPrestamo}>
                            <span>📖</span>
                            <h3>{prestamo.ejemplar.libro.titulo}</h3>
                            <p><strong>Inventario:</strong> {prestamo.ejemplar.codigoInventario}</p>
                            <p><strong>Fecha del préstamo:</strong> {prestamo.fechaPrestamo}</p>
                            <p><strong>Devolución prevista:</strong> {prestamo.fechaDevolucionPrevista}</p>
                            <p><strong>Devolución real:</strong> {prestamo.fechaDevolucionReal ?? 'Pendiente'}</p>
                            <p><strong>Estado:</strong> <span className={prestamo.estado === 'DEVUELTO' ? 'estado-devuelto' : 'estado-activo'}>{prestamo.estado}</span></p>
                        </article>
                    ))}
                </div>
            )}
        </section>
    )
    const guardarLibro = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (guardandoLibro) return
        setGuardandoLibro(true)
        setMensajeLibro('')
        try {
            const datos = {
                titulo: nuevoLibro.titulo.trim(),
                isbn: nuevoLibro.isbn.trim() || null,
                editorial: nuevoLibro.editorial.trim() || null,
                anioPublicacion: nuevoLibro.anioPublicacion
                    ? Number(nuevoLibro.anioPublicacion)
                    : null,
                activo: true,
                categoria: {
                    idCategoria: Number(nuevoLibro.idCategoria),
                },
                autores: [
                    {
                        idAutor: Number(nuevoLibro.idAutor),
                    },
                ],
            }
            const respuesta = await fetch(`${API_BASE_URL}/api/libros`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: sesion?.autorizacion ?? '',
                },
                body: JSON.stringify(datos),
            })
            if (!respuesta.ok) {
                throw new Error('No se pudo guardar el libro')
            }
            const respuestaLibros = await fetch(
                `${API_BASE_URL}/api/libros/activos`
            )
            if (!respuestaLibros.ok) {
                throw new Error('El libro se guardó, pero no se pudo actualizar el catálogo')
            }
            const librosActualizados: Libro[] = await respuestaLibros.json()
            setLibros(librosActualizados)
            setNuevoLibro({
                titulo: '',
                isbn: '',
                editorial: '',
                anioPublicacion: '',
                idCategoria: '',
                idAutor: '',
            })
            setBusqueda('')
            setMostrarFormulario(false)
            setMensajeLibro('Libro registrado correctamente.')
        } catch (error) {
            setMensajeLibro(
                error instanceof Error ? error.message : 'Ocurrió un error.'
            )
        } finally {
            setGuardandoLibro(false)
        }
    }
    const guardarEjemplar = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (guardandoEjemplar) return
        setGuardandoEjemplar(true)
        setMensajeEjemplar('')
        try {
            const datos = {
                codigoInventario: nuevoEjemplar.codigoInventario.trim(),
                estado: 'DISPONIBLE',
                activo: true,
                libro: {
                    idLibro: Number(nuevoEjemplar.idLibro),
                },
            }
            const respuesta = await fetch(`${API_BASE_URL}/api/ejemplares`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: sesion?.autorizacion ?? '',
                },
                body: JSON.stringify(datos),
            })
            if (!respuesta.ok) {
                throw new Error('No se pudo guardar el ejemplar')
            }
            const respuestaEjemplares = await fetch(
                `${API_BASE_URL}/api/ejemplares/activos`
            )
            if (!respuestaEjemplares.ok) {
                throw new Error(
                    'El ejemplar se guardó, pero no se pudo actualizar el catálogo'
                )
            }
            const ejemplaresActualizados: Ejemplar[] =
                await respuestaEjemplares.json()
            setEjemplares(ejemplaresActualizados)
            setNuevoEjemplar({
                idLibro: '',
                codigoInventario: '',
            })
            setMostrarFormularioEjemplar(false)
            setMensajeEjemplar('Ejemplar registrado correctamente.')
        } catch (error) {
            setMensajeEjemplar(
                error instanceof Error ? error.message : 'Ocurrió un error.'
            )
        } finally {
            setGuardandoEjemplar(false)
        }
    }
    const [filtroCategoria, setFiltroCategoria] = useState('')
    const [filtroDisponibilidad, setFiltroDisponibilidad] = useState('')
    const [orden, setOrden] = useState('titulo')
    const [pagina, setPagina] = useState(1)
    const [porPagina, setPorPagina] = useState(12)
    const [ficha, setFicha] = useState<number | null>(null)
    const normalizar = (texto: string) => texto.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
    const porLibro = useMemo(() => {
        const mapa = new Map<number, Ejemplar[]>()
        for (const ejemplar of ejemplares) {
            if (!ejemplar.activo) continue
            const lista = mapa.get(ejemplar.libro.idLibro) ?? []
            lista.push(ejemplar)
            mapa.set(ejemplar.libro.idLibro, lista)
        }
        return mapa
    }, [ejemplares])
    const disponibles = (id: number) => (porLibro.get(id) ?? []).filter(e => e.estado === 'DISPONIBLE').length
    const nombreAutor = (libro: Libro) => libro.autores.map(a => `${a.apellido}, ${a.nombre}`).join('; ')
    const librosFiltrados = libros.filter(libro => {
        const texto = normalizar([libro.titulo, libro.editorial ?? '', libro.isbn ?? '', libro.categoria.nombre,
            ...libro.autores.map(a => `${a.nombre} ${a.apellido} ${a.apellido} ${a.nombre}`),
            ...(porLibro.get(libro.idLibro) ?? []).map(e => e.codigoInventario)].join(' '))
        return normalizar(busqueda.trim()).split(/\s+/).every(p => texto.includes(p)) &&
            (!filtroCategoria || String(libro.categoria.idCategoria) === filtroCategoria) &&
            (!filtroDisponibilidad || (filtroDisponibilidad === 'si' ? disponibles(libro.idLibro) > 0 : disponibles(libro.idLibro) === 0))
    }).sort((a,b) => orden === 'autor' ? nombreAutor(a).localeCompare(nombreAutor(b), 'es') :
        orden === 'anio' ? (b.anioPublicacion ?? 0) - (a.anioPublicacion ?? 0) : a.titulo.localeCompare(b.titulo, 'es'))
    const totalPaginas = Math.max(1, Math.ceil(librosFiltrados.length / porPagina))
    const paginaActual = Math.min(pagina, totalPaginas)
    const librosPagina = librosFiltrados.slice((paginaActual - 1) * porPagina, paginaActual * porPagina)
    const cambiarFiltro = () => { setPagina(1); setFicha(null) }
    const limpiarFiltros = () => { setBusqueda(''); setFiltroCategoria(''); setFiltroDisponibilidad(''); setOrden('titulo'); cambiarFiltro() }
    const mostrarCatalogo = () => (
        <section className="panel">
            <h2>Catálogo de libros</h2>
            {esBibliotecario && (
                <button
                    className="boton-principal"
                    onClick={() => {
                        setMostrarFormulario(!mostrarFormulario)
                        setMensajeLibro('')
                    }}
                >
                    {mostrarFormulario ? 'Cancelar' : '+ Agregar libro'}
                </button>
            )}
            {esBibliotecario && mostrarFormularioEjemplar && (
                <form className="formulario-libro" onSubmit={guardarEjemplar}>
                    <h3>Registrar nuevo ejemplar</h3>
                    <label htmlFor="libro-ejemplar">Seleccionar libro *</label>
                    <select
                        id="libro-ejemplar"
                        required
                        value={nuevoEjemplar.idLibro}
                        onChange={(e) =>
                            setNuevoEjemplar({
                                ...nuevoEjemplar,
                                idLibro: e.target.value,
                            })
                        }
                    >
                        <option value="">Seleccionar libro</option>
                        {libros.map((libro) => (
                            <option key={libro.idLibro} value={libro.idLibro}>
                                {libro.titulo}
                            </option>
                        ))}
                    </select>
                    <label htmlFor="codigo-inventario">Código de inventario *</label>
                    <input
                        id="codigo-inventario"
                        type="text"
                        required
                        maxLength={50}
                        placeholder="Ejemplo: PRUEBA-BG-001"
                        value={nuevoEjemplar.codigoInventario}
                        onChange={(e) =>
                            setNuevoEjemplar({
                                ...nuevoEjemplar,
                                codigoInventario: e.target.value,
                            })
                        }
                    />
                    <button
                        type="submit"
                        className="boton-principal"
                        disabled={guardandoEjemplar}
                    >
                        {guardandoEjemplar ? 'Guardando...' : 'Guardar ejemplar'}
                    </button>
                </form>
            )}
            {esBibliotecario && (
                <button
                    className="boton-principal"
                    onClick={() => {
                        setMostrarFormularioEjemplar(!mostrarFormularioEjemplar)
                        setMensajeEjemplar('')
                    }}
                >
                    {mostrarFormularioEjemplar ? 'Cancelar ejemplar' : '+ Agregar ejemplar'}
                </button>
            )}
            {mensajeEjemplar && <p role="status">{mensajeEjemplar}</p>}
            {mensajeLibro && <p role="status">{mensajeLibro}</p>}
            <div className="buscador">
                {esBibliotecario && mostrarFormulario && (
                    <form className="formulario-libro" onSubmit={guardarLibro}>
                        <h3>Registrar nuevo libro</h3>
                        <label htmlFor="titulo">Título *</label>
                        <input
                            id="titulo"
                            type="text"
                            required
                            maxLength={200}
                            value={nuevoLibro.titulo}
                            onChange={(e) =>
                                setNuevoLibro({ ...nuevoLibro, titulo: e.target.value })
                            }
                        />
                        <label htmlFor="isbn">ISBN</label>
                        <input
                            id="isbn"
                            type="text"
                            maxLength={20}
                            value={nuevoLibro.isbn}
                            onChange={(e) =>
                                setNuevoLibro({ ...nuevoLibro, isbn: e.target.value })
                            }
                        />
                        <label htmlFor="editorial">Editorial</label>
                        <input
                            id="editorial"
                            type="text"
                            maxLength={150}
                            value={nuevoLibro.editorial}
                            onChange={(e) =>
                                setNuevoLibro({ ...nuevoLibro, editorial: e.target.value })
                            }
                        />
                        <label htmlFor="anio">Año de publicación</label>
                        <input
                            id="anio"
                            type="number"
                            min="1"
                            max="2100"
                            value={nuevoLibro.anioPublicacion}
                            onChange={(e) =>
                                setNuevoLibro({
                                    ...nuevoLibro,
                                    anioPublicacion: e.target.value,
                                })
                            }
                        />
                        <label htmlFor="categoria">Categoría *</label>
                        <select
                            id="categoria"
                            required
                            value={nuevoLibro.idCategoria}
                            onChange={(e) =>
                                setNuevoLibro({ ...nuevoLibro, idCategoria: e.target.value })
                            }
                        >
                            <option value="">Seleccionar categoría</option>
                            {categorias.map((categoria) => (
                                <option key={categoria.idCategoria} value={categoria.idCategoria}>
                                    {categoria.nombre}
                                </option>
                            ))}
                        </select>
                        <label htmlFor="autor">Autor *</label>
                        <select
                            id="autor"
                            required
                            value={nuevoLibro.idAutor}
                            onChange={(e) =>
                                setNuevoLibro({ ...nuevoLibro, idAutor: e.target.value })
                            }
                        >
                            <option value="">Seleccionar autor</option>
                            {autores.map((autor) => (
                                <option key={autor.idAutor} value={autor.idAutor}>
                                    {autor.nombre} {autor.apellido}
                                </option>
                            ))}
                        </select>
                        <button
                            type="submit"
                            className="boton-principal"
                            disabled={guardandoLibro}
                        >
                            {guardandoLibro ? 'Guardando...' : 'Guardar libro'}
                        </button>
                    </form>
                )}
                <div className="filtros-catalogo">
                    <label className="campo-busqueda">Buscar en el catálogo<input type="search" placeholder="Título, autor, editorial, ISBN o inventario" value={busqueda} onChange={e => { setBusqueda(e.target.value); cambiarFiltro() }} /></label>
                    <label>Categoría<select value={filtroCategoria} onChange={e => { setFiltroCategoria(e.target.value); cambiarFiltro() }}><option value="">Todas</option>{categorias.map(c => <option key={c.idCategoria} value={c.idCategoria}>{c.nombre}</option>)}</select></label>
                    <label>Disponibilidad<select value={filtroDisponibilidad} onChange={e => { setFiltroDisponibilidad(e.target.value); cambiarFiltro() }}><option value="">Todas</option><option value="si">Disponibles</option><option value="no">Sin disponibilidad</option></select></label>
                    <label>Ordenar por<select value={orden} onChange={e => { setOrden(e.target.value); cambiarFiltro() }}><option value="titulo">Título A–Z</option><option value="autor">Autor A–Z</option><option value="anio">Año más reciente</option></select></label>
                    <button className="boton-secundario" onClick={limpiarFiltros}>Limpiar filtros</button>
                </div>
            </div>
            <div className="resultados-catalogo"><p role="status">{librosFiltrados.length.toLocaleString('es-AR')} títulos encontrados</p><label>Por página <select value={porPagina} onChange={e => { setPorPagina(Number(e.target.value)); cambiarFiltro() }}>{[12,24,48].map(n => <option key={n}>{n}</option>)}</select></label></div>
            {librosPagina.length === 0 ? <p className="mis-prestamos-vacio">No se encontraron títulos con estos filtros.</p> : (
                <div className="tarjetas catalogo-grid">
                    {librosPagina.map(libro => (
                        <article className="tarjeta tarjeta-libro" key={libro.idLibro}>
                            <div className="libro-cabecera"><span className="categoria-libro">{libro.categoria.nombre}</span></div>
                            <PortadaLibro
                                key={libro.idLibro}
                                idLibro={libro.idLibro}
                                titulo={libro.titulo}
                                autor={nombreAutor(libro)}
                                categoria={libro.categoria.nombre}
                            />
                            <h3>{libro.titulo}</h3><p className="autor-libro">{nombreAutor(libro) || 'Sin autor registrado'}</p>
                            <p>{libro.editorial || 'Editorial no informada'} · {libro.anioPublicacion ?? 'Sin año'}</p>
                            <div className="libro-pie"><span className={disponibles(libro.idLibro) ? 'estado-devuelto' : 'estado-no-disponible'}>{disponibles(libro.idLibro)} disponibles / {(porLibro.get(libro.idLibro) ?? []).length}</span><button className="boton-secundario" aria-expanded={ficha === libro.idLibro} onClick={() => setFicha(ficha === libro.idLibro ? null : libro.idLibro)}>{ficha === libro.idLibro ? 'Cerrar ficha' : 'Ver ficha'}</button></div>

                            {ficha === libro.idLibro && (
                                <FichaLibroModal
                                    autorizacionLector={esLector ? sesion?.autorizacion : undefined}
                                    autorizacion={esBibliotecario ? sesion?.autorizacion : undefined}
                                    key={libro.idLibro}
                                    idLibro={libro.idLibro}
                                    titulo={libro.titulo}
                                    autor={nombreAutor(libro) || 'Sin autor registrado'}
                                    categoria={libro.categoria.nombre}
                                    editorial={libro.editorial || 'No informada'}
                                    anio={libro.anioPublicacion ?? null}
                                    isbn={libro.isbn || 'No informado'}
                                    ejemplares={porLibro.get(libro.idLibro) ?? []}
                                    onCerrar={() => setFicha(null)}
                                />
                            )}

                        </article>
                    ))}
                </div>
            )}
            <nav className="paginacion" aria-label="Paginación del catálogo"><button className="boton-secundario" disabled={paginaActual === 1} onClick={() => { setPagina(paginaActual - 1); setFicha(null) }}>Anterior</button><span>Página {paginaActual} de {totalPaginas}</span><button className="boton-secundario" disabled={paginaActual === totalPaginas} onClick={() => { setPagina(paginaActual + 1); setFicha(null) }}>Siguiente</button></nav>
        </section>
    )
    const guardarUsuario = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (guardandoUsuario) return
        setGuardandoUsuario(true)
        setMensajeUsuario('')
        try {
            const datos = {
                nombre: nuevoUsuario.nombre.trim(),
                apellido: nuevoUsuario.apellido.trim(),
                email: nuevoUsuario.email.trim(),
                password: nuevoUsuario.password,
                rol: 'LECTOR',
                activo: true,
            }
            const respuesta = await fetch(`${API_BASE_URL}/api/usuarios`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: sesion?.autorizacion ?? '',
                },
                body: JSON.stringify(datos),
            })
            if (!respuesta.ok) {
                throw new Error(
                    respuesta.status === 409
                        ? 'Ya existe un usuario con ese correo electrónico.'
                        : 'No se pudo registrar el lector.'
                )
            }
            const respuestaUsuarios = await fetch(
                `${API_BASE_URL}/api/usuarios/activos`,
                {
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }
            )
            if (!respuestaUsuarios.ok) {
                throw new Error(
                    'El lector se registró, pero no se pudo actualizar el listado. Actualizá la página antes de intentar nuevamente.'
                )
            }
            const usuariosActualizados: Usuario[] =
                await respuestaUsuarios.json()
            setUsuarios(usuariosActualizados)
            setNuevoUsuario({
                nombre: '',
                apellido: '',
                email: '',
                password: '',
            })
            setMostrarFormularioUsuario(false)
            setMensajeUsuario('Lector registrado correctamente.')
        } catch (error) {
            setMensajeUsuario(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error al registrar el lector.'
            )
        } finally {
            setGuardandoUsuario(false)
        }
    }
    const iniciarEdicionUsuario = (usuario: Usuario) => {
        setUsuarioEditando(usuario.idUsuario)
        setMostrarFormularioUsuario(false)
        setMensajeUsuario('')
        setDatosEdicionUsuario({
            nombre: usuario.nombre,
            apellido: usuario.apellido,
            email: usuario.email,
        })
    }
    const guardarEdicionUsuario = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (usuarioEditando === null || guardandoEdicionUsuario) return
        const usuarioActual = usuarios.find(
            (usuario) => usuario.idUsuario === usuarioEditando
        )
        if (!usuarioActual) {
            setMensajeUsuario('No se encontró el lector seleccionado.')
            return
        }
        setGuardandoEdicionUsuario(true)
        setMensajeUsuario('')
        try {
            const respuesta = await fetch(
                `${API_BASE_URL}/api/usuarios/${usuarioEditando}`,
                {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        Authorization: sesion?.autorizacion ?? '',
                    },
                    body: JSON.stringify({
                        nombre: datosEdicionUsuario.nombre.trim(),
                        apellido: datosEdicionUsuario.apellido.trim(),
                        email: datosEdicionUsuario.email.trim(),
                        rol: usuarioActual.rol,
                    }),
                }
            )
            if (!respuesta.ok) {
                throw new Error(
                    respuesta.status === 409
                        ? 'Ya existe un usuario con ese correo electrónico.'
                        : 'No se pudo actualizar el lector.'
                )
            }
            const respuestaUsuarios = await fetch(
                `${API_BASE_URL}/api/usuarios/activos`,
                {
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }
            )
            if (!respuestaUsuarios.ok) {
                throw new Error(
                    'El lector se actualizó, pero no se pudo refrescar el listado. Actualizá la página.'
                )
            }
            const usuariosActualizados: Usuario[] = await respuestaUsuarios.json()
            setUsuarios(usuariosActualizados)
            setUsuarioEditando(null)
            setMensajeUsuario('Lector actualizado correctamente.')
        } catch (error) {
            setMensajeUsuario(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error al actualizar el lector.'
            )
        } finally {
            setGuardandoEdicionUsuario(false)
        }
    }
    const desactivarUsuario = async (idUsuario: number) => {
        if (desactivandoUsuarioId !== null) return
        const confirmar = window.confirm(
            '¿Confirmás que querés desactivar este lector?'
        )
        if (!confirmar) return
        setDesactivandoUsuarioId(idUsuario)
        setMensajeUsuario('')
        try {
            const respuesta = await fetch(
                `${API_BASE_URL}/api/usuarios/${idUsuario}/desactivar`,
                {
                    method: 'PUT',
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }
            )
            if (!respuesta.ok) {
                throw new Error('No se pudo desactivar el lector.')
            }
            const respuestaUsuarios = await fetch(
                `${API_BASE_URL}/api/usuarios/activos`,
                {
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }
            )
            if (!respuestaUsuarios.ok) {
                throw new Error(
                    'El lector se desactivó, pero no se pudo refrescar el listado. Actualizá la página.'
                )
            }
            const usuariosActualizados: Usuario[] = await respuestaUsuarios.json()
            setUsuarios(usuariosActualizados)
            if (usuarioEditando === idUsuario) {
                setUsuarioEditando(null)
            }
            setMensajeUsuario('Lector desactivado correctamente.')
        } catch (error) {
            setMensajeUsuario(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error al desactivar el lector.'
            )
        } finally {
            setDesactivandoUsuarioId(null)
        }
    }
    const mostrarUsuarios = () => (
        <section className="panel">
            <h2>Usuarios activos</h2>
            <button
                className="boton-principal"
                onClick={() => {
                    setMostrarFormularioUsuario(!mostrarFormularioUsuario)
                    setMensajeUsuario('')
                }}
            >
                {mostrarFormularioUsuario
                    ? 'Cancelar'
                    : '+ Agregar lector'}
            </button>
            {mensajeUsuario && <p role="status">{mensajeUsuario}</p>}
            {mostrarFormularioUsuario && (
                <form className="formulario-libro" onSubmit={guardarUsuario}>
                    <h3>Registrar nuevo lector</h3>
                    <label htmlFor="nombre-usuario">Nombre *</label>
                    <input
                        id="nombre-usuario"
                        type="text"
                        required
                        maxLength={100}
                        value={nuevoUsuario.nombre}
                        onChange={(e) =>
                            setNuevoUsuario({
                                ...nuevoUsuario,
                                nombre: e.target.value,
                            })
                        }
                    />
                    <label htmlFor="apellido-usuario">Apellido *</label>
                    <input
                        id="apellido-usuario"
                        type="text"
                        required
                        maxLength={100}
                        value={nuevoUsuario.apellido}
                        onChange={(e) =>
                            setNuevoUsuario({
                                ...nuevoUsuario,
                                apellido: e.target.value,
                            })
                        }
                    />
                    <label htmlFor="email-usuario">Correo electrónico *</label>
                    <input
                        id="email-usuario"
                        type="email"
                        required
                        maxLength={150}
                        value={nuevoUsuario.email}
                        onChange={(e) =>
                            setNuevoUsuario({
                                ...nuevoUsuario,
                                email: e.target.value,
                            })
                        }
                    />
                    <label htmlFor="password-usuario">Contraseña *</label>
                    <input
                        id="password-usuario"
                        type="password"
                        required
                        minLength={8}
                        autoComplete="new-password"
                        value={nuevoUsuario.password}
                        onChange={(e) =>
                            setNuevoUsuario({
                                ...nuevoUsuario,
                                password: e.target.value,
                            })
                        }
                    />
                    <button
                        type="submit"
                        className="boton-principal"
                        disabled={guardandoUsuario}
                    >
                        {guardandoUsuario
                            ? 'Guardando...'
                            : 'Guardar lector'}
                    </button>
                </form>
            )}
            {usuarioEditando !== null && (
                <form
                    className="formulario-libro"
                    onSubmit={guardarEdicionUsuario}
                >
                    <h3>Editar lector</h3>
                    <label htmlFor="editar-nombre">Nombre *</label>
                    <input
                        id="editar-nombre"
                        type="text"
                        required
                        maxLength={100}
                        value={datosEdicionUsuario.nombre}
                        onChange={(e) =>
                            setDatosEdicionUsuario({
                                ...datosEdicionUsuario,
                                nombre: e.target.value,
                            })
                        }
                    />
                    <label htmlFor="editar-apellido">Apellido *</label>
                    <input
                        id="editar-apellido"
                        type="text"
                        required
                        maxLength={100}
                        value={datosEdicionUsuario.apellido}
                        onChange={(e) =>
                            setDatosEdicionUsuario({
                                ...datosEdicionUsuario,
                                apellido: e.target.value,
                            })
                        }
                    />
                    <label htmlFor="editar-email">Correo electrónico *</label>
                    <input
                        id="editar-email"
                        type="email"
                        required
                        maxLength={150}
                        value={datosEdicionUsuario.email}
                        onChange={(e) =>
                            setDatosEdicionUsuario({
                                ...datosEdicionUsuario,
                                email: e.target.value,
                            })
                        }
                    />
                    <button
                        type="submit"
                        className="boton-principal"
                        disabled={guardandoEdicionUsuario}
                    >
                        {guardandoEdicionUsuario ? 'Guardando...' : 'Guardar cambios'}
                    </button>
                    <button
                        type="button"
                        className="boton-principal"
                        onClick={() => setUsuarioEditando(null)}
                    >
                        Cancelar edición
                    </button>
                </form>
            )}
            {usuarios.length === 0 ? (
                <p>No hay usuarios activos.</p>
            ) : (
                <div className="tarjetas">
                                {esLector && (
                                    <article className="tarjeta tarjeta-acceso">
                                        <span aria-hidden="true">📑</span>
                                        <h3>Mis reservas</h3>
                                        <p>Consultá tus solicitudes y los libros listos para retirar.</p>
                                        <button
                                            className="boton-principal"
                                            onClick={() => setVista('mis-reservas')}
                                        >
                                            Ver mis reservas
                                        </button>
                                    </article>
                                )}

                    {usuarios.map((usuario) => (
                        <article className="tarjeta" key={usuario.idUsuario}>
                            <span>👤</span>
                            <h3>
                                {usuario.nombre} {usuario.apellido}
                            </h3>
                            <p>
                                <strong>Email:</strong> {usuario.email}
                            </p>
                            <p>
                                <strong>Rol:</strong> {usuario.rol}
                            </p>
                            <div className="acciones-usuario">
                                <button
                                    type="button"
                                    className="boton-principal"
                                    onClick={() => iniciarEdicionUsuario(usuario)}
                                >
                                    Editar
                                </button>
                                <button
                                    type="button"
                                    className="boton-principal"
                                    disabled={desactivandoUsuarioId !== null}
                                    onClick={() => desactivarUsuario(usuario.idUsuario)}
                                >
                                    {desactivandoUsuarioId === usuario.idUsuario
                                        ? 'Desactivando...'
                                        : 'Desactivar'}
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </section>
    )
    const guardarPrestamo = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (guardandoPrestamo) return
        setGuardandoPrestamo(true)
        setMensajePrestamo('')
        try {
            const hoy = new Date()
            const fechaPrestamo =
                `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`
            if (nuevoPrestamo.fechaDevolucionPrevista < fechaPrestamo) {
                throw new Error('La devolución prevista no puede ser anterior a hoy.')
            }
            const datos = {
                usuario: {
                    idUsuario: Number(nuevoPrestamo.idUsuario),
                },
                ejemplar: {
                    idEjemplar: Number(nuevoPrestamo.idEjemplar),
                },
                fechaPrestamo,
                fechaDevolucionPrevista: nuevoPrestamo.fechaDevolucionPrevista,
            }
            const respuesta = await fetch(`${API_BASE_URL}/api/prestamos`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: sesion?.autorizacion ?? '',
                },
                body: JSON.stringify(datos),
            })
            if (!respuesta.ok) {
                if (respuesta.status === 409) {
                    throw new Error('El usuario o el ejemplar no está disponible.')
                }
                throw new Error('No se pudo registrar el préstamo.')
            }
            const [respuestaPrestamos, respuestaEjemplares] = await Promise.all([
                fetch(`${API_BASE_URL}/api/prestamos`, {
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }),
                fetch(`${API_BASE_URL}/api/ejemplares/activos`),
            ])
            if (!respuestaPrestamos.ok || !respuestaEjemplares.ok) {
                throw new Error(
                    'El préstamo se registró, pero no se pudieron actualizar los listados. Actualizá la página antes de intentar nuevamente.'
                )
            }
            const prestamosActualizados: Prestamo[] =
                await respuestaPrestamos.json()
            const ejemplaresActualizados: Ejemplar[] =
                await respuestaEjemplares.json()
            setPrestamos(prestamosActualizados)
            setEjemplares(ejemplaresActualizados)
            setNuevoPrestamo({
                idUsuario: '',
                idEjemplar: '',
                fechaDevolucionPrevista: '',
            })
            setMostrarFormularioPrestamo(false)
            setMensajePrestamo('Préstamo registrado correctamente.')
        } catch (error) {
            setMensajePrestamo(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error al registrar el préstamo.'
            )
        } finally {
            setGuardandoPrestamo(false)
        }
    }
    const registrarDevolucion = async (idPrestamo: number) => {
        const confirmar = window.confirm(
            '¿Confirmás la devolución de este ejemplar?'
        )
        if (!confirmar) return
        setMensajePrestamo('')
        try {
            const respuesta = await fetch(
                `${API_BASE_URL}/api/prestamos/${idPrestamo}/devolver`,
                {
                    method: 'PUT',
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }
            )
            if (!respuesta.ok) {
                if (respuesta.status === 409) {
                    throw new Error('Este préstamo ya fue devuelto.')
                }
                throw new Error('No se pudo registrar la devolución.')
            }
            const [respuestaPrestamos, respuestaEjemplares] = await Promise.all([
                fetch(`${API_BASE_URL}/api/prestamos`, {
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }),
                fetch(`${API_BASE_URL}/api/ejemplares/activos`),
            ])
            if (!respuestaPrestamos.ok || !respuestaEjemplares.ok) {
                throw new Error(
                    'La devolución se registró, pero no se pudieron actualizar los listados. Actualizá la página.'
                )
            }
            const prestamosActualizados: Prestamo[] =
                await respuestaPrestamos.json()
            const ejemplaresActualizados: Ejemplar[] =
                await respuestaEjemplares.json()
            setPrestamos(prestamosActualizados)
            setEjemplares(ejemplaresActualizados)
            setMensajePrestamo('Devolución registrada correctamente.')
        } catch (error) {
            setMensajePrestamo(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error al registrar la devolución.'
            )
        }
    }
    const mostrarPrestamos = () => (
        <section className="panel">
            <h2>Préstamos</h2>
            <button
                className="boton-principal"
                onClick={() => {
                    setMostrarFormularioPrestamo(!mostrarFormularioPrestamo)
                    setMensajePrestamo('')
                }}
            >
                {mostrarFormularioPrestamo
                    ? 'Cancelar préstamo'
                    : '+ Registrar préstamo'}
            </button>
            {mensajePrestamo && (
                <p role="status">{mensajePrestamo}</p>
            )}
            {mostrarFormularioPrestamo && (
                <form className="formulario-libro" onSubmit={guardarPrestamo}>
                    <h3>Registrar nuevo préstamo</h3>
                    <label htmlFor="usuario-prestamo">Seleccionar lector *</label>
                    <BuscadorSeleccion
                        id="usuario-prestamo"
                        valor={String(nuevoPrestamo.idUsuario)}
                        opciones={usuarios.map((usuario) => ({
                            id: String(usuario.idUsuario),
                            etiqueta: `${usuario.nombre} ${usuario.apellido}`,
                        }))}
                        placeholder="Escribí el nombre o apellido del lector..."
                        onChange={(valor) =>
                            setNuevoPrestamo((anterior) => ({
                                ...anterior,
                                idUsuario: valor,
                            }))
                        }
                    />
                    <label htmlFor="ejemplar-prestamo">
                        Seleccionar ejemplar disponible *
                    </label>
                    <BuscadorSeleccion
                        id="ejemplar-prestamo"
                        valor={String(nuevoPrestamo.idEjemplar)}
                        opciones={ejemplares
                            .filter(
                                (ejemplar) =>
                                    ejemplar.activo &&
                                    ejemplar.estado === 'DISPONIBLE'
                            )
                            .map((ejemplar) => ({
                                id: String(ejemplar.idEjemplar),
                                etiqueta: `${ejemplar.libro.titulo} - ${ejemplar.codigoInventario}`,
                                detalle: `Inventario: ${ejemplar.codigoInventario} | Disponible`,
                            }))}
                        placeholder="Escribí el título o código de inventario..."
                        onChange={(valor) =>
                            setNuevoPrestamo((anterior) => ({
                                ...anterior,
                                idEjemplar: valor,
                            }))
                        }
                    />
                    <label htmlFor="fecha-devolucion">
                        Fecha de devolución prevista *
                    </label>
                    <input
                        id="fecha-devolucion"
                        type="date"
                        required
                        value={nuevoPrestamo.fechaDevolucionPrevista}
                        onChange={(e) =>
                            setNuevoPrestamo({
                                ...nuevoPrestamo,
                                fechaDevolucionPrevista: e.target.value,
                            })
                        }
                    />
                    <button
                        type="submit"
                        className="boton-principal"
                        disabled={guardandoPrestamo}
                    >
                        {guardandoPrestamo
                            ? 'Guardando...'
                            : 'Guardar préstamo'}
                    </button>
                </form>
            )}
            {prestamos.length === 0 ? (
                <p>No hay préstamos registrados.</p>
            ) : (
                <div className="tarjetas">
                                {esLector && (
                                    <article className="tarjeta tarjeta-acceso">
                                        <span aria-hidden="true">📑</span>
                                        <h3>Mis reservas</h3>
                                        <p>Consultá tus solicitudes y los libros listos para retirar.</p>
                                        <button
                                            className="boton-principal"
                                            onClick={() => setVista('mis-reservas')}
                                        >
                                            Ver mis reservas
                                        </button>
                                    </article>
                                )}

                    {prestamos.map((prestamo) => (
                        <article className="tarjeta" key={prestamo.idPrestamo}>
                            <span>🔄</span>
                            <h3>Préstamo #{prestamo.idPrestamo}</h3>
                            <p>
                                <strong>Usuario:</strong>{' '}
                                {prestamo.usuario.nombre} {prestamo.usuario.apellido}
                            </p>
                            <p>
                                <strong>Ejemplar:</strong>{' '}
                                {prestamo.ejemplar.codigoInventario}
                            </p>
                            <p>
                                <strong>Fecha:</strong> {prestamo.fechaPrestamo}
                            </p>
                            <p>
                                <strong>Devolución prevista:</strong>{' '}
                                {prestamo.fechaDevolucionPrevista}
                            </p>
                            <p>
                                <strong>Estado:</strong> {prestamo.estado}
                            </p>
                            {prestamo.estado === 'ACTIVO' && (
                                <button
                                    type="button"
                                    className="boton-principal"
                                    onClick={() => registrarDevolucion(prestamo.idPrestamo)}
                                >
                                    Registrar devolución
                                </button>
                            )}
                        </article>
                    ))}
                </div>
            )}
        </section>
    )
    const mostrarDashboard = () => {
        const ahora = new Date()
        const fechaLocal = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
        const hoy = fechaLocal(ahora)
        const limite = new Date(ahora)
        limite.setDate(limite.getDate() + 7)
        const activos = prestamos.filter(p => p.estado === 'ACTIVO')
        const vencidos = activos.filter(p => p.fechaDevolucionPrevista < hoy)
        const proximos = activos.filter(p => p.fechaDevolucionPrevista >= hoy && p.fechaDevolucionPrevista <= fechaLocal(limite)).sort((a,b) => a.fechaDevolucionPrevista.localeCompare(b.fechaDevolucionPrevista))
        const recientes = [...prestamos].sort((a,b) => b.fechaPrestamo.localeCompare(a.fechaPrestamo) || b.idPrestamo - a.idPrestamo).slice(0,5)
        const indicadores = [
            ['▤', 'Títulos registrados', libros.filter(l => l.activo).length, ''],
            ['▥', 'Ejemplares activos', ejemplares.filter(e => e.activo).length, ''],
            ['✓', 'Disponibles', ejemplares.filter(e => e.activo && e.estado === 'DISPONIBLE').length, 'verde'],
            ['↔', 'Préstamos activos', activos.length, ''],
            ['!', 'Préstamos vencidos', vencidos.length, 'alerta'],
            ['♙', 'Lectores activos', usuarios.filter(u => u.activo && u.rol === 'LECTOR').length, ''],
        ] as const
        const listado = (datos: Prestamo[], vacio: string, fecha: 'fechaPrestamo' | 'fechaDevolucionPrevista') => datos.length ? (
            <ul className="dashboard-lista">{datos.slice(0,5).map(p => <li key={p.idPrestamo}><div><strong>{p.ejemplar.libro.titulo}</strong><small>{p.usuario.nombre} {p.usuario.apellido} · {p.ejemplar.codigoInventario}</small></div><time dateTime={p[fecha]}>{p[fecha].split('-').reverse().join('/')}</time></li>)}</ul>
        ) : <p className="dashboard-vacio">{vacio}</p>
        return (
            <section className="panel dashboard">
                <div className="dashboard-cabecera"><div><p className="etiqueta">GESTIÓN BIBLIOTECARIA</p><h2>Panel de control</h2><p>Resumen general de la biblioteca escolar</p></div><span>{ahora.toLocaleDateString('es-AR', {day:'numeric', month:'long', year:'numeric'})}</span></div>
                <div className="dashboard-indicadores">{indicadores.map(([icono, nombre, valor, tono]) => <article key={nombre} className={`dashboard-kpi ${tono}`}><span aria-hidden="true">{icono}</span><small>{nombre}</small><strong>{valor.toLocaleString('es-AR')}</strong></article>)}</div>
                <h3>Operaciones rápidas</h3>
                <div className="dashboard-acciones">
                    <button onClick={() => setVista('reservas')}>
                        Gestionar reservas
                    </button>

                    <button onClick={() => { setMostrarFormularioPrestamo(true); setMensajePrestamo(''); setVista('prestamos') }}>＋ Nuevo préstamo</button>
                    <button onClick={() => { setMostrarFormularioPrestamo(false); setVista('prestamos') }}>↩ Registrar devolución</button>
                    <button onClick={() => { setUsuarioEditando(null); setMostrarFormularioUsuario(true); setMensajeUsuario(''); setVista('usuarios') }}>＋ Registrar lector</button>
                    <button onClick={() => setVista('catalogo')}>▤ Gestionar inventario</button>
                </div>
                <h3>Actividad bibliotecaria</h3>
                <div className="dashboard-actividad">
                    <article><h4>Préstamos vencidos <span>{vencidos.length}</span></h4>{listado(vencidos, 'No hay préstamos vencidos.', 'fechaDevolucionPrevista')}<button className="boton-secundario" onClick={() => setVista('prestamos')}>Ver préstamos</button></article>
                    <article><h4>Próximos vencimientos <span>{proximos.length}</span></h4>{listado(proximos, 'No hay vencimientos en los próximos siete días.', 'fechaDevolucionPrevista')}</article>
                    <article><h4>Últimos préstamos</h4>{listado(recientes, 'Todavía no hay préstamos registrados.', 'fechaPrestamo')}</article>
                </div>
            </section>
        )
    }
    return (
        <div className="app">
            <header className="encabezado">
                <div>
                    <h1>BiblioGest</h1>
                    <p>Sistema Web de Gestión de Biblioteca</p>
                </div>
                <nav>
                    <button aria-current={vista === 'inicio' ? 'page' : undefined} onClick={() => setVista('inicio')}>
                        {esBibliotecario ? 'Panel de control' : 'Inicio'}
                    </button>
                    <button aria-current={vista === 'catalogo' ? 'page' : undefined} onClick={() => setVista('catalogo')}>
                        Catálogo
                    </button>
                    {esLector && (
                        <>
                            <button aria-current={vista === 'mis-prestamos' ? 'page' : undefined}
                                onClick={() => setVista('mis-prestamos')}>
                                Mis préstamos
                            </button>

                            <button aria-current={vista === 'mis-reservas' ? 'page' : undefined}
                                onClick={() => setVista('mis-reservas')}>
                                Mis reservas
                            </button>
                        </>
                    )}
                    {esBibliotecario && (
                        <>
                            <button onClick={() => setVista('prestamos')}>
                                Préstamos
                            </button>
                            <button aria-current={vista === 'reservas' ? 'page' : undefined}
                                onClick={() => setVista('reservas')}>
                                Reservas
                            </button>

<button onClick={() => setVista('solicitudes')}>
                                Solicitudes
                            </button>
                            <button onClick={() => setVista('usuarios')}>
                                Usuarios
                            </button>
                        </>
                    )}
                    {sesion && (
                        <CentroNotificaciones
                            key={sesion.autorizacion}
                            autorizacion={sesion.autorizacion}
                        />
                    )}

                    {sesion ? (
                        <button onClick={cerrarSesion}>
                            Cerrar sesión ({sesion.rol})
                        </button>
                    ) : (
                        <button onClick={() => setVista('login')}>
                            Iniciar sesión
                        </button>
                    )}
                </nav>
            </header>
            <main className="contenido">
                {vista === 'mis-reservas' && esLector && sesion && (
                    <Reservas
                        key="lector"
                        autorizacion={sesion.autorizacion}
                        administrador={false}
                        onCambio={() => setRevisionDatos(valor => valor + 1)}
                    />
                )}

                {vista === 'reservas' && esBibliotecario && sesion && (
                    <Reservas
                        key="bibliotecario"
                        autorizacion={sesion.autorizacion}
                        administrador={true}
                        onCambio={() => setRevisionDatos(valor => valor + 1)}
                    />
                )}

                {vista === 'login' && !sesion && (
                    <Login onLogin={iniciarSesion} />
                )}
                {vista === 'registro' && !sesion && (
                    <Registro onVolverLogin={() => setVista('login')} />
                )}
                {vista === 'solicitudes' && esBibliotecario && sesion && (
                    <SolicitudesPendientes
                        autorizacion={sesion.autorizacion}
                        onCambio={() => setRevisionDatos(valor => valor + 1)}
                    />
                )}
                {cargando && (
                    <section className="panel">
                        <p>Cargando información...</p>
                    </section>
                )}
                {error && (
                    <section className="panel">
                        <p className="mensaje-error">{error}</p>
                    </section>
                )}
                {!cargando && !error && vista === 'inicio' && esBibliotecario && mostrarDashboard()}
                {!cargando && !error && vista === 'inicio' && !esBibliotecario && (
                    <>
                        <section className="bienvenida">
                            <p className="etiqueta">BIBLIOTECA ESCOLAR</p>
                            <h2>Bienvenido a BiblioGest</h2>
                            <p>
                                Una herramienta para organizar el catálogo, consultar ejemplares
                                disponibles y gestionar los préstamos de la biblioteca.
                            </p>
                            <button
                                className="boton-principal"
                                onClick={() => setVista('catalogo')}
                            >
                                Ver catálogo
                            </button>
                        </section>
                        <section className="panel">
                            <div className="indicadores">
                                <div><small>Títulos del catálogo</small><strong>{libros.length.toLocaleString('es-AR')}</strong></div>
                                <div><small>Ejemplares activos</small><strong>{ejemplares.filter(e => e.activo).length.toLocaleString('es-AR')}</strong></div>
                                <div><small>Disponibles</small><strong>{ejemplares.filter(e => e.activo && e.estado === 'DISPONIBLE').length.toLocaleString('es-AR')}</strong></div>
                                {esBibliotecario && <div><small>Préstamos activos</small><strong>{prestamos.filter(p => p.estado === 'ACTIVO').length}</strong></div>}
                            </div>
                            <h2>Accesos principales</h2>
                            <div className="tarjetas">
                                {esLector && (
                                    <article className="tarjeta tarjeta-acceso">
                                        <span aria-hidden="true">📑</span>
                                        <h3>Mis reservas</h3>
                                        <p>Consultá tus solicitudes y los libros listos para retirar.</p>
                                        <button
                                            className="boton-principal"
                                            onClick={() => setVista('mis-reservas')}
                                        >
                                            Ver mis reservas
                                        </button>
                                    </article>
                                )}

                                <article
                                    className="tarjeta tarjeta-acceso"
                                    onClick={() => setVista('catalogo')}
                                >
                                    <span>📚</span>
                                    <h3>Catálogo</h3>
                                    <p>Consultar los libros disponibles.</p>
                                </article>
                                {esLector && (
                                    <article className="tarjeta tarjeta-acceso" onClick={() => setVista('mis-prestamos')}>
                                        <span>📖</span>
                                        <h3>Mis préstamos</h3>
                                        <p>Consultar tus préstamos e historial de devoluciones.</p>
                                    </article>
                                )}
                                {esBibliotecario && (
                                    <>
                                        <article
                                            className="tarjeta tarjeta-acceso"
                                            onClick={() => setVista('prestamos')}
                                        >
                                            <span>🔄</span>
                                            <h3>Préstamos</h3>
                                            <p>Consultar préstamos y devoluciones.</p>
                                        </article>
                                        <article
                                            className="tarjeta tarjeta-acceso"
                                            onClick={() => setVista('usuarios')}
                                        >
                                            <span>👥</span>
                                            <h3>Usuarios</h3>
                                            <p>Consultar los lectores activos.</p>
                                        </article>
                                    </>
                                )}
                            </div>
                        </section>
                    </>
                )}
                {!cargando && !error && vista === 'catalogo' && mostrarCatalogo()}
                {!cargando && !error && esLector && vista === 'mis-prestamos' && mostrarMisPrestamos()}
                {!cargando && !error && esBibliotecario && vista === 'prestamos' && mostrarPrestamos()}
                {!cargando && !error && esBibliotecario && vista === 'usuarios' && mostrarUsuarios()}
            </main>
            <footer>
                <p>BiblioGest - Sistema de Gestión de Biblioteca</p>
            </footer>
        </div>
    )
}
function App() {
    return (
        <BrowserRouter>
            <AppContenido />
        </BrowserRouter>
    )
}
export default App