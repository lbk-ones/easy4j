/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.json.JacksonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Map工具
 *
 * @author bokun.li
 * @date 2025/6/16
 */
public class EasyMap<K, V> extends HashMap<K, V> implements Map<K, V> {
    private static final long serialVersionUID = 334636121615156130L;
    private List<K> cacheKeys = ListTs.newArrayList();

    public EasyMap() {
    }

    public EasyMap(Map<K, V> map) {
        super.putAll(map);
    }

    /**
     * 删除指定缓存
     *
     * @author bokun.li
     * @date 2025/6/16
     */
    public void removeCache(List<K> listkeys) {

        for (K object : listkeys) {
            this.remove(object);
        }

    }

    /**
     * 全删除
     *
     * @author bokun.li
     * @date 2025/6/16
     */
    public void removeCache() {
        for (K object : this.cacheKeys) {
            this.remove(object);
        }

    }

    /**
     * 添加要缓存的key
     *
     * @param key
     */
    public void addCacheKey(K key) {
        this.cacheKeys.add(key);
    }

    /**
     * 删除缓存的KEY
     *
     * @param key
     */
    public void removeCacheKey(K key) {
        this.cacheKeys.remove(key);
    }


    /**
     * 清空所有缓存的key
     *
     * @author bokun.li
     * @date 2025/6/16
     */
    public void cleanCacheKey() {
        this.cacheKeys = ListTs.newArrayList();
    }

    /**
     * 当前map是否是空的
     *
     * @return
     */
    public boolean isNotNull() {
        return !this.keySet().isEmpty();
    }

    /**
     * 兼容put 第一个v没有就put第二个s
     *
     * @param k
     * @param v
     * @param s
     */
    public void put(K k, V v, V s) {
        if (v == null) {
            super.put(k, s);
        } else {
            super.put(k, v);
        }

    }

    /**
     * 当前map是否是空的 如果是空的那么就抛出指定异常
     *
     * @return
     */
    public void isNullThrow(String code) {
        if (this.keySet().isEmpty()) {
            throw new EasyException(code);
        }
    }

    /**
     * 列出所有缓存的key
     *
     * @return
     */
    public List<K> listKeys() {
        List<K> list = ListTs.newArrayList();
        Set<K> keyss = this.keySet();

        list.addAll(keyss);

        return list;
    }

    /**
     * 转成json
     *
     * @return
     */
    @Override
    public String toString() {
        return JacksonUtil.toJson(this);
    }

    /**
     * 深拷贝
     * 第一次可能有点慢，之后就好了
     *
     * @author bokun.li
     * @date 2025/6/16
     */
    public EasyMap<K, V> deepCopy() {
        return JacksonUtil.toObject(this.toString(), new TypeReference<EasyMap<K, V>>() {
        });
    }

    /**
     * 工厂方法
     *
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> EasyMap<K, V> get() {
        return new EasyMap<>();
    }
}