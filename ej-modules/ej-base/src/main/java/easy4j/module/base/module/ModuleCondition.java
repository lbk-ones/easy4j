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
package easy4j.module.base.module;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysConstant;
import jodd.util.StringPool;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;

import java.util.List;

/**
 * ModuleCondition
 *
 * @author bokun.li
 * @date 2025-05
 */
public class ModuleCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(Module.class.getName());
        if (attributes == null) {
            return true;
        }
        final Environment environment = context.getEnvironment();
        for (Object value : attributes.get("value")) {
            String[] moduleName = (String[]) value;
            for (String module : moduleName) {
                String s = environment.resolvePlaceholders(module);
                List<String> list = ListTs.asList(s.split(":"));
                String s2 = ListTs.get(list, 0);
                String s3 = ListTs.get(list, 1);
                boolean defaultValue = false;
                if(StrUtil.isNotBlank(s3)){
                    try{
                        defaultValue= Boolean.parseBoolean(s3);
                    }catch (Exception ignored){}
                }
                assert s2 != null;
                if (environment.getProperty(s2, boolean.class, defaultValue)) {
                    return true;
                }
            }
        }
        return false;
    }
}
