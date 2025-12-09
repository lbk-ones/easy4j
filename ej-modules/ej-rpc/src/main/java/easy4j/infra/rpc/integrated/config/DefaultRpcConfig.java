package easy4j.infra.rpc.integrated.config;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.config.E4jRpcConfig;

import java.io.InputStream;
import java.util.Properties;

public class DefaultRpcConfig extends AbstractRpcConfig {
    public static final Properties fileProperties = new Properties();

    public static DefaultRpcConfig INSTANCE = new DefaultRpcConfig();

    E4jRpcConfig e4jRpcConfig = new E4jRpcConfig();

    static {
        try {
            InputStream resourceAsStream = DefaultRpcConfig.class.getClassLoader().getResourceAsStream("easy4j.properties");
            fileProperties.load(resourceAsStream);
        } catch (Exception ignored) {

        }
    }

    @Override
    public String defaultGet(String key) {
        if (null == key) return null;
        String value = ObjectUtil.defaultIfBlank(System.getenv(key), System.getProperty(key));
        if (StrUtil.isBlank(value)) {
            value = fileProperties.getProperty(key);
        }
        return value;
    }

    @Override
    public E4jRpcConfig getConfig() {
        return e4jRpcConfig;
    }

}
