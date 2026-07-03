package io.github.lbkones.cloud.openfeign;

import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.common.utils.SysLog;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class OpenFeignEnvironment extends AbstractEasy4jEnvironment {

    @Override
    public String getName() {
        return "feign.yml";
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println(SysLog.compact("enable cloud openfeign module"));
    }
}
