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
package easy4j.module.base.utils;

import cn.hutool.core.util.ReflectUtil;
import easy4j.module.base.exception.EasyException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于在代码中加载SPI服务
 * 第一次加载
 * 第二次反射 不完全反射 也会存储
 */
public class ServiceLoaderUtils {
    
    private static final Map<Class<?>, Collection<Class<?>>> SERVICES = new ConcurrentHashMap<>();
    
    /**
     * 加载SPI服务.并缓存起来供第二次使用
     *
     * @param service service class
     * @param <T> type of service
     * @return service instances
     */
    public static <T> List<T> load(final Class<T> service) {
        if (SERVICES.containsKey(service)) {
            return newServiceInstances(service);
        }
        Collection<T> result = new LinkedHashSet<>();
        for (T each : ServiceLoader.load(service)) {
            result.add(each);
            cacheServiceClass(service, each);
        }
        return ListTs.newLinkedList(result);
    }
    
    private static <T> void cacheServiceClass(final Class<T> service, final T instance) {
        if (!SERVICES.containsKey(service)) {
            SERVICES.put(service, new LinkedHashSet<>());
        }
        SERVICES.get(service).add(instance.getClass());
    }
    
    /**
     * 反射创建插件
     *
     * @param service service class
     * @param <T> type of service
     * @return service instances
     */
    public static <T> List<T> newServiceInstances(final Class<T> service) {
        return SERVICES.containsKey(service) ? newServiceInstancesFromCache(service) : ListTs.<T>newArrayList();
    }
    
    @SuppressWarnings("unchecked")
    private static <T> List<T> newServiceInstancesFromCache(Class<T> service) {
        Collection<T> result = new LinkedHashSet<>();
        for (Class<?> each : SERVICES.get(service)) {
            result.add((T) newServiceInstance(each));
        }
        return ListTs.newLinkedList(result);
    }
    
    private static Object newServiceInstance(final Class<?> clazz) {
        try {
            return ReflectUtil.newInstance(clazz);
        } catch (Exception e) {
            throw new EasyException(e.getMessage());
        }
    }
}
