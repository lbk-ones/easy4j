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

import java.util.Optional;

/**
 * Easy4jContextThread
 * 线程上下文
 *
 * @author bokun.li
 * @date 2025-05
 */
public interface Easy4jContextThread {

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


}
