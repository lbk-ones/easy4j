package io.github.lbkones.config.api;

import java.util.Map;
import java.util.Properties;

/**
 * 各个子模块的配置中心实现
 */
public interface CcSpi {

    /**
     * 放入引导参数
     * @param bootParameters 引导参数 不能为空
     */
    void setBootParameters(Map<String, String> bootParameters);


    /**
     * 获取配置中心的真实配置
     */
    Map<String,Properties> getConfig();


    /**
     * 开启配置中心
     */
    void start();


    /**
     * 销毁配置中心
     */
    void destroy();

    /**
     * 配置中心实现名称
     */
    String getName();

    /**
     * 变动返回
     * @return
     */
    void subscribe(ConfigChange configChange);

}
