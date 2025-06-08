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
package easy4j.module.idempotent;

import easy4j.infra.context.api.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.infra.context.api.idempotent.Easy4jIdempotentStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IdempotentToolFactory
 *
 * @author bokun.li
 * @date 2025-05
 */
@Component
public class IdempotentToolFactory {
    private static final AtomicInteger hasRedis = new AtomicInteger(0);

    @Autowired
    private Map<String, Easy4jIdempotentKeyGenerator> keyGenerators;

    @Autowired
    private Map<String, Easy4jIdempotentStorage> storageServices;

    public Easy4jIdempotentKeyGenerator getKeyGenerator(String type) {
        return keyGenerators.get(type + "KeyGenerator");
    }

    public Easy4jIdempotentStorage getStorage(StorageTypeEnum type) throws RuntimeException {
        if (type == StorageTypeEnum.REDIS) {
            try {
                if (hasRedis.get() == 0) {
                    hasRedis.set(1);
                    this.getClass().getClassLoader().loadClass("easy4j.module.redis.Config");
                }
            } catch (Exception e) {
                throw new RuntimeException("未引入redis模块请 请引入 ej-redis 模块");
            }
        }
        return storageServices.get(type.getType() + "IdempotentStorage");
    }
}
