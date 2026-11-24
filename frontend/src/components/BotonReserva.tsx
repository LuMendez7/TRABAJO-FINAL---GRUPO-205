import { useState } from 'react'

type Props = {
    idLibro: number
    autorizacion: string
    disponibles: number
}

export default function BotonReserva({
    idLibro,
    autorizacion,
    disponibles
}: Props) {

    const [enviando, setEnviando] = useState(false)
    const [confirmada, setConfirmada] = useState(false)
    const [error, setError] = useState('')

    const solicitar = async () => {

        setEnviando(true)
        setError('')

        try {
            const respuesta = await fetch(
                'http://localhost:8080/api/reservas/solicitar',
                {
                    method: 'POST',
                    headers: {
                        Authorization: autorizacion,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ idLibro })
                }
            )

            if (!respuesta.ok) {
                const datos = await respuesta.json().catch(() => ({}))

                throw new Error(
                    datos.message ||
                    (respuesta.status === 409
                        ? 'Ya tenés una reserva activa, alcanzaste el límite o no hay ejemplares disponibles.'
                        : 'No se pudo realizar la reserva.')
                )
            }

            setConfirmada(true)

        } catch (e) {
            setError(
                e instanceof Error ? e.message : 'Error de conexión.'
            )
        } finally {
            setEnviando(false)
        }
    }

    return (
        <section className="bg-reserva-solicitud">

            <div className="bg-reserva-solicitud-titulo">
                <svg width="25" height="25" viewBox="0 0 24 24"
                    fill="none" stroke="currentColor" strokeWidth="1.8">
                    <path d="M5 3h14v18l-7-5-7 5z" />
                </svg>

                <div>
                    <strong>Reserva bibliográfica</strong>
                    <small>Solicitá tu próximo libro.</small>
                </div>
            </div>

            {confirmada ? (
                <p className="bg-reserva-exito" role="status">
                    ¡Reserva solicitada correctamente!
                    Podés consultar su estado en Mis reservas.
                </p>
            ) : (
                <>
                    <p>
                        Solicitá este título y aguardá la confirmación
                        del bibliotecario. Una vez aprobada la reserva,
                        tendrás 48 horas para retirar el ejemplar.
                    </p>

                    <button
                        type="button"
                        className="bg-reserva-boton"
                        disabled={enviando || disponibles === 0}
                        onClick={solicitar}
                    >
                        {enviando
                            ? 'Enviando solicitud...'
                            : disponibles === 0
                                ? 'Sin ejemplares disponibles'
                                : 'Reservar libro'}
                    </button>
                </>
            )}

            {error && (
                <p className="bg-reserva-error" role="alert">
                    {error}
                </p>
            )}
        </section>
    )
}