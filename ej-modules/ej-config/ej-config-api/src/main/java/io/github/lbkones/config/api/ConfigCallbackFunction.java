package io.github.lbkones.config.api;

@FunctionalInterface
public interface ConfigCallbackFunction {

    /**
     *
     * @param key key的名称
     * @param res 返回结果
     * @param type 返回类型 1是初始化 2是变化
     */
    void callback(String key,String res,String type);
}
