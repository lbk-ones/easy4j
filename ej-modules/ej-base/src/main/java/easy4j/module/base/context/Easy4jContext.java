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
package easy4j.module.base.context;

import java.util.Optional;

/**
 * Easy4jContext
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface Easy4jContext {

    void registerThreadHash(String key, String key2, Object value);

    Optional<Object> getThreadHashValue(String key, String key2);

    Optional<Object> getThreadHash(String key);

    void clearHash();

    /**
     * 以 aclass 全类名作为default类型的key t作为值
     *
     * @param aclass
     * @param t
     */
    <T, R extends T> void set(Class<T> aclass, R t);

    /**
     * 以 aclass 全类名作为type类型的key t作为值
     *
     * @param aclass
     * @param t
     */
    void setType(String type, Class<?> aclass, Object t);

    <T> T getType(String type, Class<T> aclass);

    void set(String name, Object t);

    void setType(String type, String name, Object t);

    <T> T getType(String type, String name, Class<T> t);

    <T> T get(Class<T> aclass);

    <T> T get(String name, Class<T> aclass);


}
