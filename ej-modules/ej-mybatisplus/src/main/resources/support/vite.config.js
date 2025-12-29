import {defineConfig, loadEnv} from 'vite'
import react from '@vitejs/plugin-react'
import {visualizer} from 'rollup-plugin-visualizer'

// https://vite.dev/config/
export default defineConfig((conf) => {
    const env = loadEnv(conf.mode, process.cwd(), 'VITE_');
    // 2. 配置base：优先用环境变量，默认值设为 ./（解决路径从/改./的核心需求）
    const baseUrl = env.VITE_BASE_URL;
    return {
        server: {
            proxy: {
                // with options
                '/api': {
                    target: 'http://127.0.0.1:9214/e4j/cg',
                    changeOrigin: true,
                    rewrite: path => path.replace(/^\/api/, '')
                }
            }
        },
        build: {
            outDir: '../bundle/'
        },
        base: baseUrl,
        plugins: [react(), visualizer({
            open: true, // 自动打开报告页面
            filename: 'stats.html', // 报告文件路径
            gzipSize: true, // 显示 gzip 压缩后的大小
            brotliSize: true // 显示 brotli 压缩后的大小
        })],
    }
})
