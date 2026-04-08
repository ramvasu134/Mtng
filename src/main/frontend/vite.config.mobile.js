import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

/**
 * Vite config specifically for MOBILE (Capacitor) builds.
 * Builds into ./dist so Capacitor can pick it up.
 * The server URL points to your actual backend.
 */
export default defineConfig({
  plugins: [react()],
  root: '.',
  base: '/',
  build: {
    outDir: path.resolve(__dirname, 'dist'),
    emptyOutDir: true,
    rollupOptions: {
      output: {
        entryFileNames: 'assets/app-[hash].js',
        chunkFileNames: 'assets/chunk-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  }
});

