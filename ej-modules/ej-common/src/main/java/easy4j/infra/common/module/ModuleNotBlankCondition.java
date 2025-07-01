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
package easy4j.infra.common.module;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

/**
 * ModuleCondition
 *
 * @author bokun.li
 * @date 2025-05
 */
public class ModuleNotBlankCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(ModuleBoolean.class.getName());
        if (attributes == null) {
            return true;
        }
        final Environment environment = context.getEnvironment();
        for (Object value : attributes.get("value")) {
            if (ObjectUtil.isEmpty(value)) {
                continue;
            }
            String[] moduleName = (String[]) value;
            for (String module : moduleName) {
                String s = environment.resolvePlaceholders(module);
                if (StrUtil.isBlank(s)) {
                    return false;
                }
            }
        }
        return true;
    }
}
