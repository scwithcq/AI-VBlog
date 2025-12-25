import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    // 自动导入 Vue 相关函数 (如 ref, reactive) 和 Element Plus 组件
    AutoImport({
      imports: ['vue', 'vue-router'],
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  server: {
    port: 5173, // 前端端口
    proxy: {
      // 代理配置：将 /api 开头的请求转发到 Java 后端 (8080端口)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 后端API本身包含/api前缀，所以不需要重写路径
        rewrite: (path) => path
      }
    }
  }
})