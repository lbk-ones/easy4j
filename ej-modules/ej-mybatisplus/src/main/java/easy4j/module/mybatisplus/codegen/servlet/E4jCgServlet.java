package easy4j.module.mybatisplus.codegen.servlet;


import easy4j.infra.common.utils.servlet.ResourceServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class E4jCgServlet extends ResourceServlet {


    public E4jCgServlet() {
        super("bundle");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        selectAndInvokeHandler(request,response, E4jCgController.class);
    }

}