

import { useEffect, useState } from 'react'

import type { FormEvent } from 'react'

import { BrowserRouter, useLocation, useNavigate } from 'react-router-dom'

import Login from './components/Login.tsx'

import GestionAutoresCategorias from './components/GestionAutoresCategorias.tsx'

import type { Sesion } from './components/Login.tsx'

import { apiUrl } from './services/api.ts'

import './App.css'





interface Autor {

    idAutor: number

    nombre: string

    apellido: string

}



interface Categoria {

    idCategoria: number

    nombre: string

    descripcion?: string | null

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

    ubicacion: string | null

    estadoFisico: string | null

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

    vencido: boolean

    estado: string

    usuario: Usuario

    ejemplar: {

        idEjemplar: number

        codigoInventario: string

        estado: string

        libro: Libro

    }

}





type Vista = 'inicio' | 'catalogo' | 'prestamos' | 'mis-prestamos' | 'usuarios' | 'autores' | 'categorias' | 'login'

const rutas: Record<Vista, string> = {
    inicio: '/',
    catalogo: '/catalogo',
    prestamos: '/prestamos',
    'mis-prestamos': '/mis-prestamos',
    usuarios: '/usuarios',
    autores: '/autores',
    categorias: '/categorias',
    login: '/login',
}

const libroVacio = {
    titulo: '', isbn: '', editorial: '', anioPublicacion: '', idCategoria: '', idsAutores: [] as string[],
}

const ejemplarVacio = { idLibro: '', codigoInventario: '', ubicacion: '', estadoFisico: '' }

function obtenerEjemplares(sesion: Sesion | null) {
    return sesion?.rol === 'BIBLIOTECARIO'
        ? fetch(apiUrl('/api/ejemplares'), {
            headers: { Authorization: sesion.autorizacion },
        })
        : fetch(apiUrl('/api/ejemplares/activos'))
}





