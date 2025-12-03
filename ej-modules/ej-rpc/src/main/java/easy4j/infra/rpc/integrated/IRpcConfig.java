package easy4j.infra.rpc.integrated;

import easy4j.infra.rpc.config.E4jRpcConfig;
import easy4j.infra.rpc.integrated.config.RpcConfigCallbackFunction;

import java.util.List;
import java.util.Map;

public interface IRpcConfig {
    /**
     * 值变更 然后手动触发这个方法
     */
    void change(Map<String, String> newMap);

    /**
     * 批量值获取
     * @param keys
     */
    Map<String,String> get(List<String> keys);

    /**
     * 单个值获取
     * @param keys
     * @return 返回字符串
     */
    String get(String keys);

    /**
     * 获取客户端配置
     * @return
     */
    E4jRpcConfig getConfig();

    /**
     * 单个值订阅
     * @param key
     * @param configCallbackFunction
     * @return 返回字符串
     */
    String subscribe(String key, RpcConfigCallbackFunction configCallbackFunction);

    /**
     * 批量值监听
     * @param key
     * @param configCallbackFunction
     * @return 返回一个map
     */
    Map<String,String> subscribe(List<String> key, RpcConfigCallbackFunction configCallbackFunction);


}
