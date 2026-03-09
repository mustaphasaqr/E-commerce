import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'json', 'html'],
      exclude: [
        'node_modules/',
        'src/test/',
      ]
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@shared': path.resolve(__dirname, './src/shared'),
      '@auth': path.resolve(__dirname, './src/auth'),
      '@user': path.resolve(__dirname, './src/user'),
      '@product': path.resolve(__dirname, './src/product'),
      '@cart': path.resolve(__dirname, './src/cart'),
      '@order': path.resolve(__dirname, './src/order'),
      '@admin': path.resolve(__dirname, './src/admin'),
      '@observability': path.resolve(__dirname, './src/observability'),
      '@layout': path.resolve(__dirname, './src/layout'),
      '@store': path.resolve(__dirname, './src/store'),
    },
  },
})
