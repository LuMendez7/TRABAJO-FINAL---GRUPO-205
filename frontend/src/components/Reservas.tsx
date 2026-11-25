import { API_BASE_URL } from '../apiConfig'
import { useEffect, useState } from 'react'

type Reserva = {
    idReserva: number
    estado: string
    fechaSolicitud: string
    fechaAprobacion: string | null
    fechaVencimiento: string | null
    fechaResolucion: string | null
    observaciones: string | null
    libro: {
        idLibro: number
        titulo: string
    }
    usuario: {
        nombre: string
        apellido: string
    }
    ejemplar: {
        codigoInventario: string
        estado: string
    } | null
    prestamo: {
        idPrestamo: number
    } | null
}

type Props = {
    autorizacion: string
    administrador: boolean
    onCambio: () => void
}

const API = `${API_BASE_URL}/api/reservas`

const estados = [
    'PENDIENTE',
    'APROBADA',
    'RETIRADA',
    'CANCELADA',
    'RECHAZADA',
    'VENCIDA'
]

const fecha = (valor: string | null) =>
    valor
        ? new Date(valor).toLocaleString('es-AR')
        : '—'

export default function Reservas({
    autorizacion,
    administrador,
    onCambio
}: Props) {

    const [reservas, setReservas] = useState<Reserva[]>([])
    const [cargando, setCargando] = useState(true)
    const [procesando, setProcesando] = useState<number | null>(null)
    const [error, setError] = useState('')
    const [mensaje, setMensaje] = useState('')
    const [filtro, setFiltro] = useState('TODAS')
    const [busqueda, setBusqueda] = useState('')
    const [revision, setRevision] = useState(0)

    useEffect(() => {

        const controlador = new AbortController()

        setCargando(true)
        setError('')

        fetch(
            administrador ? API : `${API}/mis-reservas`,
            {
                headers: { Authorization: autorizacion },
                signal: controlador.signal
            }
        )
            .then(r => {
                if (!r.ok) {
                    throw new Error('No se pudieron consultar las reservas.')
                }
                return r.json()
            })
            .then(datos => {
                if (!controlador.signal.aborted) {
                    setReservas(datos)
                }
            })
            .catch(e => {
                if (!controlador.signal.aborted) {
                    setError(e.message)
                }
            })
            .finally(() => {
                if (!controlador.signal.aborted) {
                    setCargando(false)
                }
            })

        return () => controlador.abort()

    }, [autorizacion, administrador, revision])

    const operar = async (
        reserva: Reserva,
        accion: string
    ) => {

        let datos: object | undefined

        if (accion === 'rechazar') {
            const motivo = window.prompt(
                'Motivo del rechazo (opcional):', ''
            )

            if (motivo === null) return

            if (motivo.length > 500) {
                setError('El motivo admite hasta 500 caracteres.')
                return
            }

            datos = { motivo }
        }

        if (accion === 'retirar') {
            const valor = window.prompt(
                'Duración del préstamo en días (1 a 30):', '7'
            )

            if (valor === null) return

            const diasPrestamo = Number(valor)

            if (!Number.isInteger(diasPrestamo) ||
                diasPrestamo < 1 || diasPrestamo > 30) {

                setError('Ingresá una cantidad válida de días.')
                return
            }

            datos = { diasPrestamo }
        }

        if (!window.confirm(
            `¿Confirmás ${accion} la reserva #${reserva.idReserva}?`
        )) return

        setProcesando(reserva.idReserva)
        setError('')
        setMensaje('')

        try {
            const respuesta = await fetch(
                `${API}/${reserva.idReserva}/${accion}`,
                {
                    method: 'PUT',
                    headers: {
                        Authorization: autorizacion,
                        'Content-Type': 'application/json'
                    },
                    body: datos ? JSON.stringify(datos) : undefined
                }
            )

            if (!respuesta.ok) {
                const detalle = await respuesta.json().catch(() => ({}))

                throw new Error(
                    detalle.message ||
                    'No se pudo completar la operación. Verificá el estado de la reserva.'
                )
            }

            const actualizada: Reserva = await respuesta.json()

            setReservas(anteriores =>
                anteriores.map(r =>
                    r.idReserva === actualizada.idReserva
                        ? actualizada
                        : r
                )
            )

            setMensaje('Operación realizada correctamente.')
            onCambio()
            setRevision(v => v + 1)

        } catch (e) {
            setError(
                e instanceof Error ? e.message : 'Error de conexión.'
            )
        } finally {
            setProcesando(null)
        }
    }

    const visibles = reservas.filter(r => {

        const texto = (
            r.libro.titulo + ' ' +
            r.usuario.nombre + ' ' +
            r.usuario.apellido + ' ' +
            r.idReserva
        ).toLowerCase()

        return (
            (filtro === 'TODAS' || r.estado === filtro) &&
            texto.includes(busqueda.toLowerCase())
        )
    })

    const pendientes = reservas.filter(
        r => r.estado === 'PENDIENTE'
    ).length

    const aprobadas = reservas.filter(
        r => r.estado === 'APROBADA'
    ).length

    const retiradas = reservas.filter(
        r => r.estado === 'RETIRADA'
    ).length

    return (
        <section className="bg-reservas">

            <header className="bg-reservas-header">
                <div>
                    <small>SERVICIOS BIBLIOTECARIOS</small>

                    <h2>
                        {administrador
                            ? 'Gestión de reservas'
                            : 'Mis reservas'}
                    </h2>

                    <p>
                        {administrador
                            ? 'Administrá las solicitudes y los retiros de ejemplares.'
                            : 'Consultá el estado de tus solicitudes bibliográficas.'}
                    </p>
                </div>

                <button
                    type="button"
                    className="bg-reserva-boton"
                    disabled={cargando || procesando !== null}
                    onClick={() => setRevision(v => v + 1)}
                >
                    Actualizar
                </button>
            </header>

            <div className="bg-reservas-indicadores">
                {[
                    ['Total de reservas', reservas.length],
                    ['Pendientes', pendientes],
                    ['Listas para retirar', aprobadas],
                    ['Retiradas', retiradas]
                ].map(([nombre, cantidad]) => (
                    <div key={nombre}>
                        <small>{nombre}</small>
                        <strong>{cantidad}</strong>
                    </div>
                ))}
            </div>

            <div className="bg-reservas-filtros">
                <input
                    type="search"
                    aria-label="Buscar reservas"
                    placeholder="Buscar una reserva..."
                    value={busqueda}
                    onChange={e => setBusqueda(e.target.value)}
                />

                <select
                    aria-label="Filtrar por estado"
                    value={filtro}
                    onChange={e => setFiltro(e.target.value)}
                >
                    <option value="TODAS">Todos los estados</option>

                    {estados.map(e => (
                        <option key={e} value={e}>{e}</option>
                    ))}
                </select>
            </div>

            {mensaje && (
                <p className="bg-reserva-exito" role="status">
                    {mensaje}
                </p>
            )}

            {error && (
                <p className="bg-reserva-error" role="alert">
                    {error}
                </p>
            )}

            {cargando ? (
                <p>Cargando reservas...</p>
            ) : visibles.length === 0 ? (
                <div className="bg-reservas-vacio">
                    <h3>No hay reservas para mostrar</h3>
                    <p>
                        {administrador
                            ? 'Las solicitudes de los lectores aparecerán aquí.'
                            : 'Podés solicitar libros desde el catálogo bibliográfico.'}
                    </p>
                </div>
            ) : (
                <div className="bg-reservas-listado">

                    {visibles.map(r => (
                        <article
                            className="bg-reserva-card"
                            key={r.idReserva}
                        >
                            <div className="bg-reserva-card-header">
                                <div>
                                    <small>RESERVA #{r.idReserva}</small>
                                    <h3>{r.libro.titulo}</h3>
                                </div>

                                <span className={
                                    `bg-reserva-estado ${r.estado.toLowerCase()}`
                                }>
                                    {r.estado}
                                </span>
                            </div>

                            <div className="bg-reserva-detalles">

                                {administrador && (
                                    <div>
                                        <small>Lector</small>
                                        <strong>
                                            {r.usuario.nombre} {r.usuario.apellido}
                                        </strong>
                                    </div>
                                )}

                                <div>
                                    <small>Fecha de solicitud</small>
                                    <strong>{fecha(r.fechaSolicitud)}</strong>
                                </div>

                                {r.fechaVencimiento && (
                                    <div>
                                        <small>Plazo de retiro</small>
                                        <strong>{fecha(r.fechaVencimiento)}</strong>
                                    </div>
                                )}

                                {r.ejemplar && (
                                    <div>
                                        <small>Inventario</small>
                                        <strong>{r.ejemplar.codigoInventario}</strong>
                                    </div>
                                )}

                                {r.prestamo && (
                                    <div>
                                        <small>Préstamo</small>
                                        <strong>#{r.prestamo.idPrestamo}</strong>
                                    </div>
                                )}
                            </div>

                            {r.observaciones && (
                                <p>Observaciones: {r.observaciones}</p>
                            )}

                            {!administrador && r.estado === 'APROBADA' && (
                                <p className="bg-reserva-exito">
                                    Tu ejemplar está reservado.
                                    Acercate a la biblioteca antes del vencimiento.
                                </p>
                            )}

                            <div className="bg-reserva-acciones">

                                {administrador && r.estado === 'PENDIENTE' && (
                                    <>
                                        <button
                                            className="bg-reserva-boton"
                                            disabled={procesando !== null}
                                            onClick={() => operar(r, 'aprobar')}
                                        >
                                            Aprobar
                                        </button>

                                        <button
                                            className="bg-reserva-secundario"
                                            disabled={procesando !== null}
                                            onClick={() => operar(r, 'rechazar')}
                                        >
                                            Rechazar
                                        </button>
                                    </>
                                )}

                                {administrador && r.estado === 'APROBADA' && (
                                    <button
                                        className="bg-reserva-boton"
                                        disabled={procesando !== null}
                                        onClick={() => operar(r, 'retirar')}
                                    >
                                        Registrar retiro
                                    </button>
                                )}

                                {['PENDIENTE', 'APROBADA'].includes(r.estado) && (
                                    <button
                                        className="bg-reserva-secundario"
                                        disabled={procesando !== null}
                                        onClick={() => operar(r, 'cancelar')}
                                    >
                                        Cancelar reserva
                                    </button>
                                )}

                            </div>
                        </article>
                    ))}
                </div>
            )}
        </section>
    )
}