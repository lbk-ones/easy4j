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
package easy4j.infra.base.resolve;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import jodd.util.StringPool;
import org.springframework.boot.context.properties.bind.Binder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

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
        return ListTs.asList(url, userName, password)
                .stream()
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());

    }

    public String getUrl(String p) {
        List<String> strings = splitUrl(p);
        return ListTs.get(strings, 0);
    }

    public String getHost(String p) {
        return Optional.ofNullable(p).map(e ->
                getUrl(p)
        ).map(e -> {
            String[] split = e.split("//");
            return ListTs.get(ListTs.asList(split), split.length - 1);
        }).map(e -> {
            String[] split = e.split(":");
            return ListTs.get(ListTs.asList(split), 0);
        }).orElse(null);
    }

    public String getPort(String p) {
        return Optional.ofNullable(p).map(e ->
                getUrl(p)
        ).map(e -> {
            String[] split = e.split("//");
            return ListTs.get(ListTs.asList(split), split.length - 1);
        }).map(e -> {
            String[] split = e.split(":");
            return ListTs.get(ListTs.asList(split), split.length - 1);
        }).map(e -> {
            try {
                int i = Integer.parseInt(e);
                return String.valueOf(i);
            } catch (Exception e2) {
                return "80";
            }
        }).orElse(null);
    }

    public String getUsername(String p) {
        List<String> strings = splitUrl(p);
        return ListTs.get(strings, 1);
    }

    public String getPassword(String p) {
        List<String> strings = splitUrl(p);
        return StrUtil.blankToDefault(ListTs.get(strings, 2), ListTs.get(strings, 1));
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


    /**
     * 设置Spring环境值
     *
     * @author bokun.li
     * @date 2025-06-05 21:38:46
     */
    public void setSpringProperty(Object properties, String name, Object value) {
        if (null == properties || StrUtil.isBlank(name)) {
            return;
        }
        String[] staticVs = EjSysProperties.getStaticVs(name);

        if (null == staticVs || staticVs.length == 0 || Arrays.stream(staticVs).noneMatch(StrUtil::isNotBlank)) {
            staticVs = new String[]{name};
        }

        for (String staticV : staticVs) {
            if (Properties.class.getName().equals(properties.getClass().getName())) {
                Properties properties1 = (Properties) properties;
                if (Objects.nonNull(value)) {
                    properties1.setProperty(staticV, Convert.toStr(value));
                }
            } else if (properties instanceof Map) {
                if (ObjectUtil.isNotEmpty(value)) {
                    Map properties2 = (Map) properties;
                    properties2.put(staticV, value);
                }
            }
        }


    }

    /**
     * 兼容获取数据库地址 以 url+@+username+:+password 方式拼接
     *
     * @author bokun.li
     * @date 2025/6/10
     */
    public String getNormalDbUrl() {
        String url1 = Easy4j.getProperty(SysConstant.DB_URL_STR);
        String username = Easy4j.getProperty(SysConstant.DB_USER_NAME);
        String password = Easy4j.getProperty(SysConstant.DB_USER_PASSWORD);
        if (StrUtil.isNotBlank(url1)) {
            if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
                url1 += SP.AT + username + SP.COLON + password;
            }
        } else {
            url1 = Easy4j.getProperty(SysConstant.DB_URL_STR_NEW);
            String username1 = StrUtil.blankToDefault(getUsername(url1), username);
            String password1 = StrUtil.blankToDefault(getPassword(url1), password);
            url1 = getUrl(url1) + SP.AT + username1 + SP.COLON + password1;
        }
        return url1;

    }

    // 兼容获取dataIds 如果没有后缀加上后缀
    public String getNormalDataIds(EjSysProperties ejSysProperties) {
        String dataIds = ejSysProperties.getDataIds();
        if (StrUtil.isBlank(dataIds)) {
            throw new EasyException("not get data-ids from env!");
        }
        String nacosConfigFileExtension = ejSysProperties.getNacosConfigFileExtension();
        List<String> objects = ListTs.newArrayList();
        for (String normalDataId : ListTs.asList(dataIds.split(SP.COMMA))) {
            String dataId = getDataId(normalDataId);
            String group = getGroup(normalDataId, null);
            String _nacosConfigFileExtension = StrUtil.blankToDefault(StrUtil.subSuf(dataId, dataId.lastIndexOf(SP.DOT) + 1), nacosConfigFileExtension);
            if (!dataId.endsWith(SP.DOT + _nacosConfigFileExtension)) {
                String dataIds2 = dataId + SP.DOT + _nacosConfigFileExtension;
                if (StrUtil.isNotBlank(group)) {
                    dataIds2 += "?group=" + group;
                }
                objects.add(dataIds2);
            } else {
                if (StrUtil.isNotBlank(group)) {
                    dataId += "?group=" + group;
                }
                objects.add(dataId);
            }
        }

        return String.join(SP.COMMA, objects);
    }
}
