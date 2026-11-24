import { useEffect, useState } from 'react'

type Opcion = {
    id: string
    etiqueta: string
    detalle?: string
}

type Props = {
    id: string
    valor: string
    opciones: Opcion[]
    placeholder: string
    onChange: (valor: string) => void
}

const normalizar = (texto: string) =>
    texto
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim()

export default function BuscadorSeleccion({
    id,
    valor,
    opciones,
    placeholder,
    onChange,
}: Props) {
    const [busqueda, setBusqueda] = useState('')
    const [abierto, setAbierto] = useState(false)
    const [indice, setIndice] = useState(-1)

    const seleccionada = opciones.find(
        (opcion) => opcion.id === valor
    )

    const etiquetaSeleccionada =
        seleccionada?.etiqueta ?? ''

    useEffect(() => {
        if (valor) {
            setBusqueda(etiquetaSeleccionada)
        }
    }, [valor, etiquetaSeleccionada])

    const palabras = normalizar(busqueda)
        .split(/\s+/)
        .filter(Boolean)

    const coincidencias = opciones.filter((opcion) => {
        const texto = normalizar(
            opcion.etiqueta + ' ' + (opcion.detalle ?? '')
        )

        return palabras.every(
            (palabra) => texto.includes(palabra)
        )
    })

    const resultados = coincidencias.slice(0, 30)

    const seleccionar = (opcion: Opcion) => {
        setBusqueda(opcion.etiqueta)
        onChange(opcion.id)
        setAbierto(false)
        setIndice(-1)
    }

    return (
        <div className="bg-buscador">
            <div className="bg-buscador-campo">
                <svg
                    width="19"
                    height="19"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    aria-hidden="true"
                >
                    <circle cx="11" cy="11" r="7" />
                    <path d="m20 20-4-4" />
                </svg>

                <input
                    id={id}
                    type="text"
                    role="combobox"
                    aria-autocomplete="list"
                    aria-expanded={abierto}
                    aria-controls={`${id}-resultados`}
                    aria-activedescendant={
                        abierto && indice >= 0
                            ? `${id}-opcion-${indice}`
                            : undefined
                    }
                    autoComplete="off"
                    placeholder={placeholder}
                    value={valor ? etiquetaSeleccionada : busqueda}
                    required
                    onFocus={() => setAbierto(true)}
                    onBlur={(e) => {
                        setAbierto(false)

                        e.currentTarget.setCustomValidity(
                            valor && seleccionada
                                ? ''
                                : 'Seleccioná una opción de la lista.'
                        )
                    }}
                    onChange={(e) => {
                        setBusqueda(e.target.value)
                        onChange('')
                        setAbierto(true)
                        setIndice(-1)

                        e.target.setCustomValidity(
                            'Seleccioná una opción de la lista.'
                        )
                    }}
                    onKeyDown={(e) => {
                        if (e.key === 'ArrowDown') {
                            e.preventDefault()
                            setAbierto(true)
                            setIndice((i) =>
                                Math.min(i + 1, resultados.length - 1)
                            )
                        }

                        if (e.key === 'ArrowUp') {
                            e.preventDefault()
                            setIndice((i) => Math.max(i - 1, 0))
                        }

                        if (e.key === 'Escape') {
                            setAbierto(false)
                        }

                        if (e.key === 'Enter' && abierto) {
                            e.preventDefault()

                            if (resultados[indice]) {
                                seleccionar(resultados[indice])
                                e.currentTarget.setCustomValidity('')
                            }
                        }
                    }}
                />

                {(busqueda || valor) && (
                    <button
                        type="button"
                        className="bg-buscador-limpiar"
                        aria-label="Limpiar selección"
                        onClick={() => {
                            setBusqueda('')
                            onChange('')
                            setIndice(-1)
                            setAbierto(false)
                        }}
                    >
                        ×
                    </button>
                )}
            </div>

            {abierto && (
                <div className="bg-buscador-lista">
                    <div
                        id={`${id}-resultados`}
                        role="listbox"
                    >
                        {resultados.map((opcion, i) => (
                            <div
                                key={opcion.id}
                                id={`${id}-opcion-${i}`}
                                role="option"
                                aria-selected={opcion.id === valor}
                                className={
                                    'bg-buscador-opcion' +
                                    (indice === i ? ' activa' : '')
                                }
                                onMouseDown={(e) => {
                                    e.preventDefault()
                                    seleccionar(opcion)

                                    const campo =
                                        document.getElementById(id)

                                    if (campo instanceof HTMLInputElement) {
                                        campo.setCustomValidity('')
                                    }
                                }}
                            >
                                <strong>{opcion.etiqueta}</strong>

                                {opcion.detalle && (
                                    <small>{opcion.detalle}</small>
                                )}
                            </div>
                        ))}
                    </div>

                    {resultados.length === 0 && (
                        <div className="bg-buscador-info">
                            No se encontraron coincidencias.
                        </div>
                    )}

                    {coincidencias.length > 30 && (
                        <div className="bg-buscador-info">
                            Mostrando 30 de {coincidencias.length}.
                            Escribí más letras para precisar la búsqueda.
                        </div>
                    )}
                </div>
            )}
        </div>
    )
}