import react from '@vitejs/plugin-react'
import { resolve } from 'node:path'
import { defineConfig } from 'vitest/config'

export default defineConfig({
  root: __dirname,
  cacheDir: '../node_modules/.vite-admin',
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/admin': 'http://127.0.0.1:8082',
      '/api/shop': 'http://127.0.0.1:8081',
    },
  },
  build: {
    outDir: '../dist/admin',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        admin: resolve(__dirname, 'index.html'),
      },
    },
  },
})
