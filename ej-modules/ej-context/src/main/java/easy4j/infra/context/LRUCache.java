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
package easy4j.infra.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于LRU策略的固定大小缓存Map
 * 当容量达到上限时，自动移除最久未使用的元素
 * 基于LinkedHashMap
 *
 * @author bokun.li
 * @date 2025-08-10
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize; // 最大容量

    public LRUCache(int maxSize) {
        // 初始化：容量为maxSize，负载因子0.75，accessOrder=true（按访问顺序排序）
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }

    /**
     * 重写此方法，当元素数量超过maxSize时返回true，触发移除最老元素
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    // 测试
    public static void main(String[] args) {
        LRUCache<String, Integer> cache = new LRUCache<>(3); // 最大容量3
        cache.put("a", 1);
        cache.put("b", 2);
        cache.put("c", 3);
        System.out.println(cache); // {a=1, b=2, c=3}

        cache.get("a"); // 访问"a"，使其成为最近使用
        cache.put("d", 4); // 容量超3，移除最久未使用的"b"
        System.out.println(cache); // {a=1, c=3, d=4}
    }
}
