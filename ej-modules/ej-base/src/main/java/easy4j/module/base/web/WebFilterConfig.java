package easy4j.module.base.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebFilterConfig
 *
 * @author bokun.li
 * @date 2025-05
 */
public class WebFilterConfig implements WebMvcConfigurer {

    @Autowired
    PerRequestInterceptor perRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(perRequestInterceptor)
                .order(Integer.MIN_VALUE)
                .pathMatcher(new AntPathMatcher("/**"));
    }
}