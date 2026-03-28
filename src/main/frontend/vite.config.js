import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  root: '.',
  base: '/',
  build: {
    outDir: path.resolve(__dirname, '../resources/static'),
    emptyOutDir: false,
    rollupOptions: {
      output: {
        entryFileNames: 'assets/app-[hash].js',
        chunkFileNames: 'assets/chunk-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  },
  server: {
    port: 3000,
    https: false,
    proxy: {
      '/api': { target: 'https://localhost:8443', secure: false, changeOrigin: true },
      '/ws':  { target: 'https://localhost:8443', secure: false, ws: true, changeOrigin: true }
    }
  }
});

