package easy4j.module.mybatisplus.codegen.servlet;


import easy4j.infra.base.properties.CodeGenProperties;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Config {


    @Bean(name = "e4jCgServletServletRegistrationBean")
    public ServletRegistrationBean<E4jCgServlet> e4jCgServletServletRegistrationBean(){
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        CodeGenProperties codeGen = ejSysProperties.getCodeGen();
        ServletRegistrationBean<E4jCgServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setUrlMappings(ListTs.asList("/e4j/cg/*"));
        servletRegistrationBean.setServlet(new E4jCgServlet());
        servletRegistrationBean.addInitParameter("enableCrossOrigin",codeGen.isEnableCrossOrigin()+"");
        servletRegistrationBean.addInitParameter("enableBasicAuth",codeGen.isEnableBasicAuth()+"");
        servletRegistrationBean.addInitParameter("username",codeGen.getUsername());
        servletRegistrationBean.addInitParameter("password",codeGen.getPassword());
        return servletRegistrationBean;
    }
}