function AppContenido() {

    const navigate = useNavigate()
    const location = useLocation()
    const rutaActual = location.pathname.replace(/\/$/, '') || '/'
    const vista: Vista =
        (Object.entries(rutas).find(([, ruta]) => ruta === rutaActual)?.[0] as Vista | undefined)
        ?? 'inicio'

    const setVista = (nuevaVista: Vista) => {
        navigate(rutas[nuevaVista])
    }



    const [sesion, setSesion] = useState<Sesion | null>(null)



    const esBibliotecario = sesion?.rol === 'BIBLIOTECARIO'
    const esLector = sesion?.rol === 'LECTOR'

    // La URL identifica la pantalla. Las rutas administrativas se restringen por rol.
    useEffect(() => {
        if (rutaActual !== rutas[vista] ||
            ((vista === 'prestamos' || vista === 'usuarios' || vista === 'autores' || vista === 'categorias') && !esBibliotecario) ||
            (vista === 'mis-prestamos' && !esLector) ||
            (vista === 'login' && sesion)) {
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
        setEjemplarEditando(null)
        setNuevoEjemplar(ejemplarVacio)
        setConfirmandoDesactivacionEjemplar(null)
        setMensajeEjemplar('')
        setErrorEjemplar('')
        setMostrarFormularioUsuario(false)
        setMostrarFormularioPrestamo(false)
        setUsuarioEditando(null)
        setVista('inicio')

    }

    const [libros, setLibros] = useState<Libro[]>([])

    const [ejemplares, setEjemplares] = useState<Ejemplar[]>([])

    const [usuarios, setUsuarios] = useState<Usuario[]>([])

    const [prestamos, setPrestamos] = useState<Prestamo[]>([])
    const [filtroPrestamos, setFiltroPrestamos] = useState<'TODOS' | 'ACTIVOS' | 'VENCIDOS' | 'DEVUELTOS'>('TODOS')
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

    const [errorEjemplar, setErrorEjemplar] = useState('')

    const [ejemplarEditando, setEjemplarEditando] = useState<number | null>(null)

    const [confirmandoDesactivacionEjemplar, setConfirmandoDesactivacionEjemplar] = useState<number | null>(null)

    const [desactivandoEjemplar, setDesactivandoEjemplar] = useState(false)



    const [nuevoEjemplar, setNuevoEjemplar] = useState(ejemplarVacio)

    const [guardandoLibro, setGuardandoLibro] = useState(false)

    const [mensajeLibro, setMensajeLibro] = useState('')

    const [errorLibro, setErrorLibro] = useState('')

    const [libroEditando, setLibroEditando] = useState<number | null>(null)

    const [confirmandoDesactivacion, setConfirmandoDesactivacion] = useState<number | null>(null)

    const [desactivandoLibro, setDesactivandoLibro] = useState(false)



    const [nuevoLibro, setNuevoLibro] = useState(libroVacio)





    useEffect(() => {

        let cancelado = false



        const cargarDatos = async () => {

            setCargando(true)

            setError('')



            try {

                const [respuestaLibros, respuestaEjemplares,

                    respuestaCategorias, respuestaAutores] = await Promise.all([

                    fetch(apiUrl('/api/libros/activos')),

                    obtenerEjemplares(sesion),

                    fetch(apiUrl('/api/categorias')),

                    fetch(apiUrl('/api/autores')),

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

                            fetch(apiUrl('/api/usuarios/activos'), {

                                headers: {

                                    Authorization: sesion.autorizacion,

                                },

                            }),

                            fetch(apiUrl('/api/prestamos'), {

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

    }, [sesion])



    // Solo consulta los préstamos del lector autenticado cuando abre su pantalla.
    useEffect(() => {
        if (vista !== 'mis-prestamos' || !esLector || !sesion) return

        const controlador = new AbortController()
        const cargarMisPrestamos = async () => {
            setCargandoMisPrestamos(true)
            setErrorMisPrestamos('')
            try {
                const respuesta = await fetch(apiUrl('/api/prestamos/mis-prestamos'), {
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
                    {misPrestamos.map((prestamo) => (
                        <article className="tarjeta" key={prestamo.idPrestamo}>
                            <span>📖</span>
                            <h3>{prestamo.ejemplar.libro.titulo}</h3>
                            <p><strong>Inventario:</strong> {prestamo.ejemplar.codigoInventario}</p>
                            <p><strong>Fecha del préstamo:</strong> {prestamo.fechaPrestamo}</p>
                            <p><strong>Devolución prevista:</strong> {prestamo.fechaDevolucionPrevista}</p>
                            <p><strong>Devolución real:</strong> {prestamo.fechaDevolucionReal ?? 'Pendiente'}</p>
                            <p><strong>Estado:</strong> <span className={prestamo.estado === 'DEVUELTO' ? 'estado-devuelto' : 'estado-activo'}>{prestamo.estado}</span></p>
                            {prestamo.vencido && <p className="mensaje-error" role="status">Vencido</p>}
                        </article>
                    ))}
                </div>
            )}
        </section>
    )

    const recargarCatalogo = async () => {
        const [respuestaLibros, respuestaEjemplares] = await Promise.all([
            fetch(apiUrl('/api/libros/activos')),
            obtenerEjemplares(sesion),
        ])
        if (!respuestaLibros.ok || !respuestaEjemplares.ok) {
            throw new Error('La operación se completó, pero no se pudo actualizar el catálogo.')
        }
        setLibros(await respuestaLibros.json() as Libro[])
        setEjemplares(await respuestaEjemplares.json() as Ejemplar[])
    }

    const iniciarEdicionLibro = (libro: Libro) => {
        setLibroEditando(libro.idLibro)
        setNuevoLibro({
            titulo: libro.titulo,
            isbn: libro.isbn || '',
            editorial: libro.editorial || '',
            anioPublicacion: libro.anioPublicacion?.toString() || '',
            idCategoria: libro.categoria.idCategoria.toString(),
            idsAutores: libro.autores.map((autor) => autor.idAutor.toString()),
        })
        setErrorLibro('')
        setMensajeLibro('')
        setMostrarFormulario(true)
    }

    const desactivarLibro = async (idLibro: number) => {
        if (desactivandoLibro) return
        setDesactivandoLibro(true)
        setErrorLibro('')
        setMensajeLibro('')
        try {
            const respuesta = await fetch(apiUrl(`/api/libros/${idLibro}`), {
                method: 'DELETE',
                headers: { Authorization: sesion?.autorizacion ?? '' },
            })
            if (!respuesta.ok) {
                let detalle = ''
                try { detalle = (await respuesta.json() as { mensaje?: string }).mensaje || '' } catch { /* sin cuerpo */ }
                throw new Error(detalle || `No se pudo desactivar el libro (HTTP ${respuesta.status}).`)
            }
            setConfirmandoDesactivacion(null)
            if (libroEditando === idLibro) {
                setMostrarFormulario(false)
                setLibroEditando(null)
                setNuevoLibro(libroVacio)
            }
            setMensajeLibro('Libro desactivado correctamente.')
            await recargarCatalogo()
        } catch (error) {
            setErrorLibro(error instanceof Error ? error.message : 'No se pudo conectar con el servidor.')
        } finally {
            setDesactivandoLibro(false)
        }
    }

    const guardarLibro = async (e: FormEvent<HTMLFormElement>) => {

        e.preventDefault()



        if (guardandoLibro) return

        const anio = nuevoLibro.anioPublicacion ? Number(nuevoLibro.anioPublicacion) : null
        const anioActual = Number(new Intl.DateTimeFormat('en-US', {
            year: 'numeric', timeZone: 'America/Argentina/Buenos_Aires',
        }).format(new Date()))
        if (!nuevoLibro.titulo.trim() || nuevoLibro.titulo.trim().length > 200
            || nuevoLibro.isbn.trim().length > 20 || nuevoLibro.editorial.trim().length > 150
            || !nuevoLibro.idCategoria || nuevoLibro.idsAutores.length === 0
            || (anio !== null && (!Number.isInteger(anio) || anio < 1450 || anio > anioActual))) {
            setErrorLibro('Revisá título, categoría, autores y año de publicación (1450 hasta el año actual).')
            return
        }



        setGuardandoLibro(true)

        setMensajeLibro('')

        setErrorLibro('')



        try {

            const datos = {

                titulo: nuevoLibro.titulo.trim(),

                isbn: nuevoLibro.isbn.trim() || null,

                editorial: nuevoLibro.editorial.trim() || null,

                anioPublicacion: nuevoLibro.anioPublicacion

                    ? Number(nuevoLibro.anioPublicacion)

                    : null,

                categoria: {

                    idCategoria: Number(nuevoLibro.idCategoria),

                },

                autores: nuevoLibro.idsAutores.map((idAutor) => ({ idAutor: Number(idAutor) })),

            }



            const respuesta = await fetch(libroEditando === null
                ? apiUrl('/api/libros')
                : apiUrl(`/api/libros/${libroEditando}`), {

                method: libroEditando === null ? 'POST' : 'PUT',

                headers: {

                    'Content-Type': 'application/json',

                    Authorization: sesion?.autorizacion ?? '',

                },

                body: JSON.stringify(datos),

            })



            if (!respuesta.ok) {
                let detalle = ''
                try { detalle = (await respuesta.json() as { mensaje?: string }).mensaje || '' } catch { /* sin cuerpo */ }
                throw new Error(detalle || `No se pudo guardar el libro (HTTP ${respuesta.status}).`)

            }



            setNuevoLibro(libroVacio)

            setLibroEditando(null)



            setBusqueda('')

            setMostrarFormulario(false)

            setMensajeLibro(libroEditando === null ? 'Libro registrado correctamente.' : 'Libro actualizado correctamente.')

            await recargarCatalogo()

        } catch (error) {

            setErrorLibro(

                error instanceof Error ? error.message : 'Ocurrió un error.'

            )

        } finally {

            setGuardandoLibro(false)

        }

    }



    const iniciarEdicionEjemplar = (ejemplar: Ejemplar) => {
        setEjemplarEditando(ejemplar.idEjemplar)
        setNuevoEjemplar({
            idLibro: ejemplar.libro.idLibro.toString(),
            codigoInventario: ejemplar.codigoInventario,
            ubicacion: ejemplar.ubicacion || '',
            estadoFisico: ejemplar.estadoFisico || '',
        })
        setMensajeEjemplar('')
        setErrorEjemplar('')
        setMostrarFormularioEjemplar(true)
    }

    const desactivarEjemplar = async (idEjemplar: number) => {
        if (desactivandoEjemplar) return
        setDesactivandoEjemplar(true)
        setMensajeEjemplar('')
        setErrorEjemplar('')
        try {
            const respuesta = await fetch(apiUrl(`/api/ejemplares/${idEjemplar}/desactivar`), {
                method: 'PUT',
                headers: { Authorization: sesion?.autorizacion ?? '' },
            })
            if (!respuesta.ok) {
                let detalle = ''
                try { detalle = (await respuesta.json() as { mensaje?: string }).mensaje || '' } catch { /* sin cuerpo */ }
                throw new Error(detalle || `No se pudo desactivar el ejemplar (HTTP ${respuesta.status}).`)
            }
            setConfirmandoDesactivacionEjemplar(null)
            if (ejemplarEditando === idEjemplar) {
                setMostrarFormularioEjemplar(false)
                setEjemplarEditando(null)
                setNuevoEjemplar(ejemplarVacio)
            }
            setMensajeEjemplar('Ejemplar desactivado correctamente.')
            await recargarCatalogo()
        } catch (error) {
            setErrorEjemplar(error instanceof Error ? error.message : 'No se pudo conectar con el servidor.')
        } finally {
            setDesactivandoEjemplar(false)
        }
    }

    const guardarEjemplar = async (e: FormEvent<HTMLFormElement>) => {

        e.preventDefault()



        if (guardandoEjemplar) return

        const ubicacion = nuevoEjemplar.ubicacion.trim()
        const codigoInventario = nuevoEjemplar.codigoInventario.trim()
        if (!nuevoEjemplar.idLibro || !codigoInventario || codigoInventario.length > 50
            || !ubicacion || ubicacion.length > 120
            || !['BUENO', 'REGULAR', 'DANADO'].includes(nuevoEjemplar.estadoFisico)) {
            setErrorEjemplar('Completá libro, código, ubicación (hasta 120 caracteres) y estado físico válido.')
            return
        }



        setGuardandoEjemplar(true)

        setMensajeEjemplar('')

        setErrorEjemplar('')



        try {

            const datos = {

                codigoInventario,

                ubicacion,

                estadoFisico: nuevoEjemplar.estadoFisico,

                libro: {

                    idLibro: Number(nuevoEjemplar.idLibro),

                },

            }



            const respuesta = await fetch(ejemplarEditando === null
                ? apiUrl('/api/ejemplares')
                : apiUrl(`/api/ejemplares/${ejemplarEditando}`), {

                method: ejemplarEditando === null ? 'POST' : 'PUT',

                headers: {

                    'Content-Type': 'application/json',

                    Authorization: sesion?.autorizacion ?? '',

                },

                body: JSON.stringify(datos),

            })



            if (!respuesta.ok) {
                let detalle = ''
                try { detalle = (await respuesta.json() as { mensaje?: string }).mensaje || '' } catch { /* sin cuerpo */ }
                throw new Error(detalle || `No se pudo guardar el ejemplar (HTTP ${respuesta.status}).`)

            }



            setNuevoEjemplar(ejemplarVacio)

            setEjemplarEditando(null)



            setMostrarFormularioEjemplar(false)

            setMensajeEjemplar(ejemplarEditando === null
                ? 'Ejemplar registrado correctamente.' : 'Ejemplar actualizado correctamente.')

            await recargarCatalogo()

        } catch (error) {

            setErrorEjemplar(

                error instanceof Error ? error.message : 'Ocurrió un error.'

            )

        } finally {

            setGuardandoEjemplar(false)

        }

    }



    const librosFiltrados = libros.filter((libro) => {

        const texto = busqueda.toLowerCase()



        const coincideTitulo = libro.titulo.toLowerCase().includes(texto)



        const coincideCategoria = libro.categoria.nombre

            .toLowerCase()

            .includes(texto)



        const coincideAutor = libro.autores.some((autor) =>

            `${autor.nombre} ${autor.apellido}`

                .toLowerCase()

                .includes(texto)

        )



        return coincideTitulo || coincideCategoria || coincideAutor

    })



    const mostrarCatalogo = () => (

        <section className="panel">

            <h2>Catálogo de libros</h2>

            {esBibliotecario && (
                <button

                    className="boton-principal"

                    onClick={() => {

                        setMostrarFormulario(!mostrarFormulario)

                        setLibroEditando(null)

                        setNuevoLibro(libroVacio)

                        setMensajeLibro('')

                        setErrorLibro('')

                    }}

                >

                    {mostrarFormulario ? 'Cancelar' : '+ Agregar libro'}

                </button>
            )}

            {esBibliotecario && mostrarFormularioEjemplar && (

                <form className="formulario-libro" onSubmit={guardarEjemplar}>

                    <h3>{ejemplarEditando === null ? 'Registrar nuevo ejemplar' : 'Editar ejemplar'}</h3>



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

                    <label htmlFor="ubicacion-ejemplar">Ubicación *</label>

                    <input id="ubicacion-ejemplar" type="text" required maxLength={120}
                        value={nuevoEjemplar.ubicacion}
                        onChange={(e) => setNuevoEjemplar({ ...nuevoEjemplar, ubicacion: e.target.value })} />

                    <label htmlFor="estado-fisico-ejemplar">Estado físico *</label>

                    <select id="estado-fisico-ejemplar" required value={nuevoEjemplar.estadoFisico}
                        onChange={(e) => setNuevoEjemplar({ ...nuevoEjemplar, estadoFisico: e.target.value })}>
                        <option value="" disabled hidden>Seleccionar estado físico</option>
                        <option value="BUENO">Bueno</option>
                        <option value="REGULAR">Regular</option>
                        <option value="DANADO">Dañado</option>
                    </select>



                    <button

                        type="submit"

                        className="boton-principal"

                        disabled={guardandoEjemplar}

                    >

                        {guardandoEjemplar ? 'Guardando...' : ejemplarEditando === null
                            ? 'Guardar ejemplar' : 'Guardar cambios'}

                    </button>

                </form>

            )}

            {esBibliotecario && (
                <button

                    className="boton-principal"

                    onClick={() => {

                        setMostrarFormularioEjemplar(!mostrarFormularioEjemplar)

                        setEjemplarEditando(null)

                        setNuevoEjemplar(ejemplarVacio)

                        setMensajeEjemplar('')

                        setErrorEjemplar('')

                    }}

                >

                    {mostrarFormularioEjemplar ? 'Cancelar ejemplar' : '+ Agregar ejemplar'}

                </button>
            )}



            {mensajeEjemplar && <p role="status">{mensajeEjemplar}</p>}

            {errorEjemplar && <p className="mensaje-error" role="alert">{errorEjemplar}</p>}



            {mensajeLibro && <p role="status">{mensajeLibro}</p>}

            {errorLibro && <p className="mensaje-error" role="alert">{errorLibro}</p>}

            <div className="buscador">

                {esBibliotecario && mostrarFormulario && (

                    <form className="formulario-libro" onSubmit={guardarLibro}>

                        <h3>{libroEditando === null ? 'Registrar nuevo libro' : 'Editar libro'}</h3>



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

                            min="1450"

                            max={new Intl.DateTimeFormat('en-US', {
                                year: 'numeric', timeZone: 'America/Argentina/Buenos_Aires',
                            }).format(new Date())}

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



                        <fieldset className="seleccion-autores">
                            <legend>Autores * (seleccioná al menos uno)</legend>
                            {autores.map((autor) => {
                                const id = autor.idAutor.toString()
                                return <label htmlFor={`autor-${id}`} key={id}>
                                    <input id={`autor-${id}`} type="checkbox" checked={nuevoLibro.idsAutores.includes(id)}
                                        onChange={(e) => setNuevoLibro({
                                            ...nuevoLibro,
                                            idsAutores: e.target.checked
                                                ? [...nuevoLibro.idsAutores, id]
                                                : nuevoLibro.idsAutores.filter((seleccionado) => seleccionado !== id),
                                        })} />
                                    {autor.nombre} {autor.apellido}
                                </label>
                            })}
                        </fieldset>



                        <button

                            type="submit"

                            className="boton-principal"

                            disabled={guardandoLibro}

                        >

                            {guardandoLibro ? 'Guardando...' : libroEditando === null ? 'Guardar libro' : 'Guardar cambios'}

                        </button>

                    </form>

                )}

                <input

                    type="text"

                    placeholder="Buscar por título, autor o categoría..."

                    value={busqueda}

                    onChange={(e) => setBusqueda(e.target.value)}

                />

            </div>



            {librosFiltrados.length === 0 ? (

                <p>No hay libros disponibles.</p>

            ) : (

                <div className="tarjetas">

                    {librosFiltrados.map((libro) => (

                        <article className="tarjeta" key={libro.idLibro}>

                            <span>📚</span>

                            <h3>{libro.titulo}</h3>



                            <p>

                                <strong>Autor:</strong>{' '}

                                {libro.autores.length > 0

                                    ? libro.autores

                                        .map((autor) => `${autor.nombre} ${autor.apellido}`)

                                        .join(', ')

                                    : 'Sin autor'}

                            </p>



                            <p>

                                <strong>Categoría:</strong> {libro.categoria.nombre}

                            </p>



                            <p>

                                <strong>Año:</strong>{' '}

                                {libro.anioPublicacion ?? 'Sin información'}

                            </p>



                            <p>

                                <strong>Editorial:</strong>{' '}

                                {libro.editorial || 'Sin información'}

                            </p>



                            <p>

                                <strong>Ejemplares disponibles:</strong>{' '}

                                {

                                    ejemplares.filter(

                                        (ejemplar) =>

                                            ejemplar.libro.idLibro === libro.idLibro &&

                                            ejemplar.activo === true &&

                                            libro.activo === true &&

                                            ejemplar.estado === 'DISPONIBLE'

                                    ).length

                                }

                            </p>

                            {esBibliotecario && (
                                <div className="acciones-libro">
                                    <button type="button" onClick={() => iniciarEdicionLibro(libro)}
                                        disabled={guardandoLibro || desactivandoLibro}>Editar libro</button>
                                    {confirmandoDesactivacion === libro.idLibro ? (
                                        <div className="confirmacion-libro">
                                            <p>¿Desactivar este libro? Se conservarán sus ejemplares e historial.</p>
                                            <button type="button" onClick={() => { void desactivarLibro(libro.idLibro) }}
                                                disabled={desactivandoLibro}>Confirmar desactivación</button>
                                            <button type="button" onClick={() => setConfirmandoDesactivacion(null)}
                                                disabled={desactivandoLibro}>Cancelar</button>
                                        </div>
                                    ) : (
                                        <button type="button" onClick={() => setConfirmandoDesactivacion(libro.idLibro)}
                                            disabled={guardandoLibro || desactivandoLibro}>Desactivar libro</button>
                                    )}
                                </div>
                            )}

                            {esBibliotecario && (
                                <div className="ejemplares-libro">
                                    <h4>Ejemplares</h4>
                                    {ejemplares.filter((ejemplar) => ejemplar.libro.idLibro === libro.idLibro)
                                        .map((ejemplar) => {
                                            const prestado = prestamos.some((prestamo) =>
                                                prestamo.ejemplar.idEjemplar === ejemplar.idEjemplar
                                                && prestamo.fechaDevolucionReal === null)
                                            return <div className="ejemplar-detalle" key={ejemplar.idEjemplar}>
                                                <p><strong>ID:</strong> {ejemplar.idEjemplar} · <strong>Código:</strong> {ejemplar.codigoInventario}</p>
                                                <p><strong>Ubicación:</strong> {ejemplar.ubicacion || 'Sin registrar'}</p>
                                                <p><strong>Estado físico:</strong> {ejemplar.estadoFisico || 'Sin registrar'}</p>
                                                <p><strong>Circulación:</strong> {ejemplar.estado} · {ejemplar.activo ? 'Activo' : 'Inactivo'}</p>
                                                <div className="acciones-libro">
                                                    <button type="button" onClick={() => iniciarEdicionEjemplar(ejemplar)}
                                                        disabled={prestado || guardandoEjemplar || desactivandoEjemplar}>Editar ejemplar</button>
                                                    {ejemplar.activo && (confirmandoDesactivacionEjemplar === ejemplar.idEjemplar ? (
                                                        <div className="confirmacion-libro">
                                                            <p>¿Desactivar este ejemplar? Se conservará su historial.</p>
                                                            <button type="button" onClick={() => { void desactivarEjemplar(ejemplar.idEjemplar) }}
                                                                disabled={prestado || desactivandoEjemplar}>Confirmar desactivación</button>
                                                            <button type="button" onClick={() => setConfirmandoDesactivacionEjemplar(null)}
                                                                disabled={desactivandoEjemplar}>Cancelar</button>
                                                        </div>
                                                    ) : (
                                                        <button type="button" onClick={() => setConfirmandoDesactivacionEjemplar(ejemplar.idEjemplar)}
                                                            disabled={prestado || guardandoEjemplar || desactivandoEjemplar}>Desactivar ejemplar</button>
                                                    ))}
                                                </div>
                                            </div>
                                        })}
                                    {ejemplares.every((ejemplar) => ejemplar.libro.idLibro !== libro.idLibro)
                                        && <p>No hay ejemplares registrados.</p>}
                                </div>
                            )}

                        </article>

                    ))}

                </div>

            )}

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



            const respuesta = await fetch(apiUrl('/api/usuarios'), {

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

                apiUrl('/api/usuarios/activos'),

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

                apiUrl(`/api/usuarios/${usuarioEditando}`),

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

                apiUrl('/api/usuarios/activos'),

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

                apiUrl(`/api/usuarios/${idUsuario}/desactivar`),



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

                apiUrl('/api/usuarios/activos'),

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

                fechaDevolucionPrevista: nuevoPrestamo.fechaDevolucionPrevista,

            }



            const respuesta = await fetch(apiUrl('/api/prestamos'), {

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

                fetch(apiUrl('/api/prestamos'), {

                    headers: {

                        Authorization: sesion?.autorizacion ?? '',

                    },

                }),

                obtenerEjemplares(sesion),

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

                apiUrl(`/api/prestamos/${idPrestamo}/devolver`),

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

                fetch(apiUrl('/api/prestamos'), {
                    headers: {
                        Authorization: sesion?.autorizacion ?? '',
                    },
                }),

                obtenerEjemplares(sesion),

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

                    <select

                        id="usuario-prestamo"

                        required

                        value={nuevoPrestamo.idUsuario}

                        onChange={(e) =>

                            setNuevoPrestamo({

                                ...nuevoPrestamo,

                                idUsuario: e.target.value,

                            })

                        }

                    >

                        <option value="">Seleccionar lector</option>



                        {usuarios.map((usuario) => (

                            <option

                                key={usuario.idUsuario}

                                value={usuario.idUsuario}

                            >

                                {usuario.nombre} {usuario.apellido}

                            </option>

                        ))}

                    </select>



                    <label htmlFor="ejemplar-prestamo">

                        Seleccionar ejemplar disponible *

                    </label>

                    <select

                        id="ejemplar-prestamo"

                        required

                        value={nuevoPrestamo.idEjemplar}

                        onChange={(e) =>

                            setNuevoPrestamo({

                                ...nuevoPrestamo,

                                idEjemplar: e.target.value,

                            })

                        }

                    >

                        <option value="">Seleccionar ejemplar</option>



                        {ejemplares

                            .filter(

                                (ejemplar) =>

                                    ejemplar.activo &&

                                    ejemplar.libro.activo &&

                                    ejemplar.estado === 'DISPONIBLE'

                            )

                            .map((ejemplar) => (

                                <option

                                    key={ejemplar.idEjemplar}

                                    value={ejemplar.idEjemplar}

                                >

                                    {ejemplar.libro.titulo} - {ejemplar.codigoInventario}

                                </option>

                            ))}

                    </select>



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



            <label htmlFor="filtro-prestamos">Filtrar prÃ©stamos</label>
            <select id="filtro-prestamos" value={filtroPrestamos} onChange={(evento) => setFiltroPrestamos(evento.target.value as typeof filtroPrestamos)}>
                <option value="TODOS">Todos</option>
                <option value="ACTIVOS">Activos</option>
                <option value="VENCIDOS">Vencidos</option>
                <option value="DEVUELTOS">Devueltos</option>
            </select>

            {prestamos.length === 0 ? (

                <p>No hay préstamos registrados.</p>

            ) : (

                <div className="tarjetas">

                    {prestamos.filter((prestamo) => {
                        if (filtroPrestamos === 'ACTIVOS') return prestamo.estado === 'ACTIVO'
                        if (filtroPrestamos === 'VENCIDOS') return prestamo.estado === 'ACTIVO' && prestamo.fechaDevolucionReal === null && prestamo.vencido
                        if (filtroPrestamos === 'DEVUELTOS') return prestamo.estado === 'DEVUELTO'
                        return true
                    }).map((prestamo) => (

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

                            {prestamo.vencido && <p className="mensaje-error" role="status">Vencido</p>}
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



    return (

        <div className="app">

            <header className="encabezado">

                <div>

                    <h1>BiblioGest</h1>

                    <p>Sistema Web de Gestión de Biblioteca</p>

                </div>

                <nav>

                    <button onClick={() => setVista('inicio')}>

                        Inicio

                    </button>



                    <button onClick={() => setVista('catalogo')}>

                        Catálogo

                    </button>



                    {esLector && (
                        <button onClick={() => setVista('mis-prestamos')}>Mis préstamos</button>
                    )}

                    {esBibliotecario && (

                        <>

                            <button onClick={() => setVista('prestamos')}>

                                Préstamos

                            </button>



                            <button onClick={() => setVista('usuarios')}>

                                Usuarios

                            </button>

                            <button onClick={() => setVista('autores')}>Autores</button>

                            <button onClick={() => setVista('categorias')}>Categorías</button>

                        </>

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

                {vista === 'login' && !sesion && (

                    <Login onLogin={iniciarSesion} />

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



                {!cargando && !error && vista === 'inicio' && (

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

                            <h2>Accesos principales</h2>



                            <div className="tarjetas">

                                <article

                                    className="tarjeta tarjeta-acceso"

                                    role="button"

                                    tabIndex={0}

                                    onClick={() => setVista('catalogo')}

                                    onKeyDown={(event) => {
                                        if (event.key === 'Enter' || event.key === ' ') {
                                            event.preventDefault()
                                            setVista('catalogo')
                                        }
                                    }}

                                >

                                    <span>📚</span>

                                    <h3>Catálogo</h3>

                                    <p>Consultar los libros disponibles.</p>

                                </article>



                                {esLector && (
                                    <article
                                        className="tarjeta tarjeta-acceso"
                                        role="button"
                                        tabIndex={0}
                                        onClick={() => setVista('mis-prestamos')}
                                        onKeyDown={(event) => {
                                            if (event.key === 'Enter' || event.key === ' ') {
                                                event.preventDefault()
                                                setVista('mis-prestamos')
                                            }
                                        }}
                                    >
                                        <span>📖</span>
                                        <h3>Mis préstamos</h3>
                                        <p>Consultar tus préstamos e historial de devoluciones.</p>
                                    </article>
                                )}

                                {esBibliotecario && (
                                    <>
                                        <article

                                            className="tarjeta tarjeta-acceso"

                                            role="button"

                                            tabIndex={0}

                                            onClick={() => setVista('prestamos')}

                                            onKeyDown={(event) => {
                                                if (event.key === 'Enter' || event.key === ' ') {
                                                    event.preventDefault()
                                                    setVista('prestamos')
                                                }
                                            }}

                                        >

                                            <span>🔄</span>

                                            <h3>Préstamos</h3>

                                            <p>Consultar préstamos y devoluciones.</p>

                                        </article>

                                        <article
                                            className="tarjeta tarjeta-acceso"
                                            role="button"
                                            tabIndex={0}
                                            onClick={() => setVista('autores')}
                                            onKeyDown={(event) => {
                                                if (event.key === 'Enter' || event.key === ' ') {
                                                    event.preventDefault()
                                                    setVista('autores')
                                                }
                                            }}
                                        >
                                            <span>✍️</span>
                                            <h3>Autores</h3>
                                            <p>Administrar autores del catálogo.</p>
                                        </article>

                                        <article
                                            className="tarjeta tarjeta-acceso"
                                            role="button"
                                            tabIndex={0}
                                            onClick={() => setVista('categorias')}
                                            onKeyDown={(event) => {
                                                if (event.key === 'Enter' || event.key === ' ') {
                                                    event.preventDefault()
                                                    setVista('categorias')
                                                }
                                            }}
                                        >
                                            <span>🏷️</span>
                                            <h3>Categorías</h3>
                                            <p>Administrar categorías del catálogo.</p>
                                        </article>



                                        <article

                                            className="tarjeta tarjeta-acceso"

                                            role="button"

                                            tabIndex={0}

                                            onClick={() => setVista('usuarios')}

                                            onKeyDown={(event) => {
                                                if (event.key === 'Enter' || event.key === ' ') {
                                                    event.preventDefault()
                                                    setVista('usuarios')
                                                }
                                            }}

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

                {!cargando && !error && esBibliotecario && vista === 'autores' && (
                    <GestionAutoresCategorias tipo="autores" autorizacion={sesion.autorizacion}
                        onAutoresActualizados={setAutores} onCategoriasActualizadas={setCategorias} />
                )}

                {!cargando && !error && esBibliotecario && vista === 'categorias' && (
                    <GestionAutoresCategorias tipo="categorias" autorizacion={sesion.autorizacion}
                        onAutoresActualizados={setAutores} onCategoriasActualizadas={setCategorias} />
                )}

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
