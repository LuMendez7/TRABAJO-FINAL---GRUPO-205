import BotonReserva from './BotonReserva'
import EditorFichaLibro from './EditorFichaLibro'
import { useEffect, useRef, useState } from 'react'
import { createPortal } from 'react-dom'
import PortadaLibro from './PortadaLibro'

type Props = {
    idLibro: number
    titulo: string
    autor: string
    categoria: string
    editorial: string
    anio: number | null
    isbn: string
    ejemplares: {
        idEjemplar: number
        codigoInventario: string
        estado: string
    }[]
    autorizacion?: string
    autorizacionLector?: string
    onCerrar: () => void
}

type Descripcion = {
    texto: string
    fuente?: string
}

export default function FichaLibroModal({
    idLibro, titulo, autor, categoria,
    editorial, anio, isbn, ejemplares, autorizacion, autorizacionLector, onCerrar
}: Props) {
    const [descripcion, setDescripcion] = useState<Descripcion | null>(null)
    const [cargando, setCargando] = useState(true)
    const [editando, setEditando] = useState(false)
    const [revision, setRevision] = useState(0)
    const [mensaje, setMensaje] = useState('')
    const dialogo = useRef<HTMLDialogElement>(null)
    const disponibles = ejemplares.filter(e => e.estado === 'DISPONIBLE').length

    useEffect(() => {
        const elemento = dialogo.current
        const anterior = document.activeElement as HTMLElement | null
        const overflow = document.body.style.overflow

        elemento?.showModal()
        document.body.style.overflow = 'hidden'

        return () => {
            elemento?.close()
            document.body.style.overflow = overflow
            anterior?.focus()
        }
    }, [])

    useEffect(() => {
        const controlador = new AbortController()

        setCargando(true)

        const cargarDescripcion = async () => {
            try {
                const respuesta = await fetch(
                    `http://localhost:8080/api/fichas/${idLibro}`,
                    { signal: controlador.signal }
                )

                if (!respuesta.ok) {
                    throw new Error('No se pudo consultar la ficha')
                }

                const ficha = await respuesta.json()

                if (ficha.descripcion !== null &&
                    ficha.descripcion !== undefined) {

                    if (!controlador.signal.aborted) {
                        setDescripcion({
                            texto: ficha.descripcion,
                            fuente: 'Biblioteca - BiblioGest'
                        })
                    }

                } else {
                    const local = await fetch('/descripciones.json', {
                        signal: controlador.signal
                    })

                    const datos = local.ok ? await local.json() : {}

                    if (!controlador.signal.aborted) {
                        setDescripcion(datos[String(idLibro)] ?? null)
                    }
                }
            } catch {
                if (!controlador.signal.aborted) {
                    setDescripcion(null)
                }
            } finally {
                if (!controlador.signal.aborted) {
                    setCargando(false)
                }
            }
        }

        void cargarDescripcion()

        return () => controlador.abort()
    }, [idLibro, revision])

    return createPortal(
        <dialog
            ref={dialogo}
            className="bg-ficha-modal"
            aria-labelledby="bg-ficha-titulo"
            onCancel={e => {
                e.preventDefault()
                onCerrar()
            }}
            onClick={e => {
                if (e.target === e.currentTarget) onCerrar()
            }}
        >
            <div className="bg-ficha-interior">
                <header className="bg-ficha-encabezado">
                    <div>
                        <small>BIBLIOGEST</small>
                        <strong>Ficha bibliográfica</strong>
                    </div>
                    <div className="bg-ficha-acciones-superiores">
{autorizacion && !editando && (
    <button
        type="button"
        className="bg-ficha-editar-superior"
        disabled={cargando}
        onClick={() => {
            setMensaje('')
            setEditando(true)
        }}
    >
        <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            aria-hidden="true"
        >
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L9 17l-4 1 1-4Z" />
        </svg>
        Editar ficha
    </button>
)}
<button
                        type="button"
                        className="bg-ficha-cruz"
                        aria-label="Cerrar ficha"
                        onClick={onCerrar}
                    >
                        ×
                    </button>
</div>
                </header>


{autorizacion && mensaje && (
    <p role="status" className="bg-ficha-mensaje">
        {mensaje}
    </p>
)}

{autorizacion && editando && (
    <EditorFichaLibro
        idLibro={idLibro}
        autorizacion={autorizacion}
        descripcion={descripcion?.texto ?? ''}
        onCancelar={() => setEditando(false)}
        onGuardar={() => {
            setEditando(false)
            setRevision(valor => valor + 1)
            setMensaje('Ficha actualizada correctamente.')
            window.dispatchEvent(
                new Event('bibliogest-ficha-actualizada')
            )
        }}
    />
)}
{autorizacionLector && (
                    <BotonReserva
                        key={idLibro}
                        idLibro={idLibro}
                        autorizacion={autorizacionLector}
                        disponibles={disponibles}
                    />
                )}

<div className="bg-ficha-principal">
                    <div className="bg-ficha-portada">
                        <PortadaLibro
                            idLibro={idLibro}
                            titulo={titulo}
                            autor={autor}
                            categoria={categoria}
                        />
                    </div>

                    <div className="bg-ficha-resumen">
                        <span className="bg-ficha-categoria">{categoria}</span>
                        <h2 id="bg-ficha-titulo">{titulo}</h2>
                        <p className="bg-ficha-autor">{autor}</p>
                        <p className="bg-ficha-editorial">
                            {editorial} · {anio ?? 'Sin año'}
                        </p>

                        <span className={disponibles > 0
                            ? 'bg-ficha-disponible'
                            : 'bg-ficha-agotado'}>
                            {disponibles > 0 ? 'Disponible' : 'No disponible'}
                        </span>

                        <div className="bg-ficha-descripcion">
                            <h3>Acerca del libro</h3>
                            <p>{cargando
                                ? 'Cargando descripción...'
                                : descripcion?.texto
                                    ?? 'Descripción bibliográfica no disponible por el momento.'}
                            </p>
                            {descripcion?.fuente && (
                                <small>{descripcion.fuente}</small>
                            )}
                        </div>
                    </div>
                </div>

                <section className="bg-ficha-seccion">
                    <h3>Información bibliográfica</h3>
                    <dl className="bg-ficha-datos">
                        <div><dt>ISBN</dt><dd>{isbn}</dd></div>
                        <div><dt>Editorial</dt><dd>{editorial}</dd></div>
                        <div><dt>Publicación</dt><dd>{anio ?? 'Sin año'}</dd></div>
                        <div><dt>Categoría</dt><dd>{categoria}</dd></div>
                        <div><dt>Ejemplares</dt><dd>{ejemplares.length}</dd></div>
                        <div><dt>Disponibles</dt><dd>{disponibles}</dd></div>
                    </dl>
                </section>

                <section className="bg-ficha-seccion">
                    <h3>Disponibilidad de ejemplares</h3>
                    {ejemplares.length === 0 && (
                        <p>No hay ejemplares registrados.</p>
                    )}
                    {ejemplares.map(e => (
                        <div className="bg-ficha-ejemplar" key={e.idEjemplar}>
                            <div>
                                <strong>Inventario N.º {e.codigoInventario}</strong>
                                <small>Ejemplar físico</small>
                            </div>
                            <span className={e.estado === 'DISPONIBLE'
                                ? 'bg-ficha-disponible'
                                : 'bg-ficha-agotado'}>
                                {e.estado}
                            </span>
                        </div>
                    ))}
                </section>

                <footer className="bg-ficha-footer">
                    <button type="button" onClick={onCerrar}>
                        Cerrar ficha
                    </button>
                </footer>
            </div>
        </dialog>,
        document.body
    )
}
