import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      global: 'global/window'
    }
  },
  define: {
    global: 'window'
  },
  optimizeDeps: {
    include: ['global/window']
  },
  server: {
    port: 5173,
    proxy: {
      '/v1': 'http://localhost:8080'
    }
  }
});
