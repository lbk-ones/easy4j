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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ObjectHolder
 *
 * @author bokun.li
 * @date 2025-05
 */
public enum ObjectHolder{
    INSTANCE;
    private static final int MAP_SIZE = 8;
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>(MAP_SIZE);
    
    public Object getObject(String objectKey) {
        return OBJECT_MAP.get(objectKey);
    }
    
    public <T> T getObject(Class<T> clasz) {
        return clasz.cast(OBJECT_MAP.values().stream().filter(clasz::isInstance).findAny().orElseThrow(() -> new IllegalArgumentException("Can't find any object of class " + clasz.getName())));
    }

    public <T> T getOrNewObject(Class<T> clasz,T object) {
        T o = object;
        Optional<Object> any = OBJECT_MAP.values().stream().filter(clasz::isInstance).findAny();
        if (!any.isPresent()) {
            INSTANCE.setObject(object.getClass().getName(),object);
        }else{
            o = clasz.cast(any.get());
        }
        return o;
    }
    
    /**
     * Sets object.
     *
     * @param objectKey the key
     * @param object    the object
     * @return the previous object with the key, or null
     */
    public Object setObject(String objectKey, Object object) {
        return OBJECT_MAP.put(objectKey, object);
    }
    
}
