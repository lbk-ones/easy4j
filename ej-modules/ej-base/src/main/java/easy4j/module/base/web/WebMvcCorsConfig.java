package easy4j.module.base.web;


import easy4j.module.base.module.Module;
import easy4j.module.base.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static easy4j.module.base.utils.SysConstant.GLOBAL_CORS_ENABLE;

@Slf4j
@Configuration
@Module(GLOBAL_CORS_ENABLE+":true")
public class WebMvcCorsConfig implements WebMvcConfigurer, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(SysLog.compact("SpringMVC允许跨域已开启"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 匹配所有接口
                .allowedOrigins("*")  // 允许所有来源（生产环境建议指定具体域名）
                .allowedMethods("GET, POST, PUT, DELETE, OPTIONS")  // 允许所有请求方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true) // 允许携带 Cookie（需与前端配合，域名需一致）
                .maxAge(3600); // 预检请求的有效期（秒）
    }
}