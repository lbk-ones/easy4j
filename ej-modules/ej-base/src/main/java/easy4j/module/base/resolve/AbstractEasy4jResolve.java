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
package easy4j.module.base.resolve;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SP;
import easy4j.module.base.utils.SysConstant;
import jodd.util.StringPool;
import org.springframework.boot.context.properties.bind.Binder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;

/**
 * AbstractEasy4jResolve
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractEasy4jResolve<T, R> implements Easy4jResolve<T, R> {

    List<String> splitUrl(String p) {
        String[] split = p.split(SP.AT);

        String url = null;
        String userName = null;
        String password = null;
        try {
            url = split[0];
            String s = split[1];
            String[] split1 = s.split(SP.COLON);
            userName = split1[0];
            password = split1[1];
        } catch (Exception e) {

        }
        return ListTs.asList(url, userName, password);

    }

    public String getUrl(String p) {
        List<String> strings = splitUrl(p);
        return ListTs.get(strings, 0);
    }

    public String getUsername(String p) {
        List<String> strings = splitUrl(p);
        return ListTs.get(strings, 1);
    }

    public String getPassword(String p) {
        List<String> strings = splitUrl(p);
        return ListTs.get(strings, 2);
    }


    public String getDbType() {
        return Easy4j.getDbType();
    }

    public String getDbUrl() {
        return Easy4j.getDbUrl();
    }

    public String getProperty(String name) {
        return Easy4j.getProperty(name);
    }


    public void setProperties(Properties properties, String proName, String value) {
        if (StrUtil.isNotBlank(value) && StrUtil.isNotBlank(proName)) {
            properties.setProperty(proName, value);
        }
    }

    public void setPropertiesArr(Properties properties, String[] proName, String value) {
        if (StrUtil.isNotBlank(value) && proName != null) {
            for (String s : proName) {
                properties.setProperty(s, value);
            }
        }
    }


    public static String getEjSysPropertyName(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || Modifier.isTransient(modifiers)) {
            return null;
        }
        String name = field.getName();
        String underlineCase = StrUtil.toUnderlineCase(name);
        String lowerCase = underlineCase.toLowerCase();
        String s = lowerCase.replaceAll(StringPool.UNDERSCORE, StringPool.DASH);
        return SysConstant.PARAM_PREFIX + StringPool.DOT + s;
    }


    public static List<String> getConfigImports() {

        return Binder.get(Easy4j.environment)
                .bind(SysConstant.SPRING_CONFIG_IMPORT, String[].class)
                .map(ListTs::asList)
                .orElse(ListTs.newArrayList());
    }

    public List<String> splitDataId(String dataId) {
        List<String> list = ListTs.newArrayList();
        if (dataId.contains("?")) {
            String[] split = dataId.split("\\?group=");
            list = ListTs.asList(split);
        } else {
            list.add(dataId);
        }
        return list;
    }

    public String getDataId(String dataId) {
        List<String> strings = splitDataId(dataId);
        return ListTs.get(strings, 0);
    }

    public String getGroup(String dataId, String group) {
        List<String> strings = splitDataId(dataId);
        return StrUtil.blankToDefault(ListTs.get(strings, 1), group);
    }

}
