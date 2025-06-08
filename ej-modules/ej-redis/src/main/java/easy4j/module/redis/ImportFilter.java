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
package easy4j.module.redis;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

/**
 * exclude redis autoconfigure
 *
 * @author bokun.li
 * @date 2025/6/6
 */
public class ImportFilter implements AutoConfigurationImportFilter {


    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            matches[i] = !shouldExclude(autoConfigurationClasses[i]);
        }
        return matches;
    }

    private boolean shouldExclude(String autoConfigurationClass) {
        if (StrUtil.isNotBlank(autoConfigurationClass)) {
            boolean contains = autoConfigurationClass.contains("Redis");
            if (contains) {
                String eas4jRedisUrl = Easy4j.getProperty(SysConstant.EASY4J_REDIS_URL);
                String redisUrl = Easy4j.getProperty(SysConstant.SPRING_REDIS_URL);
                String redisHost = Easy4j.getProperty(SysConstant.SPRING_REDIS_HOST);
                return StrUtil.isBlank(eas4jRedisUrl) && StrUtil.isBlank(redisUrl) && StrUtil.isBlank(redisHost);
            }
        }
        return false;
    }
}
