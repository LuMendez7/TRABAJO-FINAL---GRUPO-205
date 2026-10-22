import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { apiUrl } from '../services/api.ts'

const API = apiUrl('/api')

interface Autor {
    idAutor: number
    nombre: string
    apellido: string
}

interface Categoria {
    idCategoria: number
    nombre: string
    descripcion: string | null
}

interface Props {
    tipo: 'autores' | 'categorias'
    autorizacion: string
    onAutoresActualizados: (autores: Autor[]) => void
    onCategoriasActualizadas: (categorias: Categoria[]) => void
}

type DatosFormulario = { nombre: string; apellido: string; descripcion: string }

function mensajeHttp(status: number, mensajeServidor?: string) {
    if (status === 400) return mensajeServidor || 'Revisá los datos ingresados.'
    if (status === 401) return 'La sesión venció. Iniciá sesión nuevamente.'
    if (status === 403) return 'No tenés permiso para realizar esta operación.'
    if (status === 404) return 'El registro ya no existe. Actualizá la lista.'
    if (status === 409) return mensajeServidor || 'La operación entra en conflicto con datos existentes.'
    return mensajeServidor || `No se pudo completar la operación (HTTP ${status}).`
}

async function detalleError(respuesta: Response) {
    try {
        const cuerpo = await respuesta.json() as { mensaje?: string; message?: string }
        return cuerpo.mensaje || cuerpo.message
    } catch {
        return undefined
    }
}

