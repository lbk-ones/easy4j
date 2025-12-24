package easy4j.infra.common.utils.servlet;

import cn.hutool.core.exceptions.InvocationTargetRuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.json.JacksonUtil;
import jodd.exception.ExceptionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 从 ClassPath 加载静态资源的 Servlet
 * 映射路径：/resources/*
 * 示例：请求 /resources/css/style.css → 读取 classpath:css/style.css
 *
 * @author bokun
 * @since 2.0.1
 */
public class ResourceServlet extends HttpServlet {

    public String classPath;

    public ResourceServlet(String classPath) {
        this.classPath = classPath;
    }

    // 1. 后缀 -> Content-Type 映射（扩展可自行添加）
    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();

    static {
        // 文本类
        CONTENT_TYPE_MAP.put(".html", "text/html;charset=UTF-8");
        CONTENT_TYPE_MAP.put(".css", "text/css;charset=UTF-8");
        CONTENT_TYPE_MAP.put(".js", "application/javascript;charset=UTF-8");
        CONTENT_TYPE_MAP.put(".json", "application/json;charset=UTF-8");
        CONTENT_TYPE_MAP.put(".xml", "text/xml;charset=UTF-8");
        CONTENT_TYPE_MAP.put(".txt", "text/plain;charset=UTF-8");

        // 图片类
        CONTENT_TYPE_MAP.put(".png", "image/png");
        CONTENT_TYPE_MAP.put(".jpg", "image/jpeg");
        CONTENT_TYPE_MAP.put(".jpeg", "image/jpeg");
        CONTENT_TYPE_MAP.put(".gif", "image/gif");
        CONTENT_TYPE_MAP.put(".svg", "image/svg+xml");
        CONTENT_TYPE_MAP.put(".ico", "image/x-icon");

        // 字体类
        CONTENT_TYPE_MAP.put(".woff", "font/woff");
        CONTENT_TYPE_MAP.put(".woff2", "font/woff2");
        CONTENT_TYPE_MAP.put(".ttf", "font/ttf");
        CONTENT_TYPE_MAP.put(".eot", "application/vnd.ms-fontobject");

        // 其他
        CONTENT_TYPE_MAP.put(".zip", "application/zip");
        CONTENT_TYPE_MAP.put(".pdf", "application/pdf");
    }

