import { useEffect, useState } from 'react'

type Props = {
    idLibro: number
    titulo: string
    autor?: string
    categoria?: string
}

const paletas = [
    ['#142d46', '#294e70', '#e9d5a2'],
    ['#512632', '#874657', '#f5d9b5'],
    ['#173e35', '#386859', '#e8d8aa'],
    ['#302747', '#60517c', '#e5d8f2'],
    ['#523522', '#956643', '#f4dfb7'],
    ['#183d4c', '#347384', '#d4eceb'],
    ['#393b2b', '#73774c', '#eee4b8'],
    ['#292e3b', '#525e75', '#e5e9f0'],
]

function PortadaDigital({
    idLibro,
    titulo,
    autor,
    categoria,
}: Props) {

    const [fondo, secundario, detalle] =
        paletas[Math.abs(idLibro) % paletas.length]

    const tamano = titulo.length > 90
        ? '0.82rem'
        : titulo.length > 55
            ? '1rem'
            : titulo.length > 30
                ? '1.2rem'
                : '1.45rem'

    return (
        <div className="portada-digital-contenedor">
            <div
                className="portada-digital"
                role="img"
                aria-label={`Portada ilustrativa de ${titulo}`}
                style={{
                    background: `linear-gradient(145deg, ${fondo}, ${secundario})`,
                    color: detalle,
                }}
            >
                <div className="pd-lomo" />
                <div className="pd-marco" />

                <div className="pd-categoria">
                    {categoria || 'Coleccion bibliografica'}
                </div>

                <svg
                    className="pd-ilustracion"
                    viewBox="0 0 120 85"
                    fill="none"
                    stroke="currentColor"
                    aria-hidden="true"
                >
                    <circle cx="60" cy="42" r="37" opacity=".25" />
                    <circle cx="60" cy="42" r="29" opacity=".4" />

                    <path
                        d="M22 25 Q42 16 60 33 Q78 16 98 25
                           L98 61 Q78 52 60 68 Q42 52 22 61 Z"
                        strokeWidth="1.8"
                        strokeLinejoin="round"
                    />

                    <path d="M60 33 V68" strokeWidth="1.8" />

                    <path
                        d="M30 35 Q43 29 52 38
                           M30 44 Q43 38 52 47
                           M68 38 Q77 29 90 35
                           M68 47 Q77 38 90 44"
                        opacity=".65"
                    />
                </svg>

                <div
                    className="pd-titulo"
                    style={{ fontSize: tamano }}
                >
                    {titulo}
                </div>

                <div className="pd-separador" />

                <div className="pd-autor">
                    {autor || 'Autor no registrado'}
                </div>

                <div className="pd-editorial">
                    BIBLIOGEST
                    <small>Portada ilustrativa</small>
                </div>
            </div>
        </div>
    )
}

export default function PortadaLibro(props: Props) {

    const { idLibro, titulo } = props

    const [url, setUrl] = useState('')
    const [error, setError] = useState(false)
    const [cargando, setCargando] = useState(true)

    useEffect(() => {

        let controlador: AbortController

        const cargar = async () => {

            controlador?.abort()
            controlador = new AbortController()

            const signal = controlador.signal

            setCargando(true)
            setError(false)
            setUrl('')

            try {

                const api =
                    `http://localhost:8080/api/fichas/${idLibro}`

                const respuesta = await fetch(api, { signal })

                if (!respuesta.ok) throw new Error()

                const ficha = await respuesta.json()

                if (signal.aborted) return

                if (ficha.portadaEliminada) {
                    setUrl('')
                } else if (ficha.tienePortada) {
                    setUrl(`${api}/portada?v=${Date.now()}`)
                } else if ([129, 131].includes(idLibro)) {
                    setUrl(`/portadas/${idLibro}.jpg`)
                }

            } catch {

                if (!signal.aborted) {
                    setUrl('')
                }

            } finally {

                if (!signal.aborted) {
                    setCargando(false)
                }
            }
        }

        void cargar()

        window.addEventListener(
            'bibliogest-ficha-actualizada', cargar
        )

        return () => {

            controlador?.abort()

            window.removeEventListener(
                'bibliogest-ficha-actualizada', cargar
            )
        }

    }, [idLibro])

    if (cargando || error || !url) {
        return <PortadaDigital {...props} />
    }

    return (
        <div className="portada-contenedor">
            <img
                className="portada-original"
                src={url}
                alt={`Portada de ${titulo}`}
                loading="lazy"
                onError={() => setError(true)}
            />
        </div>
    )
}