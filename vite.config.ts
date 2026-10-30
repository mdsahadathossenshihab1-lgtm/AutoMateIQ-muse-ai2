import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // This allows usage of process.env.API_KEY in client-side code by replacing it with Vite's env variable at build time.
    'process.env.API_KEY': 'import.meta.env.VITE_API_KEY'
  }
});