package easy4j.module.mybatisplus.codegen.servlet;


import easy4j.infra.base.properties.CodeGenProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.servletmvc.Easy4jServlet;
import easy4j.infra.common.utils.servletmvc.MimeType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 代码生成servlet
 *
 * @author bokun
 * @since 2.0.1
 */
public class E4jCgServlet extends Easy4jServlet {


    public E4jCgServlet() {
        super("bundle");
        // handler
        super.setObtainClasses(new Class[]{E4jCgController.class});
        super.setRedirectHome("index.html");
    }

    @Override
    public boolean intercept(HttpServletRequest request, HttpServletResponse response) {
        CodeGenProperties codeGen = Easy4j.getEjSysProperties().getCodeGen();
        if (codeGen != null && !codeGen.isEnable()) {
            try {
                PrintWriter writer = response.getWriter();
                response.setContentType(MimeType.TEXT_PLAIN.getFullMimeTypeWithUtf8());
                writer.write("This resource has been disabled！");
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        } else {
            return super.intercept(request, response);
        }
    }
}