import React, {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import 'antd/dist/reset.css'
import './index.css'
import App from './App.jsx'
import GlobalLoading from "./loading.js";
import {ConfigProvider} from "antd";
import {ColorProvider, useColor} from "./color.jsx";
// 初始化单例（全局可访问）
window.$loading = new GlobalLoading();

function Child(props){
    let {primaryColor} = useColor();
    return <ConfigProvider
        theme={{
            token: {
                // Seed Token，影响范围大
                colorPrimary: primaryColor,
                borderRadius: 0,
                // colorBorder: '#722ed1',
                // colorPrimaryBg: '#722ed1',
                // colorInfoBg: '#722ed1',
                // 派生变量，影响范围小
                colorBgContainer: '#F2F3F5',
                colorBgContainerDisabled: '#E4E5E7'
            },
        }}
    >
        <StrictMode>
            <App/>
        </StrictMode>
    </ConfigProvider>
}

function Main(props){
    return <ColorProvider>
        <Child/>
    </ColorProvider>
}


createRoot(document.getElementById('root')).render(
    <Main/>
    ,
)
