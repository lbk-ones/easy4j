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
package easy4j.infra.base.console;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.properties.SpringVs;
import easy4j.infra.common.utils.SysConstant;
import jodd.util.StringPool;

import java.lang.reflect.Field;

/**
 * 生成系统参数描述
 */
public class GenSysVarDesc {
    public static void main(String[] args) {
        Field[] fields = ReflectUtil.getFields(EjSysProperties.class);
        for (Field field : fields) {
            String name = field.getName();
            SpringVs annotation = field.getAnnotation(SpringVs.class);
            String value = annotation.desc();
            String lowerCase = StrUtil.toUnderlineCase(name).toLowerCase();
            String replace = SysConstant.PARAM_PREFIX + StringPool.DOT + lowerCase.replace(StringPool.UNDERSCORE, StringPool.DASH);
            String print = "- **" + replace + "**: " + value;
            System.out.println(print);
        }
    }
}
