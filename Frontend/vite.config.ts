import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          const moduleId = id.replaceAll('\\', '/')
          if (!moduleId.includes('/node_modules/')) return
          if (moduleId.includes('/node_modules/echarts/')) return 'vendor-echarts'
          if (moduleId.includes('/node_modules/element-plus/')) {
            const componentMatch = moduleId.match(
              /\/node_modules\/element-plus\/es\/components\/([^/]+)\//
            )
            if (componentMatch) return `vendor-ep-${componentMatch[1]}`
            if (moduleId.includes('/node_modules/element-plus/es/hooks/')) return 'vendor-ep-hooks'
            if (moduleId.includes('/node_modules/element-plus/es/utils/')) return 'vendor-ep-utils'
            if (moduleId.includes('/node_modules/element-plus/es/directives/')) {
              return 'vendor-ep-directives'
            }
            if (moduleId.includes('/node_modules/element-plus/es/constants/')) {
              return 'vendor-ep-constants'
            }
            if (moduleId.includes('/node_modules/element-plus/es/tokens/')) return 'vendor-ep-tokens'
            if (moduleId.includes('/node_modules/element-plus/es/locale/')) return 'vendor-ep-locale'
            return 'vendor-element-plus'
          }
          if (moduleId.includes('/node_modules/@element-plus/icons-vue/')) {
            return 'vendor-element-icons'
          }
          if (
            moduleId.includes('/node_modules/vue/') ||
            moduleId.includes('/node_modules/vue-router/') ||
            moduleId.includes('/node_modules/pinia/')
          ) {
            return 'vendor-vue'
          }
          return 'vendor'
        }
      }
    }
  },
  server: {
    port: 5173
  },
  test: {
    environment: 'jsdom',
    globals: true
  }
})
