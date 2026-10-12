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

function App() {
  const [libros, setLibros] = useState<Libro[]>([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    fetch('http://localhost:8080/api/libros/activos')
        .then((respuesta) => {
          if (!respuesta.ok) {
            throw new Error('No se pudo obtener el catálogo')
          }

          return respuesta.json()
        })
        .then((datos: Libro[]) => {
          setLibros(datos)
          setCargando(false)
        })
        .catch(() => {
          setError('No se pudo conectar con el servidor.')
          setCargando(false)
        })
  }, [])

  return (
      <div className="app">
        <header className="encabezado">
          <div>
            <h1>BiblioGest</h1>
            <p>Sistema Web de Gestión de Biblioteca</p>
          </div>

          <nav>
            <button>Inicio</button>
            <button>Catálogo</button>
            <button>Préstamos</button>
            <button>Usuarios</button>
          </nav>
        </header>

        <main className="contenido">
          <section className="bienvenida">
            <p className="etiqueta">BIBLIOTECA ESCOLAR</p>
            <h2>Bienvenido a BiblioGest</h2>

            <p>
              Una herramienta para organizar el catálogo, consultar ejemplares
              disponibles y gestionar los préstamos de la biblioteca.
            </p>
          </section>

          <section className="panel">
            <h2>Catálogo de libros</h2>

            {cargando && <p>Cargando catálogo...</p>}

            {error && <p className="mensaje-error">{error}</p>}

            {!cargando && !error && libros.length === 0 && (
                <p>No hay libros disponibles.</p>
            )}

            <div className="tarjetas">
              {libros.map((libro) => (
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
                  </article>
              ))}
            </div>
          </section>
        </main>

        <footer>
          <p>BiblioGest - Sistema de Gestión de Biblioteca</p>
        </footer>
      </div>
  )
}

export default App