# BiblioGest frontend

## Requisitos y comandos

- Node.js `^20.19.0` o `>=22.12.0`, según el requisito de Vite instalado.
- npm compatible con esa versión de Node. Esta revisión se ejecutó con Node `v24.19.0` y npm `11.17.0`.

Desde `frontend`, instalar exactamente las dependencias del lockfile y arrancar Vite:

```powershell
npm.cmd ci
npm.cmd run dev
```

La API usa `http://localhost:8080` si `VITE_API_URL` no está definida.

Para usar otra API, copiá `.env.example` a `.env.local` y ajustá solo el origen:

```dotenv
VITE_API_URL=https://api.ejemplo.test
```

`.env.local` está ignorado por Git. `.env.example` documenta el valor local. `VITE_API_URL` se incorpora al bundle del
navegador: no coloques contraseñas, claves ni otras credenciales en variables
`VITE_`. Configurá la URL antes de ejecutar `npm.cmd run build` para ese entorno.

Comandos de verificación desde `frontend`:

```powershell
npm.cmd run lint
npm.cmd run build
```

## Pantallas y permisos

La aplicación incluye las vistas Inicio, Catálogo, Iniciar sesión, Mis préstamos, Préstamos, Usuarios, Autores y Categorías. `Mis préstamos` se ofrece al rol `LECTOR`; Préstamos, Usuarios, Autores y Categorías al rol `BIBLIOTECARIO`. El backend verifica los permisos aunque se navegue directamente a una ruta. El historial SPA usa las rutas `/`, `/catalogo`, `/login`, `/mis-prestamos`, `/prestamos`, `/usuarios`, `/autores` y `/categorias`.

En F09 se aprobaron en navegador los flujos documentados de usuarios, autores, categorías, libros, ejemplares y circulación. También se verificaron `VITE_API_URL`, rutas, historial Atrás/Adelante y navegación por teclado. Recargar con F5 requiere iniciar sesión nuevamente para ambos roles; es una limitación aceptada del MVP. El detalle por criterio, incluida la pantalla de vencidos sin afirmar una comprobación manual de un vencimiento real, está en `../docs/VERIFICACION_MVP.md`.

La autenticación HTTP Basic de este MVP se usa para desarrollo local. Antes de
exponer el frontend y la API en otro entorno, ambos deben usar HTTPS y el
backend debe permitir explícitamente el origen del frontend en CORS. Esta
configuración no convierte al proyecto en un despliegue listo para producción.

# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

You can also install [eslint-plugin-react-x](https://npmx.dev/package/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://npmx.dev/package/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```
