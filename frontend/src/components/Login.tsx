
import { useState } from 'react'
import type { FormEvent } from 'react'
import { apiUrl } from '../services/api.ts'

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
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState('')

  const iniciarSesion = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (cargando) return

    setCargando(true)
    setError('')

    try {
      // Convertimos las credenciales a Base64 para HTTP Basic.
      const bytes = new TextEncoder().encode(`${email.trim()}:${password}`)
      const binario = Array.from(bytes, (byte) =>
        String.fromCharCode(byte)
      ).join('')
      const autorizacion = `Basic ${btoa(binario)}`

      const respuesta = await fetch(
        apiUrl('/api/auth/me'),
        {
          method: 'GET',
          headers: {
            Authorization: autorizacion,
          },
        }
      )

      if (respuesta.status === 401) {
        throw new Error('Correo o contraseña incorrectos.')
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
        error instanceof Error
          ? error.message
          : 'Ocurrió un error al iniciar sesión.'
      )
    } finally {
      setCargando(false)
    }
  }

  return (
    <section className="panel">
      <form className="formulario-libro" onSubmit={iniciarSesion}>
        <h2>Iniciar sesión</h2>

        <label htmlFor="login-email">Correo electrónico</label>
        <input
          id="login-email"
          type="email"
          required
          autoComplete="username"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <label htmlFor="login-password">Contraseña</label>
        <input
          id="login-password"
          type="password"
          required
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        {error && (
          <p className="mensaje-error" role="alert">
            {error}
          </p>
        )}

        <button
          type="submit"
          className="boton-principal"
          disabled={cargando}
        >
          {cargando ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>
    </section>
  )
}
