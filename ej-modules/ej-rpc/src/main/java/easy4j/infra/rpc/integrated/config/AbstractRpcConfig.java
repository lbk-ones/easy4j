package easy4j.infra.rpc.integrated.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.rpc.integrated.IRpcConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractRpcConfig implements IRpcConfig {

    // logger
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // callback cache
    protected final Map<String, RpcConfigCallbackFunction> cacheKeyMap = new ConcurrentHashMap<>();

    // last key map
    protected final Map<String, String> oldKeyMap = new ConcurrentHashMap<>();

    protected final Map<String, String> newKeyMap = new ConcurrentHashMap<>();

    // key vs List<String>
    protected final Map<String, List<String>> keyVsKey = new ConcurrentHashMap<>();

    // queue
    protected final LinkedBlockingQueue<Tuple> queue = new LinkedBlockingQueue<>();

    // is init
    private boolean init = false;


    public AbstractRpcConfig() {
        Class<? extends AbstractRpcConfig> aClass = this.getClass();
        synchronized (aClass){
            Thread thread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Tuple take = queue.take();
                        String key = take.get(0);
                        String value = take.get(1);
                        String operateType = take.get(2);
                        String keyName = take.get(3);
                        RpcConfigCallbackFunction configCallbackFunction = cacheKeyMap.get(key);
                        if (null != configCallbackFunction) {
                            try {
                                configCallbackFunction.callback(keyName, value, operateType);
                            } catch (Throwable e) {
                                if (logger.isErrorEnabled()) {
                                    logger.error("take thread result handler ", e);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        if (logger.isErrorEnabled()) {
                            logger.error("config-center-queue-take-thread appear exception", e);
                        }
                        Thread.currentThread().interrupt();
                    }catch (Exception e2){
                        if (logger.isErrorEnabled()) {
                            logger.error("config-center-queue-take-thread appear unknown exception", e2);
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.setName("config-center-queue-take-thread");
            thread.start();
        }
    }

    /**
     * 默认获取值的方式
     *
     * @author bokun.li
     * @date 2025/9/24
     */
    public abstract String defaultGet(String key);

    public void change(Map<String, String> newMap) {
        if (CollUtil.isEmpty(newMap)) return;
        newKeyMap.putAll(newMap);
        for (String newKey : newKeyMap.keySet()) {
            String oldValue = oldKeyMap.get(newKey);
            String newValue = newKeyMap.get(newKey);
            if (!StrUtil.equals(newValue, oldValue)) {
                List<String> strings = keyVsKey.get(newKey);
                if (CollUtil.isNotEmpty(strings)) {
                    for (String key : strings) {
                        if(key == null) continue;
                        queue.add(new Tuple(key, newValue, !this.init ? "1" : "2", newKey));
                    }
                }
            }
        }
        oldKeyMap.putAll(newMap);
        this.init = true;
    }

    public synchronized String subscribe(String key, RpcConfigCallbackFunction configCallbackFunction) {
        final String originKey = key;
        if (null != key) {
            List<String> strings = keyVsKey.get(originKey);
            if (null == strings) {
                strings = new CopyOnWriteArrayList<>();
                keyVsKey.put(originKey, strings);
            }
            key = key + StrPool.DOT + System.currentTimeMillis() + StrPool.DOT + RandomUtil.randomString(5);
            strings.add(key);
            cacheKeyMap.putIfAbsent(key, configCallbackFunction);
        }
        return get(originKey);
    }

    @Override
    public synchronized Map<String, String> subscribe(List<String> key, RpcConfigCallbackFunction configCallbackFunction) {
        Map<String, String> resMap = new HashMap<>();
        for (String s : key) {
            String s1 = get(s);
            resMap.put(s, s1);
        }
        for (String s : key) {
            subscribe(s, configCallbackFunction);
        }
        return resMap;
    }

    @Override
    public Map<String, String> get(List<String> keys) {
        Map<String, String> res = new HashMap<>();
        if (CollUtil.isEmpty(keys)) return res;
        for (String key : keys) {
            String s = newKeyMap.get(key);
            if (null == s) {
                String s1 = defaultGet(s);
                res.put(key, s1);
            }
        }
        return res;
    }

    @Override
    public String get(String keys) {
        if (StrUtil.isBlank(keys)) return null;
        String res = newKeyMap.get(keys);
        if (res == null) {
            res = defaultGet(keys);
        }
        return res;
    }

}
