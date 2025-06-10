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

/**
 * Easy4jContext
 * 算了 把这个上下文挂在 ioc容器里面吧
 *
 * @author bokun.li
 * @date 2025-06-07 18:36:12
 */
public interface Easy4jContext extends Easy4jContextThread {

    /**
     * 注册对象
     *
     * @param object
     */
    void register(Object object);

    /**
     * 根据名称注册对象
     *
     * @param name
     * @param object
     */
    void register(String name, Object object);

    /**
     * 根据名称（这个名称要么就是注册的时候指定的、要么就是默认名称）获取
     *
     * @param name
     * @return
     */
    Object get(String name);

    /**
     * 根据名称（这个名称要么就是注册的时候指定的、要么就是默认名称）获取，获取之后 同时转成对应的类型
     *
     * @param name
     * @param aClass
     * @param <T>
     * @return
     */
    <T> T get(String name, Class<T> aClass);

    /**
     * 根据类型获取
     *
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T get(Class<T> tClass);


    /**
     * 根据类型获取所有实现
     *
     * @param tClass
     * @param <T>
     * @return
     */
    <T> Map<String, T> getMapOfType(Class<T> tClass);

    /**
     * 根据类型获取所有实现
     *
     * @param tClass
     * @param <T>
     * @return
     */
    <T> String[] getNamesOfType(Class<T> tClass);


}
