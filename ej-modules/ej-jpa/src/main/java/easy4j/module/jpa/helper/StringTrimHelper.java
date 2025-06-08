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
package easy4j.module.jpa.helper;


import cn.hutool.core.util.ReflectUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.module.jpa.annotations.Trim;

import java.lang.reflect.Field;

/**
 * StringTrimHelper
 *
 * @author bokun.li
 * @date 2025-05
 */
public class StringTrimHelper {

    public static void trim(Object obj) throws EasyException {
        Class<? extends Object> clazz = obj.getClass();

        for (Field field : ReflectUtil.getFields(clazz)) {
            if (field.isAnnotationPresent(Trim.class)) {
                Object o = ReflectUtil.getFieldValue(obj, field);
                if (o == null) {
                    continue;
                }
                if (o instanceof String) {
                    String f = (String) o;
                    ReflectUtil.setFieldValue(obj, field, f.trim());
                } else {
                    EasyException.throwExc("unable format not string field");
                }
            }
        }
    }
}
