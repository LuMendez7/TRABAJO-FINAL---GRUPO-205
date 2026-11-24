import { useCallback, useEffect, useRef, useState } from 'react'

type Notificacion = {
    idNotificacion: number
    titulo: string
    mensaje: string
    tipo: string
    leida: boolean
    fechaCreacion: string
}

type Props = {
    autorizacion: string
}

const API = 'http://localhost:8080/api/notificaciones'

function IconoCampana() {
    return (
        <svg width="23" height="23" viewBox="0 0 24 24"
            fill="none" stroke="currentColor" strokeWidth="1.8"
            strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/>
            <path d="M10 21h4"/>
        </svg>
    )
}

function iconoTipo(tipo: string) {
    if (tipo.includes('VENCIDO')) return '!'
    if (tipo.includes('RECORDATORIO')) return '◷'
    if (tipo.includes('APROBADA')) return '✓'
    if (tipo.includes('DEVUELTO')) return '✓'
    if (tipo.includes('CANCELADA')) return '×'
    if (tipo.includes('RECHAZADA')) return '×'
    return '▤'
}

function fechaTexto(fecha: string) {
    const valor = new Date(fecha)
    return Number.isNaN(valor.getTime())
        ? fecha
        : valor.toLocaleString('es-AR')
}

export default function CentroNotificaciones({ autorizacion }: Props) {
    const [abierto, setAbierto] = useState(false)
    const [cantidad, setCantidad] = useState(0)
    const [notificaciones, setNotificaciones] = useState<Notificacion[]>([])
    const [soloNuevas, setSoloNuevas] = useState(false)
    const [cargando, setCargando] = useState(false)
    const [procesando, setProcesando] = useState(false)
    const [error, setError] = useState('')
    const contenedor = useRef<HTMLDivElement>(null)
    const campana = useRef<HTMLButtonElement>(null)

    const consultarCantidad = useCallback(async () => {
        try {
            const respuesta = await fetch(`${API}/no-leidas`, {
                headers: { Authorization: autorizacion }
            })

            if (!respuesta.ok) return

            const datos = await respuesta.json()
            setCantidad(Number(datos.cantidad) || 0)
        } catch {
            // Se conserva el ultimo contador disponible.
        }
    }, [autorizacion])

    const cargar = useCallback(async () => {
        setCargando(true)
        setError('')

        try {
            const respuesta = await fetch(API, {
                headers: { Authorization: autorizacion }
            })

            if (!respuesta.ok) {
                throw new Error('No se pudieron cargar las notificaciones.')
            }

            const datos: Notificacion[] = await respuesta.json()
            setNotificaciones(datos)
            await consultarCantidad()
        } catch {
            setError('No se pudieron cargar las notificaciones.')
        } finally {
            setCargando(false)
        }
    }, [autorizacion, consultarCantidad])

    useEffect(() => {
        void consultarCantidad()

        const intervalo = window.setInterval(() => {
            if (abierto) {
                void cargar()
            } else {
                void consultarCantidad()
            }
        }, 30000)

        return () => window.clearInterval(intervalo)
    }, [abierto, cargar, consultarCantidad])

    useEffect(() => {
        if (!abierto) return

        function cerrarFuera(evento: MouseEvent) {
            if (!contenedor.current?.contains(evento.target as Node)) {
                setAbierto(false)
            }
        }

        function cerrarEscape(evento: KeyboardEvent) {
            if (evento.key === 'Escape') {
                setAbierto(false)
                campana.current?.focus()
            }
        }

        document.addEventListener('mousedown', cerrarFuera)
        document.addEventListener('keydown', cerrarEscape)

        return () => {
            document.removeEventListener('mousedown', cerrarFuera)
            document.removeEventListener('keydown', cerrarEscape)
        }
    }, [abierto])

    async function marcarLeida(id?: number) {
        if (procesando) return

        setProcesando(true)
        setError('')

        try {
            const ruta = id === undefined
                ? `${API}/leer-todas`
                : `${API}/${id}/leer`

            const respuesta = await fetch(ruta, {
                method: 'PUT',
                headers: { Authorization: autorizacion }
            })

            if (!respuesta.ok) {
                throw new Error('No se pudo actualizar la notificación.')
            }

            await cargar()
        } catch {
            setError('No se pudo actualizar la notificación.')
        } finally {
            setProcesando(false)
        }
    }

    const visibles = soloNuevas
        ? notificaciones.filter(n => !n.leida)
        : notificaciones

    return (
        <div className="bg-notificaciones" ref={contenedor}>
            <button
                ref={campana}
                type="button"
                className="bg-noti-campana"
                aria-label={`Notificaciones: ${cantidad} sin leer`}
                aria-expanded={abierto}
                title="Notificaciones"
                onClick={() => {
                    if (!abierto) void cargar()
                    setAbierto(!abierto)
                }}
            >
                <IconoCampana />

                {cantidad > 0 && (
                    <span className="bg-noti-contador">
                        {cantidad > 99 ? '99+' : cantidad}
                    </span>
                )}
            </button>

            {abierto && (
                <section
                    className="bg-noti-panel"
                    aria-label="Centro de notificaciones"
                >
                    <header className="bg-noti-encabezado">
                        <div>
                            <small>BIBLIOGEST</small>
                            <h2>Notificaciones</h2>
                            <p>
                                {cantidad === 0
                                    ? 'Estás al día con tus avisos.'
                                    : `${cantidad} notificaciones sin leer`}
                            </p>
                        </div>

                        <button
                            type="button"
                            className="bg-noti-cerrar"
                            aria-label="Cerrar notificaciones"
                            onClick={() => {
                                setAbierto(false)
                                campana.current?.focus()
                            }}
                        >
                            ×
                        </button>
                    </header>

                    <div className="bg-noti-herramientas">
                        <button
                            type="button"
                            className={!soloNuevas ? 'seleccionado' : ''}
                            onClick={() => setSoloNuevas(false)}
                        >
                            Todas
                        </button>

                        <button
                            type="button"
                            className={soloNuevas ? 'seleccionado' : ''}
                            onClick={() => setSoloNuevas(true)}
                        >
                            Sin leer
                        </button>

                        <button
                            type="button"
                            className="bg-noti-leer-todas"
                            disabled={cantidad === 0 || procesando}
                            onClick={() => void marcarLeida()}
                        >
                            Marcar todas como leídas
                        </button>
                    </div>

                    {error && (
                        <p className="bg-noti-error" role="alert">
                            {error}
                        </p>
                    )}

                    <div className="bg-noti-listado">
                        {cargando && notificaciones.length === 0 ? (
                            <p className="bg-noti-vacio">Cargando avisos...</p>
                        ) : visibles.length === 0 ? (
                            <div className="bg-noti-vacio">
                                <IconoCampana />
                                <strong>No hay notificaciones para mostrar.</strong>
                                <p>Aquí recibirás las novedades de la biblioteca.</p>
                            </div>
                        ) : (
                            visibles.map(n => (
                                <article
                                    key={n.idNotificacion}
                                    className={`bg-noti-item ${!n.leida ? 'nueva' : ''}`}
                                >
                                    <div className="bg-noti-icono">
                                        {iconoTipo(n.tipo)}
                                    </div>

                                    <div className="bg-noti-contenido">
                                        <h3>{n.titulo}</h3>
                                        <p>{n.mensaje}</p>
                                        <time>{fechaTexto(n.fechaCreacion)}</time>

                                        {!n.leida && (
                                            <button
                                                type="button"
                                                disabled={procesando}
                                                onClick={() => void marcarLeida(n.idNotificacion)}
                                            >
                                                Marcar como leída
                                            </button>
                                        )}
                                    </div>

                                    {!n.leida && (
                                        <span className="bg-noti-punto" />
                                    )}
                                </article>
                            ))
                        )}
                    </div>

                    <footer className="bg-noti-pie">
                        Servicios bibliotecarios · BiblioGest
                    </footer>
                </section>
            )}
        </div>
    )
}