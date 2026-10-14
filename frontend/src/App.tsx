import { useEffect, useState } from 'react'
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

    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState('')
    const [busqueda, setBusqueda] = useState('')

    useEffect(() => {
        Promise.all([
            fetch('http://localhost:8080/api/libros/activos'),
            fetch('http://localhost:8080/api/ejemplares/activos'),
            fetch('http://localhost:8080/api/usuarios/activos'),
            fetch('http://localhost:8080/api/prestamos'),
        ])
            .then(async ([
                             respuestaLibros,
                             respuestaEjemplares,
                             respuestaUsuarios,
                             respuestaPrestamos,
                         ]) => {
                if (
                    !respuestaLibros.ok ||
                    !respuestaEjemplares.ok ||
                    !respuestaUsuarios.ok ||
                    !respuestaPrestamos.ok
                ) {
                    throw new Error('No se pudieron obtener los datos')
                }

                const datosLibros: Libro[] = await respuestaLibros.json()
                const datosEjemplares: Ejemplar[] = await respuestaEjemplares.json()
                const datosUsuarios: Usuario[] = await respuestaUsuarios.json()
                const datosPrestamos: Prestamo[] = await respuestaPrestamos.json()

                setLibros(datosLibros)
                setEjemplares(datosEjemplares)
                setUsuarios(datosUsuarios)
                setPrestamos(datosPrestamos)
                setCargando(false)
            })
            .catch(() => {
                setError('No se pudo conectar con el servidor.')
                setCargando(false)
            })
    }, [])

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
            <div className="buscador">
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

    const mostrarPrestamos = () => (
        <section className="panel">
            <h2>Préstamos</h2>

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