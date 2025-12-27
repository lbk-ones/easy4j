package easy4j.infra.common.utils.servletmvc;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.exceptions.InvocationTargetRuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.TypeUtils;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.json.JacksonUtil;
import jodd.exception.ExceptionUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ClassUtils;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 从 ClassPath 加载静态资源的 Servlet
 * 映射路径：/resources/*
 * 示例：请求 /resources/css/style.css → 读取 classpath:css/style.css
 * 鉴权，静态资源返回，模板引擎，mvc处理器（springmvc的简化版）
 *
 * @author bokun
 * @since 2.0.1
 */
public class Easy4jServlet extends HttpServlet {

    public Easy4jServlet(String classPath) {
        this.classPath = classPath;
    }

    public Map<Class<?>, Object> CACHE_OBJECT = Maps.newConcurrentMap();
    public Map<String, Boolean> CLASS_MAP_BOOL = Maps.newConcurrentMap();
    public String classPath;
    // 缓存时间（秒）：可选，静态资源建议添加缓存提升性能
    private static final int CACHE_SECONDS = 86400 * 7; // 7天
    private boolean enableCrossOrigin;
    // 拦截所有静态资源和接口
    private boolean enableBasicAuth;
    private String username;
    private String password;
    private String redirectHome;
    private Class<?>[] obtainClasses;
    private ViewEngine viewEngine;
    // 保护域（弹窗提示用）
    private static final String REALM = "Easy-CodeGen";
    private static final String BASIC_AUTH_HEADER = "Authorization";
    private static final String BASIC = "Basic ";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        enableCrossOrigin = "true".equalsIgnoreCase(config.getInitParameter("enableCrossOrigin"));
        enableBasicAuth = "true".equalsIgnoreCase(config.getInitParameter("enableBasicAuth"));
        username = StrUtil.blankToDefault(config.getInitParameter("username"), "easy4j");
        password = StrUtil.blankToDefault(config.getInitParameter("password"), "easy@123456");
        viewEngine = ViewEngine.of(StrUtil.blankToDefault(config.getInitParameter("viewEngine"), "THYMELEAF"));
    }

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

    public void setRedirectHome(String redirectHome) {
        this.redirectHome = redirectHome;
    }

    public void setObtainClasses(Class<?>[] obtainClasses) {
        this.obtainClasses = obtainClasses;
    }

    /**
     * 获取handler
     *
     * @return Class<?>[]
     */
    private Class<?>[] obtainHandler() {
        return obtainClasses;
    }

    /**
     * 重定向到主页
     *
     * @return String
     */
    private String redirectHome() {
        return redirectHome;
    }


    /**
     * 拦截器
     *
     * @param request  request
     * @param response response
     * @return boolean
     */
    public boolean intercept(HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    public void crossOrigin(HttpServletRequest request, HttpServletResponse response) {
        if (enableCrossOrigin) {
            // 核心跨域配置
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Headers", "*");
            // 允许Cookie（需指定具体域名）
            //response.setHeader("Access-Control-Allow-Credentials", "true");
            // resp.setHeader("Access-Control-Allow-Origin", "https://www.example.com");
        }
    }

    // 封装401响应（触发认证弹窗）
    private void sendUnauthorizedResponse(HttpServletResponse resp) {
        try {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType(MimeType.TEXT_HTML.getFullMimeTypeWithUtf8());
            resp.setHeader("WWW-Authenticate", "Basic realm=\"" + REALM + "\"");
            resp.getWriter().write("<h1 style=\"color:red\">请输入正确的用户名密码!</h1>");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public boolean authentication(HttpServletRequest request, HttpServletResponse response) {
        if (enableBasicAuth) {
            String authHeader = request.getHeader(BASIC_AUTH_HEADER);
            // 校验认证头是否存在且格式正确
            if (authHeader == null || !authHeader.startsWith(BASIC)) {
                // 认证失败
                sendUnauthorizedResponse(response);
                return false;
            } else {
                // 解码Base64串，拆分用户名密码
                String base64Credentials = authHeader.substring(BASIC.length()).trim();
                String credentials = new String(
                        Base64.getDecoder().decode(base64Credentials),
                        StandardCharsets.UTF_8
                );
                // 拆分 用户名:密码
                final String[] values = credentials.split(SP.COLON, 2);
                if (values.length != 2) {
                    sendUnauthorizedResponse(response);
                    return false;
                }
                String username_ = values[0];
                String password_ = values[1];
                if (StrUtil.equals(username, username_) && StrUtil.equals(password_, password)) {
                    return true;
                } else {
                    sendUnauthorizedResponse(response);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 优先匹配 handler 然后再去匹配静态资源，静态资源只能是get请求
     *
     * @param request  the {@link HttpServletRequest} object that contains the request the client made of the servlet
     * @param response the {@link HttpServletResponse} object that contains the response the servlet returns to the client
     * @throws ServletException ServletException
     * @throws IOException      IOException
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            if (!intercept(request, response)) return;

            crossOrigin(request, response);

            if (!authentication(request, response)) return;

            Class<?>[] aClass = obtainHandler();

            if (aClass != null && aClass.length > 0) {

                selectAndInvokeHandler(request, response, aClass);

            } else {
                super.service(request, response);
            }
        } catch (Throwable e) {
            int status = response.getStatus();
            if (status != HttpServletResponse.SC_UNAUTHORIZED) {
                response(response, SRes.error(e.getMessage()).toString());
            }
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
        String fullMimeTypeWithUtf8 = MimeType.fromFileExtension(fileExt).getFullMimeTypeWithUtf8();
        response.setContentType(fullMimeTypeWithUtf8);

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

    /**
     * 不能实例化的class对象
     *
     * @param classFromType
     * @return
     */
    public boolean isSkipClass(Class<?> classFromType) {

        return classFromType.isEnum() ||
                classFromType.isArray() ||
                classFromType.isInterface() ||
                classFromType.isPrimitive() ||
                classFromType.getName().equals(Object.class.getName());

    }

    /**
     * 调用 handler
     * 先匹配handler再匹配静态资源
     *
     * @param request  req
     * @param response res
     * @param aClazz_  clazz 可以是接口类的class
     */
    public void selectAndInvokeHandler(HttpServletRequest request, HttpServletResponse response, Class<?>... aClazz_) throws ServletException, IOException {

        String path = handlerPath(getPath(request));
        String reqMethod = request.getMethod();
        List<Object> args = ListTs.newList();
        Object targetObject = null;
        Method method_ = null;
        UrlMap annotation = null;
        lbk:
        for (Class<?> aClazz : aClazz_) {
            String cacheClassKey = reqMethod + aClazz.getName() + path;
            Boolean b = CLASS_MAP_BOOL.get(cacheClassKey);
            if (b != null && !b) {
                continue;
            }
            Method[] methods = ReflectUtil.getMethods(aClazz);
            if (methods == null || methods.length == 0) {
                CLASS_MAP_BOOL.put(cacheClassKey, false);
                continue;
            }
            targetObject = CACHE_OBJECT.get(aClazz);
            if (targetObject == null) {
                Object bean = null;
                // 小小的整合一下spring ，呃 当然没有也没有关系
                try {
                    ListableBeanFactory beanFactory = SpringUtil.getBeanFactory();
                    bean = beanFactory.getBean(aClazz);
                } catch (Exception ignored) {
                }
                if (bean == null) {
                    if (isSkipClass(aClazz)) {
                        CLASS_MAP_BOOL.put(cacheClassKey, false);
                        continue;
                    }
                    CACHE_OBJECT.put(aClazz, ReflectUtil.newInstance(aClazz));
                } else {
                    CACHE_OBJECT.put(aClazz, bean);
                }
                targetObject = CACHE_OBJECT.get(aClazz);
            }

            String urlPrefix = "";
            if (aClazz.isAnnotationPresent(UrlMap.class)) {
                UrlMap annotation1 = aClazz.getAnnotation(UrlMap.class);
                urlPrefix = annotation1.url();
            }
            for (Method method : methods) {
                method = ClassUtils.getMostSpecificMethod(method, targetObject.getClass());
                if (
                        !method.isBridge() &&
                                method.isAnnotationPresent(UrlMap.class) &&
                                Modifier.isPublic(method.getModifiers()) &&
                                !method.isDefault() &&
                                !Modifier.isStatic(method.getModifiers())
                ) {
                    annotation = method.getAnnotation(UrlMap.class);
                    MethodType method2 = annotation.method();
                    String url = handlerPath(urlPrefix + SP.SLASH + annotation.url());
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
                        handlerParameters(args, servletHandler);
                        // 跳出大循环 代表找到了
                        break lbk;
                    }
                }
            }
            CLASS_MAP_BOOL.put(cacheClassKey, false);
        }
        if (targetObject != null && method_ != null) {
            invoke(response, args, targetObject, method_, annotation);
        } else {
            String redirect = redirectHome();
            if ("get".equalsIgnoreCase(reqMethod) && "/".equals(path) && StrUtil.isNotBlank(redirect)) {
                response.sendRedirect(redirect);
                return;
            }
            super.service(request, response);
        }

    }

    /**
     * 处理方法参数
     *
     * @param args           args
     * @param servletHandler shandler
     */
    private void handlerParameters(List<Object> args, ServletHandler servletHandler) {
        Method method = servletHandler.getMethod();
        HttpServletRequest request = servletHandler.getRequest();
        Map<String, String> formDataMap = servletHandler.getFormDataMap();
        HttpServletResponse response = servletHandler.getResponse();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        if (ListTs.isNotEmpty(genericParameterTypes)) {
            for (Type parameterType : genericParameterTypes) {
                Class<?> classFromType = TypeUtils.getClassFromType(parameterType);
                if (HttpServletRequest.class.isAssignableFrom(classFromType)) {
                    args.add(request);
                } else if (HttpServletResponse.class.isAssignableFrom(classFromType)) {
                    args.add(response);
                } else if (ServletHandler.class.isAssignableFrom(classFromType)) {
                    args.add(servletHandler);
                } else if (classFromType.isAnnotationPresent(URequestBody.class)) {
                    Object body = servletHandler.getBody(parameterType);
                    args.add(body);
                } else if (classFromType.isAnnotationPresent(URequestAttr.class) && !Objects.equals(classFromType.getAnnotation(URequestAttr.class).value(), "")) {
                    Object body = request.getAttribute(classFromType.getAnnotation(URequestAttr.class).value());
                    args.add(body);
                } else if (classFromType.isAnnotationPresent(USession.class) && !Objects.equals(classFromType.getAnnotation(USession.class).value(), "")) {
                    HttpSession session = request.getSession();
                    Object attribute = session.getAttribute(classFromType.getAnnotation(USession.class).value());
                    args.add(attribute);
                } else if (classFromType.isAnnotationPresent(URequestParam.class) && !Objects.equals(classFromType.getAnnotation(URequestParam.class).value(), "")) {
                    Object attribute = formDataMap.get(classFromType.getAnnotation(URequestParam.class).value());
                    args.add(attribute);
                } else {
                    if (isSkipClass(classFromType)) {
                        args.add(null);
                    } else {
                        Object o = null;
                        try {
                            o = BeanUtil.mapToBean(formDataMap, classFromType, true, CopyOptions.create().ignoreNullValue().ignoreError());
                        } catch (Exception ignored) {
                        }
                        args.add(o);
                    }
                }
            }
        }
    }

    @NotNull
    private static String handlerPath(String path) {
        path = path == null ? "/" : path;
        if (!path.startsWith(SP.SLASH)) path = SP.SLASH + path;
        path = path.replaceAll("/+", "/");
        return path;
    }

    /**
     * 反射调用 并响应结果
     *
     * @param response
     * @param args
     * @param targetObject
     * @param method_
     * @param annotation
     * @throws IOException
     */
    private void invoke(HttpServletResponse response, List<Object> args, Object targetObject, Method method_, UrlMap annotation) throws IOException {
        try {
            Object invokeRes = null;
            if (args.isEmpty()) {
                invokeRes = ReflectUtil.invoke(targetObject, method_);
            } else {
                invokeRes = ReflectUtil.invoke(targetObject, method_, args.toArray(new Object[]{}));
            }
            if (invokeRes != null) {
                if (invokeRes instanceof ModalView) {
                    ModalView mv = (ModalView) invokeRes;
                    if (mv.getViewEngine() == null) {
                        mv.setViewEngine(viewEngine);
                    }
                    ModalViewExe.exe(mv, response, this.classPath);
                } else {
                    Class<?> aClass = invokeRes.getClass();
                    if (!Objects.equals(aClass.getName(), Object.class.getName())) {
                        response.setContentType(annotation.returnContentType().getFullMimeTypeWithUtf8());
                        PrintWriter writer = response.getWriter();
                        if (invokeRes instanceof CharSequence) {
                            String resString = invokeRes.toString();
                            writer.write(resString, 0, resString.length());
                        } else if (aClass == byte[].class) {
                            byte[] aClass1 = (byte[]) invokeRes;
                            String resString = new String(aClass1, StandardCharsets.UTF_8);
                            writer.write(resString, 0, resString.length());
                        } else {
                            String resString = JacksonUtil.toJsonContainNull(invokeRes);
                            writer.write(resString, 0, resString.length());
                        }
                        writer.flush();
                    }
                }
            }
        } catch (InvocationTargetRuntimeException e) {
            response.setContentType(annotation.returnContentType().getFullMimeTypeWithUtf8());
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            String message = ExceptionUtil.exceptionChainToString(cause);
            throw new RuntimeException(message);
        }
    }

    public void response(HttpServletResponse response, String data) {
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