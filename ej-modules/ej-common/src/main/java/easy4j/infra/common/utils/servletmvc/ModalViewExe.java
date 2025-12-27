package easy4j.infra.common.utils.servletmvc;

import cn.hutool.core.util.StrUtil;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jodd.util.StringPool;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class ModalViewExe {


    /**
     * 模板引擎执行
     *
     * @param modalView
     * @param response
     * @param classPath
     * @throws IOException
     */
    public static void exe(ModalView modalView, HttpServletResponse response, String classPath) throws IOException {
        ViewEngine viewEngine = modalView.getViewEngine();
        if (classPath.startsWith("/")) {
            classPath = StrUtil.replaceFirst(classPath, "/", "");
        }
        if (classPath.endsWith("/")) {
            classPath = StrUtil.replaceLast(classPath, "/", "");
        }
        String view = modalView.getView();
        int i = view.lastIndexOf(".");
        if (i > 0) view = view.substring(0, i);
        Map<String, Object> params = modalView.getModal();
        if (viewEngine == ViewEngine.THYMELEAF) {
            // 1. 创建模板解析器（类路径加载，最常用）
            ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
            resolver.setPrefix(classPath + "/"); // 模板文件在类路径的 templates/ 目录下
            resolver.setSuffix(".html");     // 模板文件后缀
            resolver.setTemplateMode("HTML");// 模板模式（HTML/TEXT/XML 等）
            resolver.setCharacterEncoding("UTF-8"); // 编码（必须，避免中文乱码）
            resolver.setCacheable(true);    // 开发环境关闭缓存（生产环境设为 true）
            // 2. 创建模板引擎，绑定解析器
            TemplateEngine templateEngine = new TemplateEngine();
            templateEngine.setTemplateResolver(resolver);
            // 3. 创建上下文（传递变量）
            Context context = new Context();

            context.setVariables(params);

            // 4. 渲染模板（参数1：模板文件名，参数2：上下文）
            String renderedHtml = templateEngine.process(view, context);
            response.setContentType(modalView.getMimeType().getFullMimeTypeWithUtf8());
            PrintWriter writer = response.getWriter();
            writer.write(renderedHtml, 0, renderedHtml.length());
            writer.flush();
        } else if (viewEngine == ViewEngine.FREEMARKER) {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setTemplateLoader(new ClassTemplateLoader(ModalViewExe.class, StringPool.SLASH + classPath));
            try (Writer writer = new StringWriter()) {
                Template template = cfg.getTemplate(view + ".ftl");
                template.process(params, writer);
                String renderedHtml = writer.toString();
                response.setContentType(modalView.getMimeType().getFullMimeTypeWithUtf8());
                PrintWriter writer2 = response.getWriter();
                writer2.write(renderedHtml, 0, renderedHtml.length());
                writer2.flush();
            } catch (TemplateException e) {
                throw new RuntimeException("模板渲染失败：" + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("模板加载/IO 异常：" + e.getMessage(), e);
            }
        }
    }


}
