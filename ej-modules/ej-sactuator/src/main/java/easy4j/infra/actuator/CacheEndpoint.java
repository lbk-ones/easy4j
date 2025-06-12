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
package easy4j.infra.actuator;

import com.google.common.collect.Maps;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.context.api.cache.Easy4jCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CacheEndpoint
 *
 * @author bokun.li
 * @date 2025/6/12
 */
@Endpoint(id = "easy4j-cache")
@Component
public class CacheEndpoint {

    @Autowired
    Easy4jContext easy4jContext;


    @ReadOperation
    public String getCacheInfo() {
        Map<String, Object> map = Maps.newHashMap();
        Map<String, Easy4jCache> mapOfType = easy4jContext.getMapOfType(Easy4jCache.class);
        for (String s : mapOfType.keySet()) {
            Easy4jCache easy4jCache = mapOfType.get(s);
            String name = easy4jCache.getName();
            Easy4jCache.CacheStats stats = easy4jCache.getStats();
            map.putIfAbsent(name, stats);
        }
        return JacksonUtil.toJson(map);
    }

}
