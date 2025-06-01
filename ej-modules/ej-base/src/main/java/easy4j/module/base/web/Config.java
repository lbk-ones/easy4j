package easy4j.module.base.web;

import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.web.filter.RequestWrapperFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

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
}
