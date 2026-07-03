package io.github.lbkones.sentinel;

import easy4j.infra.base.properties.NacosPropetiesParse;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class AlibabaSentinelEnvironment extends AbstractEasy4jEnvironment {

    @Override
    public String getName() {
        return "sentinel.properties";
    }

    /**
     * 默认 开启Sentinel Web 拦截器
     * 不设置 feign.sentinel.enabled=true 会重复保护 没必要
     *
     * @return
     */
    @Override
    public Properties getProperties() {

        System.out.println(SysLog.compact("begin handler alibaba sentinel environment"));
        Properties properties = new Properties();
        ClassLoader classLoader = this.getClass().getClassLoader();
        // 与feign相关的配置
        // feign相关的降级全部需要搭配 @FeignClient中的 fallback 字段或者 fallbackFactory字段使用
        try {
            classLoader.loadClass("io.github.lbkones.cloud.openfeign.OpenFeignEnvironment");
            // 这个配置只管openFeign调用降级
            properties.setProperty("spring.cloud.openfeign.circuitbreaker.enabled", "true");

            // 硬编码的流控开关（硬编码就是url写死了）
            // 资源名称默认是 HttpMethod:protocol://url
            // 比如 POST:http://127.0.0.1/api/v1/auth POST必须大写
            // 看似和spring.cloud.openfeign.circuitbreaker.enabled的功能重复了，其实它只管硬编码
            properties.setProperty("feign.sentinel.enabled","true");

            // 流控规则全部放到datasource也就是配置中心去解析
        } catch (ClassNotFoundException ignored) {

        }


        try {
            // 如果引入了 sentinel-datasource-nacos 则自动配置nacos地址
            classLoader.loadClass("com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource");
            System.out.println(SysLog.compact("begin load nacos config...."));
            NacosPropetiesParse build = NacosPropetiesParse.build(this.getConfigEnvironment(), true);
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.server-addr", build.getNacosConfigUrl());
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.dataId", "sentinel-flow-rules");
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.groupId", build.getNacosConfigGroup());
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.namespace", build.getNacosConfigNameSpace());
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.username", build.getNacosConfigUsername());
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.password", build.getNacosConfigPassword());
            properties.setProperty("spring.cloud.sentinel.datasource.flow-rule.nacos.rule-type", "flow");

            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.server-addr", build.getNacosConfigUrl());
            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.dataId", "sentinel-degrade-rules");
            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.groupId", build.getNacosConfigGroup());
            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.namespace", build.getNacosConfigNameSpace());
            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.username", build.getNacosConfigUsername());
            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.password", build.getNacosConfigPassword());
            properties.setProperty("spring.cloud.sentinel.datasource.degrade-rule.nacos.rule-type", "degrade");
        } catch (Exception ex) {
            System.err.println(SysLog.compact("load sentinel nacos config error...." + ex.getMessage()));
        }

        String logPath = this.getLogPath() + SP.SLASH + "csp";
        System.out.println(SysLog.compact("alibaba sentinel log path is " + logPath));

        properties.setProperty("spring.cloud.sentinel.log.dir", logPath);
        // 区分HTTP大小写
        properties.setProperty("spring.cloud.sentinel.http-method-specify", "true");


        // 使用dashboard
        Boolean enabled = Easy4j.getProperty(SysConstant.EASY4J_SENTINEL_DASHBOARD_ENABLE, Boolean.class, false);
        System.out.println(SysLog.compact("alibaba sentinel dashboard enabled is  " + enabled));
        String url = Easy4j.getProperty(SysConstant.EASY4J_SENTINEL_DASHBOARD_CONSOLE_URL, String.class, "");
        Integer runtimePort = Easy4j.getProperty(SysConstant.EASY4J_SENTINEL_DASHBOARD_RUNTIME_PORT, Integer.class, 8719);
        if (enabled == true) {
            properties.setProperty("spring.cloud.sentinel.transport.port", String.valueOf(runtimePort));
            properties.setProperty("spring.cloud.sentinel.transport.dashboard", url);
        }

        return properties;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println(SysLog.compact("enable cloud sentinel module"));
    }
}
