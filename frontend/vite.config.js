import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
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
  }
})
