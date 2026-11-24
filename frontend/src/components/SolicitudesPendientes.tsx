import { useCallback, useEffect, useState } from 'react'

interface Solicitud {
    idUsuario: number
    nombre: string
    apellido: string
    email: string
    rol: string
    activo: boolean
    estadoCuenta: string
}

interface SolicitudesPendientesProps {
    autorizacion: string
    onCambio?: () => void
}

const API = 'http://localhost:8080/api'

export default function SolicitudesPendientes({
    autorizacion,
    onCambio,
}: SolicitudesPendientesProps) {
    const [solicitudes, setSolicitudes] = useState<Solicitud[]>([])
    const [cargando, setCargando] = useState(true)
    const [procesando, setProcesando] = useState<number | null>(null)
    const [error, setError] = useState('')
    const [mensaje, setMensaje] = useState('')

    const cargarSolicitudes = useCallback(async () => {
        setCargando(true)
        setError('')

        try {
            const respuesta = await fetch(
                `${API}/usuarios/pendientes`,
                {
                    headers: {
                        Authorization: autorizacion,
                    },
                }
            )

            if (!respuesta.ok) {
                throw new Error(
                    'No se pudieron cargar las solicitudes pendientes.'
                )
            }

            const datos: Solicitud[] = await respuesta.json()

            setSolicitudes(datos)

        } catch (error) {
            setError(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error inesperado.'
            )
        } finally {
            setCargando(false)
        }
    }, [autorizacion])

    useEffect(() => {
        void cargarSolicitudes()
    }, [cargarSolicitudes])

    const gestionarSolicitud = async (
        solicitud: Solicitud,
        accion: 'aprobar' | 'rechazar'
    ) => {
        const confirmar = window.confirm(
            `¿Confirmás ${accion} la solicitud de ${solicitud.nombre} ${solicitud.apellido}?`
        )

        if (!confirmar) return

        setProcesando(solicitud.idUsuario)
        setError('')
        setMensaje('')

        try {
            const respuesta = await fetch(
                `${API}/usuarios/${solicitud.idUsuario}/${accion}`,
                {
                    method: 'PUT',
                    headers: {
                        Authorization: autorizacion,
                    },
                }
            )

            if (!respuesta.ok) {
                if (respuesta.status === 409) {
                    throw new Error(
                        'La solicitud ya fue procesada. Actualizá el listado.'
                    )
                }

                throw new Error(
                    'No se pudo procesar la solicitud.'
                )
            }

            setSolicitudes(actuales =>
                actuales.filter(
                    usuario =>
                        usuario.idUsuario !== solicitud.idUsuario
                )
            )

            setMensaje(
                accion === 'aprobar'
                    ? `La cuenta de ${solicitud.nombre} fue aprobada correctamente.`
                    : `La solicitud de ${solicitud.nombre} fue rechazada correctamente.`
            )

            onCambio?.()

        } catch (error) {
            setError(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error inesperado.'
            )
        } finally {
            setProcesando(null)
        }
    }

    return (
        <section className="solicitudes-contenedor">
            <div className="solicitudes-encabezado">
                <div>
                    <span className="solicitudes-etiqueta">
                        ADMINISTRACIÓN DE LECTORES
                    </span>

                    <h2>Solicitudes de registro</h2>

                    <p>
                        Revisá las solicitudes de acceso
                        de los nuevos lectores.
                    </p>
                </div>

                <button
                    type="button"
                    className="solicitudes-actualizar"
                    onClick={() => void cargarSolicitudes()}
                    disabled={cargando || procesando !== null}
                >
                    Actualizar listado
                </button>
            </div>

            <div className="solicitudes-resumen">
                <span>Solicitudes pendientes</span>

                <strong>{solicitudes.length}</strong>

                <small>
                    Cuentas que requieren revisión
                </small>
            </div>

            {mensaje && (
                <div
                    className="solicitudes-mensaje"
                    role="status"
                >
                    {mensaje}
                </div>
            )}

            {error && (
                <div
                    className="solicitudes-error"
                    role="alert"
                >
                    {error}
                </div>
            )}

            {cargando ? (
                <div className="solicitudes-vacio">
                    Cargando solicitudes...
                </div>
            ) : solicitudes.length === 0 ? (
                !error && (
                    <div className="solicitudes-vacio">
                        <span className="solicitudes-vacio-icono">
                            ✓
                        </span>

                        <h3>No hay solicitudes pendientes</h3>

                        <p>
                            Las nuevas solicitudes de registro
                            aparecerán en esta sección.
                        </p>
                    </div>
                )
            ) : (
                <div className="solicitudes-tabla-contenedor">
                    <table className="solicitudes-tabla">
                        <thead>
                            <tr>
                                <th>Nombre y apellido</th>
                                <th>Correo electrónico</th>
                                <th>Estado</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>

                        <tbody>
                            {solicitudes.map(solicitud => (
                                <tr key={solicitud.idUsuario}>
                                    <td>
                                        <strong>
                                            {solicitud.nombre}{' '}
                                            {solicitud.apellido}
                                        </strong>
                                    </td>

                                    <td>{solicitud.email}</td>

                                    <td>
                                        <span className="solicitudes-pendiente">
                                            Pendiente
                                        </span>
                                    </td>

                                    <td>
                                        <div className="solicitudes-acciones">
                                            <button
                                                type="button"
                                                className="solicitudes-aprobar"
                                                disabled={procesando !== null}
                                                onClick={() =>
                                                    void gestionarSolicitud(
                                                        solicitud,
                                                        'aprobar'
                                                    )
                                                }
                                            >
                                                Aprobar
                                            </button>

                                            <button
                                                type="button"
                                                className="solicitudes-rechazar"
                                                disabled={procesando !== null}
                                                onClick={() =>
                                                    void gestionarSolicitud(
                                                        solicitud,
                                                        'rechazar'
                                                    )
                                                }
                                            >
                                                Rechazar
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    )
}