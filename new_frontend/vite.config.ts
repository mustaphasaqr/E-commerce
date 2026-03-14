import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

const __dirname = new URL('.', import.meta.url).pathname

export default defineConfig(({ command, mode }) => {
  // Load environment variables for proxy configuration
  const env = loadEnv(mode, '.', '')
  
  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': `${__dirname}src`,
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_API_URL || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
          ws: true,
          headers: {
            'Connection': 'upgrade',
          },
          rewrite: (path) => {
            console.log(`🔄 Proxy: ${path} → ${env.VITE_API_URL}${path}`)
            return path
          },
        }
      }
    }
  }
})
