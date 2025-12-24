/**
 * 全局 Loading 封装
 * 功能：支持显示/隐藏、自定义加载文本、防重复创建、自动移除DOM
 */
class GlobalLoading {
    // 单例模式，避免重复创建
    static instance = null;
    // Loading 容器元素
    #loadingEl = null;
    // 加载文本默认值
    #defaultText = '加载中...';

    constructor() {
        if (GlobalLoading.instance) return GlobalLoading.instance;
        GlobalLoading.instance = this;
        this.#createStyle(); // 初始化样式
    }

    /**
     * 1. 创建 Loading 样式（动态插入到 head）
     */
    #createStyle() {
        const style = document.createElement('style');
        style.id = 'global-loading-style';
        style.textContent = `
      .global-loading-mask {
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background: rgba(0, 0, 0, 0.5); /* 半透明遮罩 */
        z-index: 9999; /* 最高层级，避免被遮挡 */
        display: flex;
        justify-content: center;
        align-items: center;
        backdrop-filter: blur(2px); /* 可选：背景模糊 */
      }
      .global-loading-content {
        background: #fff;
        padding: 24px 32px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        gap: 12px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
      .global-loading-spinner {
        width: 20px;
        height: 20px;
        border: 3px solid #eee;
        border-top-color: #409eff; /* 加载动画颜色 */
        border-radius: 50%;
        animation: loading-spin 1s linear infinite;
      }
      .global-loading-text {
        font-size: 14px;
        color: #333;
      }
      @keyframes loading-spin {
        to { transform: rotate(360deg); }
      }
    `;
        // 避免重复插入样式
        if (!document.getElementById('global-loading-style')) {
            document.head.appendChild(style);
        }
    }

    /**
     * 2. 创建 Loading DOM 结构
     * @param {string} text - 自定义加载文本
     */
    #createLoadingDom(text = this.#defaultText) {
        // 若已存在，直接更新文本并显示
        if (this.#loadingEl) {
            this.#loadingEl.querySelector('.global-loading-text').textContent = text;
            this.#loadingEl.style.display = 'flex';
            return;
        }

        // 创建遮罩层
        const mask = document.createElement('div');
        mask.className = 'global-loading-mask';

        // 创建加载内容容器
        const content = document.createElement('div');
        content.className = 'global-loading-content';

        // 创建加载动画
        const spinner = document.createElement('div');
        spinner.className = 'global-loading-spinner';

        // 创建加载文本
        const textEl = document.createElement('div');
        textEl.className = 'global-loading-text';
        textEl.textContent = text;

        // 组装 DOM
        content.appendChild(spinner);
        content.appendChild(textEl);
        mask.appendChild(content);
        document.body.appendChild(mask);

        // 保存引用
        this.#loadingEl = mask;
    }

    /**
     * 3. 显示全局 Loading
     * @param {string} [text] - 自定义加载文本
     */
    show(text) {
        // 确保 DOM 加载完成后执行
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.#createLoadingDom(text));
        } else {
            this.#createLoadingDom(text);
        }
    }

    /**
     * 4. 隐藏全局 Loading
     */
    hide() {
        if (!this.#loadingEl) return;
        // 先隐藏，再移除（避免动画卡顿）
        this.#loadingEl.style.display = 'none';
        // 可选：延迟移除 DOM，防止快速切换时闪烁
        setTimeout(() => {
            if (this.#loadingEl && this.#loadingEl.parentNode) {
                this.#loadingEl.parentNode.removeChild(this.#loadingEl);
            }
            this.#loadingEl = null; // 清空引用
        }, 300);
    }
}

export default GlobalLoading;
