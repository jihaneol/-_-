import react from '@vitejs/plugin-react'
import { resolve } from 'node:path'
import type { Plugin } from 'vite'
import { defineConfig } from 'vitest/config'

const rejectLegacyShopHtml = (): Plugin => ({
  name: 'reject-legacy-shop-html',
  enforce: 'pre',
  configureServer(server) {
    server.middlewares.use((request, response, next) => {
      if (!request.url?.startsWith('/shop.html')) {
        next()
        return
      }

      response.statusCode = 404
      response.end('Not found')
    })
  },
})

export default defineConfig({
  root: __dirname,
  cacheDir: '../node_modules/.vite-shop',
  plugins: [rejectLegacyShopHtml(), react()],
  server: {
    port: 5174,
    proxy: {
      '/api/shop': 'http://127.0.0.1:8081',
    },
  },
  build: {
    outDir: '../dist/shop',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        shop: resolve(__dirname, 'index.html'),
      },
    },
  },
})
