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

import java.util.Map;
import java.util.Optional;

/**
 * Easy4jContext
 * 底层为三层键值对
 * 1、type
 * 2、name
 * 3、value
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface Easy4jContext {

    /**
     * 将value的值注入到当前线程中去
     *
     * @param key   第一层key
     * @param key2  第二层key
     * @param value 要注入的值
     */
    void registerThreadHash(String key, String key2, Object value);


    /**
     * 从当前线程中拿取key和key2对应的值
     *
     * @param key
     * @param key2
     * @return
     */
    Optional<Object> getThreadHashValue(String key, String key2);

    /**
     * 根据第一层key从当前线程拿取对应的第二层Map
     *
     * @param key
     * @return
     */
    Optional<Object> getThreadHash(String key);

    /**
     * 清除掉当前线程中缓存的所有变量
     */
    void clearHash();

    /**
     * 往当前上下文注入值
     * 第一层为DEFAULT
     * 第二层为传入的aclass的全类名
     * 第三层为对应的值t
     *
     * @param aclass
     * @param t
     */
    <T, R extends T> void set(Class<T> aclass, R t);

    /**
     * 往当前上下文注入值
     * 第一层为传入的type
     * 第二层为传入的aclass的全类名
     * 第三层为对应的值t
     *
     * @param aclass
     * @param t
     */
    void setType(String type, Class<?> aclass, Object t);

    /**
     * 根据第一层变量 拿取所有第二层的值
     *
     * @param type
     * @return
     */
    Map<String, Object> getType(String type);

    /**
     * 第一层键的名称为 DEFAULTTYPE
     * 第二层键的名称为传入的name
     * 第三层为传入的t
     *
     * @param name
     * @param t
     */
    void set(String name, Object t);

    /**
     * 第一层键的名称为 传入的type
     * 第二层键的名称为传入的name
     * 第三层为传入的t
     *
     * @param type 第一层键的名称
     * @param name 第二层键的名称
     * @param t
     */
    void setType(String type, String name, Object t);

    /**
     * 根据传入的第一层键和第二层键返回存储的值，并转化为想要的类型(传入的t)，如果没有会抛出异常
     *
     * @param type 第一层键
     * @param name 第二层键
     * @param t    将要转换的类型
     * @param <T>
     * @return
     */
    <T> T getType(String type, String name, Class<T> t);

    /**
     * 根据class的全类名来从DEFAULTTYPE中拿取值 如果没有会抛出异常
     *
     * @author bokun.li
     * @date 2025/6/3
     */
    <T> T get(Class<T> aclass);

    /**
     * 根据class类型来从上下文拿取 如果没有给个默认值
     * 其实是根据class的全类名为name来拿取
     * type = easy4j.module.base.context.DefaultEasy4jContext#DEFAULT_KEY
     *
     * @author bokun.li
     * @date 2025/6/3
     */
    <T> T getOrDefault(Class<T> aclass, T object);


    /**
     * 从DEFAULT_KEY中拿取key为name的值并转换成Class<T> 如果没有就返回传入的默认值
     *
     * @param name   根据名称
     * @param aclass 类型转换
     * @param def
     * @param <T>
     * @return
     */
    <T> T get(String name, Class<T> aclass, T def);


}
