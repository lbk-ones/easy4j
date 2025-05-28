package easy4j.module.sauth.filter;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class FilterConfig implements WebMvcConfigurer {

    Easy4jSecurityFilterInterceptor easy4jSecurityFilterInterceptor;

    public FilterConfig(Easy4jSecurityFilterInterceptor easy4jSecurityFilterInterceptor) {
        this.easy4jSecurityFilterInterceptor = easy4jSecurityFilterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(easy4jSecurityFilterInterceptor)
                .order(Integer.MIN_VALUE + 1)
                .pathMatcher(new AntPathMatcher("/**"));
    }
}
