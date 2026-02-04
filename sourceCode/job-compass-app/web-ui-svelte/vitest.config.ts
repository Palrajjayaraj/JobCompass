import { defineConfig } from 'vitest/config'
import { svelte } from '@sveltejs/vite-plugin-svelte'
import path from 'path'

export default defineConfig({
    plugins: [svelte({ hot: !process.env.VITEST })],
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
    test: {
        globals: true,
        environment: 'jsdom',
        setupFiles: ['./src/tests/setup.ts'],
        coverage: {
            provider: 'v8',
            reporter: ['text', 'json', 'html'],
            exclude: [
                'node_modules/',
                'src/tests/',
                '**/*.test.ts',
                '**/*.spec.ts',
                'vite.config.ts',
                'vitest.config.ts'
            ]
        }
    }
})
