import { useState } from 'react'
import type { FormEvent } from 'react'

interface RegistroProps {
    onVolverLogin: () => void
}

export default function Registro({ onVolverLogin }: RegistroProps) {
    const [nombre, setNombre] = useState('')
    const [apellido, setApellido] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [confirmarPassword, setConfirmarPassword] = useState('')
    const [mostrarPassword, setMostrarPassword] = useState(false)
    const [enviando, setEnviando] = useState(false)
    const [error, setError] = useState('')
    const [registroExitoso, setRegistroExitoso] = useState(false)

    const registrar = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault()

        if (enviando) return

        setError('')

        if (!nombre.trim() || !apellido.trim() || !email.trim()) {
            setError('Completá todos los campos obligatorios.')
            return
        }

        if (password.length < 8) {
            setError('La contraseña debe tener al menos 8 caracteres.')
            return
        }

        if (new TextEncoder().encode(password).length > 72) {
            setError('La contraseña supera la longitud máxima permitida.')
            return
        }

        if (password !== confirmarPassword) {
            setError('Las contraseñas no coinciden.')
            return
        }

        setEnviando(true)

        try {
            const respuesta = await fetch(
                'http://localhost:8080/api/auth/registro',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        nombre: nombre.trim(),
                        apellido: apellido.trim(),
                        email: email.trim().toLowerCase(),
                        password,
                    }),
                }
            )

            if (!respuesta.ok) {
                if (respuesta.status === 409) {
                    throw new Error(
                        'Ya existe una cuenta o solicitud con ese correo electrónico.'
                    )
                }

                if (respuesta.status === 400) {
                    throw new Error(
                        'Revisá los datos ingresados e intentá nuevamente.'
                    )
                }

                throw new Error(
                    'No se pudo enviar la solicitud de registro.'
                )
            }

            setPassword('')
            setConfirmarPassword('')
            setRegistroExitoso(true)

        } catch (error) {
            setError(
                error instanceof Error
                    ? error.message
                    : 'Ocurrió un error inesperado.'
            )
        } finally {
            setEnviando(false)
        }
    }

    return (
        <section className="registro-contenedor">
            <div className="registro-presentacion">
                <div className="registro-marca">
                    <span className="registro-icono">▤</span>
                    <strong>BiblioGest</strong>
                </div>

                <span className="registro-etiqueta">
                    BIBLIOTECA ESCOLAR
                </span>

                <h2>Tu próximo capítulo comienza acá.</h2>

                <p>
                    Formá parte de nuestra comunidad lectora
                    y descubrí todos los recursos disponibles
                    en la biblioteca.
                </p>

                <div className="registro-beneficios">
                    <div>
                        <span>✓</span>
                        Consultá el catálogo bibliográfico.
                    </div>

                    <div>
                        <span>✓</span>
                        Accedé a tus préstamos personales.
                    </div>

                    <div>
                        <span>✓</span>
                        Consultá tus fechas de devolución.
                    </div>
                </div>

                <small>
                    BiblioGest · Sistema de Gestión Bibliotecaria
                </small>
            </div>

            <div className="registro-panel">
                {registroExitoso ? (
                    <div className="registro-exito" role="status">
                        <div className="registro-exito-icono">
                            ✓
                        </div>

                        <h2>¡Solicitud enviada!</h2>

                        <p>
                            Tu solicitud de registro fue recibida
                            correctamente.
                        </p>

                        <div className="registro-aviso">
                            <strong>Estado: pendiente de aprobación</strong>

                            <p>
                                El bibliotecario deberá revisar y aprobar
                                tu solicitud antes de que puedas iniciar
                                sesión.
                            </p>
                        </div>

                        <button
                            type="button"
                            className="registro-boton"
                            onClick={onVolverLogin}
                        >
                            Volver a iniciar sesión
                        </button>
                    </div>
                ) : (
                    <>
                        <span className="registro-etiqueta">
                            NUEVA CUENTA
                        </span>

                        <h2>Creá tu cuenta</h2>

                        <p className="registro-subtitulo">
                            Completá tus datos para solicitar el acceso
                            a la biblioteca.
                        </p>

                        <form
                            className="registro-formulario"
                            onSubmit={registrar}
                        >
                            <div className="registro-fila">
                                <label>
                                    Nombre
                                    <input
                                        type="text"
                                        value={nombre}
                                        onChange={e => setNombre(e.target.value)}
                                        placeholder="Tu nombre"
                                        autoComplete="given-name"
                                        maxLength={100}
                                        required
                                    />
                                </label>

                                <label>
                                    Apellido
                                    <input
                                        type="text"
                                        value={apellido}
                                        onChange={e => setApellido(e.target.value)}
                                        placeholder="Tu apellido"
                                        autoComplete="family-name"
                                        maxLength={100}
                                        required
                                    />
                                </label>
                            </div>

                            <label>
                                Correo electrónico
                                <input
                                    type="email"
                                    value={email}
                                    onChange={e => setEmail(e.target.value)}
                                    placeholder="nombre@correo.com"
                                    autoComplete="email"
                                    maxLength={150}
                                    required
                                />
                            </label>

                            <label>
                                Contraseña
                                <input
                                    type={mostrarPassword ? 'text' : 'password'}
                                    value={password}
                                    onChange={e => setPassword(e.target.value)}
                                    placeholder="Mínimo 8 caracteres"
                                    autoComplete="new-password"
                                    minLength={8}
                                    maxLength={72}
                                    required
                                />
                            </label>

                            <label>
                                Confirmar contraseña
                                <input
                                    type={mostrarPassword ? 'text' : 'password'}
                                    value={confirmarPassword}
                                    onChange={e => setConfirmarPassword(e.target.value)}
                                    placeholder="Repetí tu contraseña"
                                    autoComplete="new-password"
                                    minLength={8}
                                    maxLength={72}
                                    required
                                />
                            </label>

                            <label className="registro-checkbox">
                                <input
                                    type="checkbox"
                                    checked={mostrarPassword}
                                    onChange={e => setMostrarPassword(e.target.checked)}
                                />
                                Mostrar contraseñas
                            </label>

                            <div className="registro-aviso">
                                <strong>Importante</strong>

                                <p>
                                    Tu cuenta deberá ser aprobada por
                                    el bibliotecario de la institución
                                    antes de que puedas ingresar.
                                </p>
                            </div>

                            {error && (
                                <div
                                    className="registro-error"
                                    role="alert"
                                >
                                    {error}
                                </div>
                            )}

                            <button
                                type="submit"
                                className="registro-boton"
                                disabled={enviando}
                            >
                                {enviando
                                    ? 'Enviando solicitud...'
                                    : 'Solicitar registro'}
                            </button>
                        </form>

                        <div className="registro-pie">
                            ¿Ya tenés una cuenta?

                            <button
                                type="button"
                                onClick={onVolverLogin}
                            >
                                Iniciar sesión
                            </button>
                        </div>
                    </>
                )}
            </div>
        </section>
    )
}