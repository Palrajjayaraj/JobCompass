import { defineConfig } from 'vite'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import path from 'path'

export default defineConfig({
    plugins: [svelte()],
    resolve: {
        alias: {
            '$lib': path.resolve(__dirname, './src/lib'),
            '$components': path.resolve(__dirname, './src/components'),
            '$stores': path.resolve(__dirname, './src/stores'),
            '$types': path.resolve(__dirname, './src/types'),
            '$services': path.resolve(__dirname, './src/services'),
            '$utils': path.resolve(__dirname, './src/utils')
        }
    },
    server: {
        port: 8085,
        host: '0.0.0.0',
        proxy: {
            '/api/scraper': {
                target: 'http://scraper-service:8082',
                changeOrigin: true
            },
            '/api': {
                target: 'http://storage-service:8081',
                changeOrigin: true
            }
        }
    },
    build: {
        outDir: 'dist',
        sourcemap: true
    }
})
