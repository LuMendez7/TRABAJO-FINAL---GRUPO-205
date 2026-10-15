import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
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

type Vista = 'inicio' | 'catalogo' | 'prestamos' | 'usuarios'

function App() {
    const [vista, setVista] = useState<Vista>('inicio')

    const [libros, setLibros] = useState<Libro[]>([])
    const [ejemplares, setEjemplares] = useState<Ejemplar[]>([])
    const [usuarios, setUsuarios] = useState<Usuario[]>([])
    const [prestamos, setPrestamos] = useState<Prestamo[]>([])
    const [categorias, setCategorias] = useState<Categoria[]>([])
    const [autores, setAutores] = useState<Autor[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState('')
    const [busqueda, setBusqueda] = useState('')
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
        Promise.all([
            fetch('http://localhost:8080/api/libros/activos'),
            fetch('http://localhost:8080/api/ejemplares/activos'),
            fetch('http://localhost:8080/api/usuarios/activos'),
            fetch('http://localhost:8080/api/prestamos'),
            fetch('http://localhost:8080/api/categorias'),
            fetch('http://localhost:8080/api/autores'),
        ])
            .then(async ([
                             respuestaLibros,
                             respuestaEjemplares,
                             respuestaUsuarios,
                             respuestaPrestamos,
                             respuestaCategorias,
                             respuestaAutores,
                         ]) => {
                if (
                    !respuestaLibros.ok ||
                    !respuestaEjemplares.ok ||
                    !respuestaUsuarios.ok ||
                    !respuestaPrestamos.ok ||
                    !respuestaCategorias.ok ||
                    !respuestaAutores.ok
                ) {
                    throw new Error('No se pudieron obtener los datos')
                }

                const datosLibros: Libro[] = await respuestaLibros.json()
                const datosEjemplares: Ejemplar[] = await respuestaEjemplares.json()
                const datosUsuarios: Usuario[] = await respuestaUsuarios.json()
                const datosPrestamos: Prestamo[] = await respuestaPrestamos.json()
                const datosCategorias: Categoria[] = await respuestaCategorias.json()
                const datosAutores: Autor[] = await respuestaAutores.json()

                setLibros(datosLibros)
                setEjemplares(datosEjemplares)
                setUsuarios(datosUsuarios)
                setPrestamos(datosPrestamos)
                setCategorias(datosCategorias)
                setAutores(datosAutores)
                setCargando(false)
            })
            .catch(() => {
                setError('No se pudo conectar con el servidor.')
                setCargando(false)
            })
    }, [])

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

            const respuesta = await fetch('http://localhost:8080/api/libros', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(datos),
            })

            if (!respuesta.ok) {
                throw new Error('No se pudo guardar el libro')
            }

            const respuestaLibros = await fetch(
                'http://localhost:8080/api/libros/activos'
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

            const respuesta = await fetch('http://localhost:8080/api/ejemplares', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(datos),
            })

            if (!respuesta.ok) {
                throw new Error('No se pudo guardar el ejemplar')
            }

            const respuestaEjemplares = await fetch(
                'http://localhost:8080/api/ejemplares/activos'
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
            <button
                className="boton-principal"
                onClick={() => {
                    setMostrarFormulario(!mostrarFormulario)
                    setMensajeLibro('')
                }}
            >
                {mostrarFormulario ? 'Cancelar' : '+ Agregar libro'}
            </button>
            {mostrarFormularioEjemplar && (
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
            <button
                className="boton-principal"
                onClick={() => {
                    setMostrarFormularioEjemplar(!mostrarFormularioEjemplar)
                    setMensajeEjemplar('')
                }}
            >
                {mostrarFormularioEjemplar ? 'Cancelar ejemplar' : '+ Agregar ejemplar'}
            </button>

            {mensajeEjemplar && <p role="status">{mensajeEjemplar}</p>}

            {mensajeLibro && <p role="status">{mensajeLibro}</p>}
            <div className="buscador">
                {mostrarFormulario && (
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
                                            ejemplar.estado === 'DISPONIBLE'
                                    ).length
                                }
                            </p>
                        </article>
                    ))}
                </div>
            )}
        </section>
    )

    const mostrarUsuarios = () => (
        <section className="panel">
            <h2>Usuarios activos</h2>

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

            const respuesta = await fetch('http://localhost:8080/api/prestamos', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
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
                fetch('http://localhost:8080/api/prestamos'),
                fetch('http://localhost:8080/api/ejemplares/activos'),
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
                `http://localhost:8080/api/prestamos/${idPrestamo}/devolver`,
                {
                    method: 'PUT',
                }
            )

            if (!respuesta.ok) {
                if (respuesta.status === 409) {
                    throw new Error('Este préstamo ya fue devuelto.')
                }

                throw new Error('No se pudo registrar la devolución.')
            }

            const [respuestaPrestamos, respuestaEjemplares] = await Promise.all([
                fetch('http://localhost:8080/api/prestamos'),
                fetch('http://localhost:8080/api/ejemplares/activos'),
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

            {prestamos.length === 0 ? (
                <p>No hay préstamos registrados.</p>
            ) : (
                <div className="tarjetas">
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

    return (
        <div className="app">
            <header className="encabezado">
                <div>
                    <h1>BiblioGest</h1>
                    <p>Sistema Web de Gestión de Biblioteca</p>
                </div>

                <nav>
                    <button onClick={() => setVista('inicio')}>Inicio</button>
                    <button onClick={() => setVista('catalogo')}>Catálogo</button>
                    <button onClick={() => setVista('prestamos')}>Préstamos</button>
                    <button onClick={() => setVista('usuarios')}>Usuarios</button>
                </nav>
            </header>

            <main className="contenido">
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
                                    onClick={() => setVista('catalogo')}
                                >
                                    <span>📚</span>
                                    <h3>Catálogo</h3>
                                    <p>Consultar los libros disponibles.</p>
                                </article>

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
                            </div>
                        </section>
                    </>
                )}

                {!cargando && !error && vista === 'catalogo' && mostrarCatalogo()}
                {!cargando && !error && vista === 'prestamos' && mostrarPrestamos()}
                {!cargando && !error && vista === 'usuarios' && mostrarUsuarios()}
            </main>

            <footer>
                <p>BiblioGest - Sistema de Gestión de Biblioteca</p>
            </footer>
        </div>
    )
}

export default App