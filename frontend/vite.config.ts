import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  
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

  server: {
    port: 3001,
    open: true,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },

  build: {
    outDir: 'dist',
    sourcemap: process.env.NODE_ENV === 'development',
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: process.env.NODE_ENV === 'production',
      },
    },
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom', 'react-router-dom'],
          redux: ['@reduxjs/toolkit', 'react-redux'],
          utils: ['axios', 'date-fns', 'clsx'],
        },
      },
    },
  },
})