    // 缓存时间（秒）：可选，静态资源建议添加缓存提升性能
    private static final int CACHE_SECONDS = 86400 * 7; // 7天

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //  解析请求路径，提取 ClassPath 中的资源路径
        String resourcePath = getClassPathResourcePath(request);
        if (resourcePath == null || resourcePath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "资源路径为空");
            return;
        }

        // 从 ClassPath 加载资源（核心：ClassLoader 读取）
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream resourceStream = classLoader.getResourceAsStream((StrUtil.isBlank(classPath) ? "" : classPath + "/") + resourcePath)) {
            //  处理资源不存在的情况
            if (resourceStream == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "ClassPath 中未找到资源：" + resourcePath);
                return;
            }

            // 设置响应头（Content-Type + 缓存 + 编码）
            setResponseHeaders(response, resourcePath);

            //  输出资源到响应流（字节流适配所有类型：文本/二进制）
            try (BufferedInputStream bis = new BufferedInputStream(resourceStream);
                 BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {

                byte[] buffer = new byte[4096]; // 4KB 缓冲，提升读写效率
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                bos.flush(); // 确保所有数据输出
            }

        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "读取资源失败：" + e.getMessage());
        }
    }

    // 得到请求路径 比如 /css/style.css  /api/test.json
    public String getPath(HttpServletRequest request) {
        String contextPath = request.getContextPath(); // 应用上下文（如 /myapp）
        String requestURI = request.getRequestURI();   // 完整请求URI（如 /myapp/resources/css/style.css）
        String servletPath = request.getServletPath(); // Servlet匹配路径（/resources）

        // 剥离 上下文路径 + Servlet路径，得到资源相对路径
        int startIndex = (contextPath == null ? 0 : contextPath.length()) + servletPath.length();
        if (startIndex >= requestURI.length()) {
            return null; // 无资源路径（如仅请求 /resources）
        }
        // 提取资源路径
        return requestURI.substring(startIndex);
    }

    /**
     * 解析请求路径，提取 ClassPath 中的资源路径
     * 示例：请求 /resources/css/style.css → css/style.css
     */
    private String getClassPathResourcePath(HttpServletRequest request) {
        String resourcePath = getPath(request);
        // 提取资源路径（如 /css/style.css → 去掉开头的/ → css/style.css，适配ClassPath读取规则）
        if (resourcePath != null && resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        return resourcePath;
    }

    /**
     * 设置响应头：Content-Type + 缓存 + 字符编码
     */
    private void setResponseHeaders(HttpServletResponse response, String resourcePath) {
        // 1. 获取文件后缀，匹配 Content-Type
        String fileExt = getFileExtension(resourcePath);
        String contentType = CONTENT_TYPE_MAP.getOrDefault(fileExt, "application/octet-stream");
        response.setContentType(contentType);

        // 2. 设置缓存（可选，静态资源建议开启）
        response.setHeader("Cache-Control", "public, max-age=" + CACHE_SECONDS);
        response.setDateHeader("Expires", System.currentTimeMillis() + CACHE_SECONDS * 1000L);

        // 3. 禁用缓存（如需实时更新资源，注释上面两行，打开下面一行）
        // response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        // 4. 防止中文文件名乱码（可选）
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + resourcePath);
    }

    /**
     * 提取文件后缀（小写）
     * 示例：style.css → .css；logo.png → .png
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ""; // 无后缀
        }
        return fileName.substring(lastDotIndex).toLowerCase();
    }


    public Map<Class<?>, Object> CACHE_OBJECT = Maps.newConcurrentMap();
    public Map<String, Boolean> CLASS_MAP_BOOL = Maps.newConcurrentMap();

    /**
     * 调用 handler
     * 先匹配handler再匹配静态资源
     *
     * @param request  req
     * @param response res
     * @param aClazz_  clazz
     */
    public void selectAndInvokeHandler(HttpServletRequest request, HttpServletResponse response, Class<?>... aClazz_) throws ServletException, IOException {

        String path = getPath(request);
        path = path == null ? "/" : path;
        String reqMethod = request.getMethod();
        List<Object> args = ListTs.newList();
        Object targetObject = null;
        Method method_ = null;
        UrlMap annotation = null;
        lbk:
        for (Class<?> aClazz : aClazz_) {
            Boolean b = CLASS_MAP_BOOL.get(reqMethod + aClazz.getName() + path);
            if (b != null && !b) {
                continue;
            }
            Method[] methods = ReflectUtil.getMethods(aClazz);
            targetObject = CACHE_OBJECT.get(aClazz);
            if (targetObject == null) {
                CACHE_OBJECT.put(aClazz, ReflectUtil.newInstance(aClazz));
                targetObject = CACHE_OBJECT.get(aClazz);
            }
            for (Method method : methods) {
                if (
                        !method.isBridge() &&
                                method.isAnnotationPresent(UrlMap.class) &&
                                Modifier.isPublic(method.getModifiers()) &&
                                !method.isDefault() &&
                                !Modifier.isStatic(method.getModifiers())
                ) {

                    annotation = method.getAnnotation(UrlMap.class);
                    MethodType method2 = annotation.method();
                    String url = annotation.url();
                    String name = method2.name();
                    if (StrUtil.equalsIgnoreCase(reqMethod, name) && StrUtil.equals(url, path)) {
                        method_ = method;
                        Enumeration<String> parameterNames = request.getParameterNames();
                        Map<String, String> params = Maps.newHashMap();
                        while (parameterNames.hasMoreElements()) {
                            String s = parameterNames.nextElement();
                            String parameter = request.getParameter(s);
                            params.put(s, parameter);
                        }
                        ServletHandler servletHandler = new ServletHandler();
                        servletHandler.setFormDataMap(params);
                        servletHandler.setRequest(request);
                        servletHandler.setResponse(response);
                        servletHandler.setMethod(method);
                        servletHandler.setUrl(path);
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (ListTs.isNotEmpty(parameterTypes)) {
                            for (Class<?> parameterType : parameterTypes) {
                                if (HttpServletRequest.class.isAssignableFrom(parameterType)) {
                                    args.add(request);
                                } else if (HttpServletResponse.class.isAssignableFrom(parameterType)) {
                                    args.add(response);
                                } else if (ServletHandler.class.isAssignableFrom(parameterType)) {
                                    args.add(servletHandler);
                                } else {
                                    args.add(null);
                                }
                            }
                        }
                        break lbk;
                    }
                }
            }
            CLASS_MAP_BOOL.put(reqMethod + aClazz.getName() + path, false);
        }
        if (targetObject != null && method_ != null) {
            try {
                Object invokeRes = null;
                if (args.isEmpty()) {
                    invokeRes = ReflectUtil.invoke(targetObject, method_);
                } else {
                    invokeRes = ReflectUtil.invoke(targetObject, method_, args.toArray(new Object[]{}));
                }
                if (invokeRes != null) {
                    if (!Objects.equals(invokeRes.getClass().getName(), Object.class.getName())) {
                        response.setContentType(annotation.returnContentType());
                        PrintWriter writer = response.getWriter();
                        if (invokeRes instanceof CharSequence) {
                            String resString = invokeRes.toString();
                            writer.write(resString, 0, resString.length());
                        } else {
                            String resString = JacksonUtil.toJson(invokeRes);
                            writer.write(resString, 0, resString.length());
                        }
                        writer.flush();
                    }
                }
            } catch (InvocationTargetRuntimeException e) {
                response.setContentType(annotation.returnContentType());
                Throwable cause = e;
                while (cause.getCause()!=null){
                    cause = cause.getCause();
                }
                String message = ExceptionUtil.exceptionChainToString(cause);
                response(response,JacksonUtil.toJson(SRes.error(message)));
            }

        } else {
            super.service(request, response);
        }

    }

    public void response(HttpServletResponse response,String data){
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer.write(data, 0, data.length());
        writer.flush();
    }
}