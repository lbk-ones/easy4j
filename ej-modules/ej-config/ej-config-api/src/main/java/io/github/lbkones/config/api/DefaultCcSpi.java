package io.github.lbkones.config.api;

import easy4j.infra.common.utils.SysLog;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultCcSpi extends AbstractCcSpi{

    public static final String DEFAULT_SPI = "default file system";


    @Override
    public Map<String,Properties> getConfig() {
        return new HashMap<>();
    }

    @Override
    public void start() {
        System.out.println(SysLog.compact("config center do nothing"));
    }

    @Override
    public void destroy() {

    }

    @Override
    public String getName() {
        return DEFAULT_SPI;
    }

    @Override
    public void subscribe(ConfigChange configChange) {

    }

}
