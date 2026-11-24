import { useEffect, useState } from 'react'

type Props = {
    idLibro: number
    autorizacion: string
    descripcion: string
    onGuardar: () => void
    onCancelar: () => void
}

export default function EditorFichaLibro({
    idLibro,
    autorizacion,
    descripcion,
    onGuardar,
    onCancelar
}: Props) {

    const [texto, setTexto] = useState(descripcion)
    const [archivo, setArchivo] = useState<File | null>(null)
    const [preview, setPreview] = useState('')
    const [eliminar, setEliminar] = useState(false)
    const [guardando, setGuardando] = useState(false)
    const [error, setError] = useState('')

    const api = `http://localhost:8080/api/fichas/${idLibro}`

    useEffect(() => {
        if (!archivo) {
            setPreview('')
            return
        }

        const url = URL.createObjectURL(archivo)
        setPreview(url)

        return () => URL.revokeObjectURL(url)
    }, [archivo])

    const seleccionar = (nuevo: File | undefined) => {
        if (!nuevo) return

        setError('')

        if (!['image/jpeg', 'image/png'].includes(nuevo.type)) {
            setError('Seleccioná una imagen JPG o PNG.')
            return
        }

        if (nuevo.size > 5 * 1024 * 1024) {
            setError('La imagen no puede superar los 5 MB.')
            return
        }

        setArchivo(nuevo)
        setEliminar(false)
    }

    const guardar = async () => {
        setGuardando(true)
        setError('')

        try {
            if (archivo) {
                const formulario = new FormData()
                formulario.append('archivo', archivo)

                const respuesta = await fetch(`${api}/portada`, {
                    method: 'PUT',
                    headers: { Authorization: autorizacion },
                    body: formulario
                })

                if (!respuesta.ok) {
                    throw new Error('No se pudo guardar la portada.')
                }
            }

            if (eliminar) {
                const respuesta = await fetch(`${api}/portada`, {
                    method: 'DELETE',
                    headers: { Authorization: autorizacion }
                })

                if (!respuesta.ok) {
                    throw new Error('No se pudo eliminar la portada.')
                }
            }

            const respuesta = await fetch(`${api}/descripcion`, {
                method: 'PUT',
                headers: {
                    Authorization: autorizacion,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ descripcion: texto })
            })

            if (!respuesta.ok) {
                throw new Error('No se pudo guardar la descripción.')
            }

            onGuardar()

        } catch (e) {
            setError(e instanceof Error
                ? e.message + ' Revisá los cambios antes de reintentar.'
                : 'Error al guardar los cambios.')

            window.dispatchEvent(new Event('bibliogest-ficha-actualizada'))
        } finally {
            setGuardando(false)
        }
    }

    return (
        <section className="bg-editor-ficha">

            <div className="bg-editor-titulo">
                <small>ADMINISTRACIÓN BIBLIOGRÁFICA</small>
                <h3>Editar portada y descripción</h3>
                <p>Personalizá la ficha del libro.</p>
            </div>

            <label className="bg-editor-carga">
                <strong>Seleccionar nueva portada</strong>
                <span>Archivos JPG o PNG de hasta 5 MB</span>

                <input
                    type="file"
                    accept="image/jpeg,image/png"
                    disabled={guardando}
                    onChange={e => seleccionar(e.target.files?.[0])}
                />
            </label>

            {preview && (
                <div className="bg-editor-preview">
                    <img src={preview} alt="Vista previa" />
                    <small>Vista previa de la nueva portada</small>
                </div>
            )}

            <button
                type="button"
                className="bg-editor-eliminar"
                disabled={guardando}
                onClick={() => {
                    setEliminar(!eliminar)
                    setArchivo(null)
                }}
            >
                {eliminar
                    ? 'Cancelar eliminación de portada'
                    : 'Eliminar portada actual'}
            </button>

            {eliminar && (
                <p className="bg-editor-aviso">
                    La portada se eliminará al guardar los cambios.
                </p>
            )}

            <label className="bg-editor-label">
                Descripción del libro

                <textarea
                    rows={6}
                    maxLength={5000}
                    value={texto}
                    disabled={guardando}
                    onChange={e => setTexto(e.target.value)}
                    placeholder="Escribí una breve descripción del libro..."
                />
            </label>

            <small>{texto.length} / 5000 caracteres</small>

            {error && (
                <p role="alert" className="bg-editor-aviso">{error}</p>
            )}

            <div className="bg-editor-acciones">
                <button
                    type="button"
                    disabled={guardando}
                    onClick={onCancelar}
                >
                    Cancelar
                </button>

                <button
                    type="button"
                    className="bg-editor-guardar"
                    disabled={guardando}
                    onClick={guardar}
                >
                    {guardando ? 'Guardando...' : 'Guardar cambios'}
                </button>
            </div>
        </section>
    )
}