export default function GestionAutoresCategorias({
    tipo,
    autorizacion,
    onAutoresActualizados,
    onCategoriasActualizadas,
}: Props) {
    const esAutor = tipo === 'autores'
    const recurso = esAutor ? 'autores' : 'categorias'
    const [autores, setAutores] = useState<Autor[]>([])
    const [categorias, setCategorias] = useState<Categoria[]>([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState('')
    const [mensaje, setMensaje] = useState('')
    const [formularioAbierto, setFormularioAbierto] = useState(false)
    const [registroEditando, setRegistroEditando] = useState<number | null>(null)
    const [guardando, setGuardando] = useState(false)
    const [datos, setDatos] = useState<DatosFormulario>({ nombre: '', apellido: '', descripcion: '' })

    const cargar = useCallback(async () => {
        setError('')
        try {
            const respuesta = await fetch(`${API}/${recurso}`)
            if (!respuesta.ok) throw new Error(mensajeHttp(respuesta.status, await detalleError(respuesta)))
            if (esAutor) {
                const lista = await respuesta.json() as Autor[]
                lista.sort((a, b) => a.apellido.localeCompare(b.apellido, 'es')
                    || a.nombre.localeCompare(b.nombre, 'es') || a.idAutor - b.idAutor)
                setAutores(lista)
                onAutoresActualizados(lista)
            } else {
                const lista = await respuesta.json() as Categoria[]
                setCategorias(lista)
                onCategoriasActualizadas(lista)
            }
        } catch (causa) {
            setError(causa instanceof Error ? causa.message : 'No se pudo conectar con el servidor.')
        } finally {
            setCargando(false)
        }
    }, [esAutor, onAutoresActualizados, onCategoriasActualizadas, recurso])

    useEffect(() => {
        const temporizador = window.setTimeout(() => { void cargar() }, 0)
        return () => window.clearTimeout(temporizador)
    }, [cargar])

    const iniciarAlta = () => {
        setRegistroEditando(null)
        setDatos({ nombre: '', apellido: '', descripcion: '' })
        setError('')
        setMensaje('')
        setFormularioAbierto(true)
    }

    const iniciarEdicion = (registro: Autor | Categoria) => {
        setRegistroEditando(esAutor ? (registro as Autor).idAutor : (registro as Categoria).idCategoria)
        setDatos({
            nombre: registro.nombre,
            apellido: esAutor ? (registro as Autor).apellido : '',
            descripcion: esAutor ? '' : (registro as Categoria).descripcion || '',
        })
        setError('')
        setMensaje('')
        setFormularioAbierto(true)
    }

    const cancelar = () => {
        setFormularioAbierto(false)
        setRegistroEditando(null)
        setDatos({ nombre: '', apellido: '', descripcion: '' })
        setError('')
    }

    const guardar = async (evento: FormEvent<HTMLFormElement>) => {
        evento.preventDefault()
        if (guardando) return
        const nombre = datos.nombre.trim()
        const apellido = datos.apellido.trim()
        const descripcion = datos.descripcion.trim()
        if (!nombre || nombre.length > 100 || (esAutor && (!apellido || apellido.length > 100))
            || (!esAutor && descripcion.length > 255)) {
            setError(esAutor
                ? 'Nombre y apellido son obligatorios y admiten hasta 100 caracteres.'
                : 'El nombre es obligatorio (hasta 100 caracteres) y la descripción admite hasta 255 caracteres.')
            return
        }

        setGuardando(true)
        setError('')
        setMensaje('')
        const cuerpo = esAutor ? { nombre, apellido } : { nombre, descripcion: descripcion || null }
        const metodo = registroEditando === null ? 'POST' : 'PUT'
        const url = registroEditando === null
            ? `${API}/${recurso}`
            : `${API}/${recurso}/${registroEditando}`
        try {
            const respuesta = await fetch(url, {
                method: metodo,
                headers: { 'Content-Type': 'application/json', Authorization: autorizacion },
                body: JSON.stringify(cuerpo),
            })
            if (!respuesta.ok) {
                setError(mensajeHttp(respuesta.status, await detalleError(respuesta)))
                return
            }
            setMensaje(esAutor ? 'Autor guardado correctamente.' : 'Categoría guardada correctamente.')
            setFormularioAbierto(false)
            setRegistroEditando(null)
            setDatos({ nombre: '', apellido: '', descripcion: '' })
            await cargar()
        } catch {
            setError('No se pudo conectar con el servidor. Revisá la conexión e intentá nuevamente.')
        } finally {
            setGuardando(false)
        }
    }

    return (
        <section className="panel gestion-catalogo">
            <h2>{esAutor ? 'Autores' : 'Categorías'}</h2>
            <button type="button" className="boton-principal" onClick={iniciarAlta} disabled={guardando}>
                Nuevo {esAutor ? 'autor' : 'categoría'}
            </button>
            {mensaje && <p role="status">{mensaje}</p>}
            {error && <p className="mensaje-error" role="alert">{error}</p>}

            {formularioAbierto && (
                <form className="formulario-libro" onSubmit={guardar}>
                    <h3>{registroEditando === null ? 'Alta' : 'Editar'} {esAutor ? 'autor' : 'categoría'}</h3>
                    <label htmlFor={`${tipo}-nombre`}>Nombre *</label>
                    <input id={`${tipo}-nombre`} value={datos.nombre} maxLength={100}
                        onChange={(evento) => setDatos({ ...datos, nombre: evento.target.value })} required />
                    {esAutor ? (
                        <>
                            <label htmlFor="autores-apellido">Apellido *</label>
                            <input id="autores-apellido" value={datos.apellido} maxLength={100}
                                onChange={(evento) => setDatos({ ...datos, apellido: evento.target.value })} required />
                        </>
                    ) : (
                        <>
                            <label htmlFor="categorias-descripcion">Descripción</label>
                            <input id="categorias-descripcion" value={datos.descripcion} maxLength={255}
                                onChange={(evento) => setDatos({ ...datos, descripcion: evento.target.value })} />
                        </>
                    )}
                    <button className="boton-principal" type="submit" disabled={guardando}>
                        {guardando ? 'Guardando…' : 'Guardar'}
                    </button>
                    <button type="button" onClick={cancelar} disabled={guardando}>Cancelar</button>
                </form>
            )}

            {cargando ? <p role="status">Cargando {recurso}…</p> : (
                esAutor ? (
                    autores.length === 0 ? <p className="mis-prestamos-vacio">No hay autores registrados.</p> :
                        <div className="tabla-contenedor"><table><thead><tr><th>ID</th><th>Nombre</th><th>Apellido</th><th>Acciones</th></tr></thead>
                            <tbody>{autores.map((autor) => <tr key={autor.idAutor}>
                                <td>{autor.idAutor}</td><td>{autor.nombre}</td><td>{autor.apellido}</td>
                                <td><button type="button" onClick={() => iniciarEdicion(autor)} disabled={guardando}>Editar</button></td>
                            </tr>)}</tbody></table></div>
                ) : (
                    categorias.length === 0 ? <p className="mis-prestamos-vacio">No hay categorías registradas.</p> :
                        <div className="tabla-contenedor"><table><thead><tr><th>ID</th><th>Nombre</th><th>Descripción</th><th>Acciones</th></tr></thead>
                            <tbody>{categorias.map((categoria) => <tr key={categoria.idCategoria}>
                                <td>{categoria.idCategoria}</td><td>{categoria.nombre}</td><td>{categoria.descripcion || '—'}</td>
                                <td><button type="button" onClick={() => iniciarEdicion(categoria)} disabled={guardando}>Editar</button></td>
                            </tr>)}</tbody></table></div>
                )
            )}
        </section>
    )
}
