import react from '@vitejs/plugin-react'
import { resolve } from 'node:path'
import { defineConfig } from 'vitest/config'

export default defineConfig(({ mode }) => {
  const input = mode === 'admin'
    ? { admin: resolve(__dirname, 'index.html') }
    : mode === 'shop'
      ? { shop: resolve(__dirname, 'shop.html') }
      : {
          admin: resolve(__dirname, 'index.html'),
          shop: resolve(__dirname, 'shop.html'),
        }

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': 'http://127.0.0.1:8080',
      },
    },
    build: {
      rollupOptions: {
        input,
      },
    },
    test: {
      environment: 'jsdom',
      setupFiles: './src/shared/test/setup.ts',
    },
  }
})
