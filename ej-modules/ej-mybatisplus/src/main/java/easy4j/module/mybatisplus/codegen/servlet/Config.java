package easy4j.module.mybatisplus.codegen.servlet;


import easy4j.infra.common.utils.ListTs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class Config implements InitializingBean {


    @Bean(name = "e4jCgServletServletRegistrationBean")
    public ServletRegistrationBean<E4jCgServlet> e4jCgServletServletRegistrationBean(){
        ServletRegistrationBean<E4jCgServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setUrlMappings(ListTs.asList("/e4j/cg/*"));
        servletRegistrationBean.setServlet(new E4jCgServlet());
        return servletRegistrationBean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
