import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Forward API calls to the Spring Boot backend so the browser
    // only ever talks to the Vite dev server (no CORS headaches).
    proxy: {
      '/api': 'http://localhost:8081',
    },
  },
})
