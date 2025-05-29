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
package easy4j.module.base.console;

import cn.hutool.core.util.ReflectUtil;
import easy4j.module.base.annotations.Desc;
import easy4j.module.base.utils.BusCode;

import java.lang.reflect.Field;

/**
 * GenI18n
 *
 * @author bokun.li
 * @date 2025-05
 */
public class GenI18n {
    public static void main(String[] args) {
        Field[] fields = ReflectUtil.getFields(BusCode.class);
        for (Field field : fields) {
            String name = field.getName();
            if (field.isAnnotationPresent(Desc.class)) {
                Desc annotation = field.getAnnotation(Desc.class);
                String value = annotation.value();
                System.out.println(name + "=" + value.replaceAll(",", "，"));
            }
        }
    }
}
