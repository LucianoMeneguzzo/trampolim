import react from '@vitejs/plugin-react'
import { cwd } from 'node:process'
import { defineConfig, loadEnv } from 'vite'

// Content-Security-Policy do site publicado: diz ao navegador de onde o site pode carregar
// scripts, estilos e fontes, e com qual servidor ele pode conversar (só a nossa API).
// É a principal defesa contra XSS, importante aqui porque o token de login fica no localStorage:
// mesmo que alguém consiga injetar um <script> na página, o navegador se recusa a executá-lo.
// Entra só no build: em desenvolvimento o Vite precisa de scripts inline para recarregar a tela.
function politicaDeSeguranca(apiUrl) {
  const politica = [
    "default-src 'self'",
    "script-src 'self'",
    "style-src 'self' https://fonts.googleapis.com",
    "font-src 'self' https://fonts.gstatic.com",
    "img-src 'self' data:",
    `connect-src 'self' ${new URL(apiUrl).origin}`,
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
  ].join('; ')

  return {
    name: 'politica-de-seguranca',
    apply: 'build',
    transformIndexHtml() {
      return [{
        tag: 'meta',
        attrs: { 'http-equiv': 'Content-Security-Policy', content: politica },
        injectTo: 'head-prepend',
      }]
    },
  }
}

export default defineConfig(({ mode }) => {
  // Mesmo valor que o services/api.js usa: vem do ambiente ou do arquivo .env
  const { VITE_API_URL } = loadEnv(mode, cwd(), 'VITE_')

  return {
    plugins: [react(), politicaDeSeguranca(VITE_API_URL || 'http://localhost:8080')],
    test: {
      globals: true,
      environment: 'jsdom',
      setupFiles: './src/setupTests.js',
      // npm run coverage: o critério de avaliação pede no mínimo 70%
      coverage: {
        include: ['src/**/*.{js,jsx}'],
        exclude: ['src/main.jsx', 'src/setupTests.js', 'src/test/**', 'src/**/*.test.{js,jsx}'],
        thresholds: { statements: 70, branches: 70, functions: 70, lines: 70 },
      },
    },
  }
})
