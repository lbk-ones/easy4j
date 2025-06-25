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
package easy4j.infra.sca.seata;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

/**
 * ImportFilter
 *
 * @author bokun.li
 * @date 2025/6/24
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

    // return true will been exclude
    private boolean shouldExclude(String autoConfigurationClass) {
        if (StrUtil.isNotBlank(autoConfigurationClass)) {
            boolean contains = autoConfigurationClass.contains("Seata");
            if (contains) {
                return !Easy4j.getProperty(SysConstant.EASY4J_SEATA_ENABLE, boolean.class);
            }
        }
        return false;
    }
}

