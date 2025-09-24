package easy4j.infra.base.properties.cc;

import java.util.List;
import java.util.Map;
/**
 * 配置中心值获取，值订阅
 * @author bokun.li
 * @date 2025/9/24
 */
public interface ConfigCenter {

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
     * 单个值订阅
     * @param key
     * @param configCallbackFunction
     * @return 返回字符串
     */
    String subscribe(String key, ConfigCallbackFunction configCallbackFunction);

    /**
     * 批量值监听
     * @param key
     * @param configCallbackFunction
     * @return 返回一个map
     */
    Map<String,String> subscribe(List<String> key, ConfigCallbackFunction configCallbackFunction);



}
