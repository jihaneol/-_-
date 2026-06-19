import react from '@vitejs/plugin-react'
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import type { Plugin } from 'vite'
import { defineConfig } from 'vitest/config'

export default defineConfig(({ mode }) => {
  const isShop = mode === 'shop'
  const input = mode === 'admin'
    ? { admin: resolve(__dirname, 'index.html') }
    : mode === 'shop'
      ? { shop: resolve(__dirname, 'shop.html') }
      : {
          admin: resolve(__dirname, 'index.html'),
          shop: resolve(__dirname, 'shop.html'),
        }

  const rootEntryPlugin = (): Plugin => ({
    name: 'root-entry-by-mode',
    enforce: 'pre',
    configureServer(server) {
      server.middlewares.use(async (request, response, next) => {
        if (isShop && (request.url === '/' || request.url === '/index.html')) {
          const template = readFileSync(resolve(__dirname, 'shop.html'), 'utf-8')
          const html = await server.transformIndexHtml('/shop.html', template, request.originalUrl)
          response.statusCode = 200
          response.setHeader('Content-Type', 'text/html')
          response.end(html)
          return
        }

        next()
      })
    },
    transformIndexHtml(html) {
      if (!isShop) {
        return html
      }

      return html
        .replace('Coupon Admin', 'Coupon Shop')
        .replace('/apps/admin/main.tsx', '/apps/shop/main.tsx')
    },
  })

  return {
    cacheDir: isShop ? 'node_modules/.vite-shop' : 'node_modules/.vite-admin',
    plugins: [rootEntryPlugin(), react()],
    server: {
      port: isShop ? 5174 : 5173,
      proxy: {
        '/api/admin': 'http://127.0.0.1:8082',
        '/api/shop': 'http://127.0.0.1:8081',
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
