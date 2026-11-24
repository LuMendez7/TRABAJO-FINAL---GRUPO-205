
import { useState } from 'react'
import type { FormEvent } from 'react'
import { Link } from 'react-router-dom'

export interface Sesion {
  email: string
  rol: string
  autorizacion: string
}

interface LoginProps {
  onLogin: (sesion: Sesion) => void
}

export default function Login({ onLogin }: LoginProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [mostrarPassword, setMostrarPassword] = useState(false)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState('')

  const iniciarSesion = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (cargando) return

    setCargando(true)
    setError('')

    try {
      const bytes = new TextEncoder().encode(
        `${email.trim()}:${password}`
      )

      const binario = Array.from(bytes, (byte) =>
        String.fromCharCode(byte)
      ).join('')

      const autorizacion = `Basic ${btoa(binario)}`

      const respuesta = await fetch(
        'http://localhost:8080/api/auth/me',
        {
          method: 'GET',
          headers: {
            Authorization: autorizacion,
          },
        }
      )

      if (respuesta.status === 401) {
        throw new Error('Credenciales incorrectas o cuenta no habilitada. Si solicitaste tu registro recientemente, consultá al bibliotecario.')
      }

      if (!respuesta.ok) {
        throw new Error('No se pudo iniciar sesión.')
      }

      const usuario: { email: string; rol: string } =
        await respuesta.json()

      if (!['BIBLIOTECARIO', 'LECTOR'].includes(usuario.rol)) {
        throw new Error('El usuario no tiene un rol válido.')
      }

      onLogin({
        email: usuario.email,
        rol: usuario.rol,
        autorizacion,
      })

      setPassword('')
    } catch (error) {
      setError(
        error instanceof TypeError
          ? 'No se pudo conectar con el servidor. Intentá nuevamente.'
          : error instanceof Error
            ? error.message
            : 'Ocurrió un error al iniciar sesión.'
      )
    } finally {
      setCargando(false)
    }
  }

  return (
    <section className="acceso-pagina">
      <div className="acceso-contenedor">

        <aside className="acceso-institucional">
          <div className="acceso-marca">
            <svg
              width="42"
              height="42"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              aria-hidden="true"
            >
              <path d="M12 7v14M3 18V4c4-1 7 0 9 3 2-3 5-4 9-3v14c-4-1-7 0-9 3-2-3-5-4-9-3Z" />
            </svg>
            <span>BiblioGest</span>
          </div>

          <div className="acceso-presentacion">
            <span className="acceso-etiqueta">
              BIBLIOTECA ESCOLAR DIGITAL
            </span>

            <h2>
              El conocimiento,
              <br />
              más cerca de vos.
            </h2>

            <p>
              Un espacio para descubrir nuevas lecturas,
              acceder a recursos educativos y disfrutar
              de los servicios de tu biblioteca.
            </p>

            <div className="acceso-beneficios">
              <div>
                <span aria-hidden="true">✓</span>
                Consultá el catálogo bibliográfico
              </div>
              <div>
                <span aria-hidden="true">✓</span>
                Accedé a tus préstamos
              </div>
              <div>
                <span aria-hidden="true">✓</span>
                Descubrí recursos para aprender
              </div>
            </div>
          </div>

          <div className="acceso-pie-institucional">
            Educación, lectura y comunidad.
          </div>
        </aside>

        <div className="acceso-formulario-panel">

          <Link to="/catalogo" className="acceso-volver">
            ← Volver al catálogo
          </Link>

          <div className="acceso-titulo">
            <span className="acceso-subtitulo">
              ACCESO A MI CUENTA
            </span>

            <h2>¡Bienvenido!</h2>

            <p>
              Ingresá tus credenciales para acceder
              a los servicios de BiblioGest.
            </p>
          </div>

          <form
            className="acceso-formulario"
            onSubmit={iniciarSesion}
          >
            <div className="acceso-campo">
              <label htmlFor="login-email">
                Correo electrónico
              </label>

              <div className="acceso-input">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.7"
                  aria-hidden="true"
                >
                  <rect x="3" y="5" width="18" height="14" rx="2" />
                  <path d="m3 7 9 6 9-6" />
                </svg>

                <input
                  id="login-email"
                  type="email"
                  placeholder="nombre@correo.com"
                  required
                  autoComplete="username"
                  value={email}
                  disabled={cargando}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>

            <div className="acceso-campo">
              <label htmlFor="login-password">
                Contraseña
              </label>

              <div className="acceso-input">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="1.7"
                  aria-hidden="true"
                >
                  <rect x="5" y="10" width="14" height="11" rx="2" />
                  <path d="M8 10V7a4 4 0 0 1 8 0v3" />
                </svg>

                <input
                  id="login-password"
                  type={mostrarPassword ? 'text' : 'password'}
                  placeholder="Ingresá tu contraseña"
                  required
                  autoComplete="current-password"
                  value={password}
                  disabled={cargando}
                  onChange={(e) => setPassword(e.target.value)}
                />

                <button
                  type="button"
                  className="acceso-ver-password"
                  aria-label={
                    mostrarPassword
                      ? 'Ocultar contraseña'
                      : 'Mostrar contraseña'
                  }
                  aria-pressed={mostrarPassword}
                  onClick={() =>
                    setMostrarPassword(!mostrarPassword)
                  }
                >
                  <svg
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.7"
                    aria-hidden="true"
                  >
                    <path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z" />
                    <circle cx="12" cy="12" r="3" />
                    {mostrarPassword && (
                      <path d="m3 3 18 18" />
                    )}
                  </svg>
                </button>
              </div>
            </div>

            {error && (
              <div className="acceso-error" role="alert">
                {error}
              </div>
            )}

            <button
              type="submit"
              className="acceso-boton"
              disabled={cargando}
            >
              {cargando && (
                <span
                  className="acceso-spinner"
                  aria-hidden="true"
                />
              )}

              {cargando ? 'Ingresando...' : 'Iniciar sesión'}
            </button>
          </form>

          <div className="acceso-registro">
            <strong>¿Todavía no tenés una cuenta?</strong>
            <p>
              Registrate para acceder a los servicios de tu biblioteca.
            </p>
            <p>
              <Link to="/registro" className="acceso-crear-cuenta">
                Crear una cuenta
              </Link>
            </p>
          </div>

          <div className="acceso-seguridad">
            Acceso exclusivo para usuarios autorizados.
          </div>

        </div>
      </div>
    </section>
  )
}
