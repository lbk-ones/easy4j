package easy4j.infra.webmvc;

import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.webmvc.filter.RequestWrapperFilter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.LocaleResolver;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(value = {WebMvcAutoConfiguration.class})
public class Config {

    @Bean
    FilterRegistrationBean<RequestWrapperFilter> requestWrapperFilterFilterRegistrationBean() {
        RequestWrapperFilter requestWrapperFilter = new RequestWrapperFilter();
        FilterRegistrationBean<RequestWrapperFilter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(requestWrapperFilter);
        filterFilterRegistrationBean.addUrlPatterns("/*");

        // 设置初始化参数
        Map<String, String> initParams = new HashMap<>();

        String property = Easy4j.getProperty(SysConstant.EASY4J_CACHE_CONTENT_LENGTH);

        initParams.put(SysConstant.EASY4J_CACHE_CONTENT_LENGTH, property);
        filterFilterRegistrationBean.setInitParameters(initParams);
        return filterFilterRegistrationBean;
    }


    /**
     * 开启跨域
     *
     * @return
     * @parameters
     * @method Config#webMvcCorsConfig
     * @author bokun.li
     * @date 2025-06-08 17:56:37
     */
    @Bean
    public WebMvcCorsConfig webMvcCorsConfig() {
        return new WebMvcCorsConfig();
    }

    /**
     * 移除请求结果受xml的影响
     *
     * @return
     * @parameters
     * @method Config#webMvcConvertConfig
     * @author bokun.li
     * @date 2025-06-08 17:56:14
     */
    @Bean
    public WebMvcConvertConfig webMvcConvertConfig() {
        return new WebMvcConvertConfig();
    }

    /**
     * mvc 拦截器
     *
     * @author bokun.li
     * @date 2025-06-08 17:54:55
     */
    @Bean
    public PerRequestInterceptor perRequestInterceptor() {
        return new PerRequestInterceptor();
    }

    /**
     * mvc 拦截器  配置
     *
     * @author bokun.li
     * @date 2025-06-08 17:54:55
     */
    @Bean
    public WebMvcFilterConfig webMvcFilterConfig() {
        return new WebMvcFilterConfig();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * 默认解析器 其中locale表示默认语言
     */
    @Bean("localeResolver")
    public LocaleResolver localeResolver() {
        return new EasyLocaleResolver();
    }


}
