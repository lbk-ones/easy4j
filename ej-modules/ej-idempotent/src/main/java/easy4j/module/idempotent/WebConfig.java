package easy4j.module.idempotent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfig implements WebMvcConfigurer {

    @Autowired
    @Qualifier("idempotentHandlerInterceptor")
    IdempotentHandlerInterceptor idempotentHandlerInterceptor;


    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(idempotentHandlerInterceptor)
                .addPathPatterns("/**");
    }

}
