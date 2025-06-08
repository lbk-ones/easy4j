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
package easy4j.infra.common.utils.lambda;


import cn.hutool.core.util.ReflectUtil;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.Map;


/**
 * EasyLambda
 *
 * @author bokun.li
 * @date 2025-05
 */
public class EasyLambda {
    private static final Map<SFunction<?, ?>, Field> FUNCTION_CACHE = new ConcurrentReferenceHashMap<>();

    public static <T, R> String getFieldName(SFunction<T, R> function) {
        Field field = getField(function);
        return field.getName();
    }

    public static <T, R> Field getField(SFunction<T, R> function) {
        return FUNCTION_CACHE.computeIfAbsent(function, EasyLambda::findField);
    }

    public static <T, R> Field findField(SFunction<T, R> function) {
        final SerializedLambda serializedLambda = getSerializedLambda(function);
        final String implClass = serializedLambda.getImplClass();
        final String implMethodName = serializedLambda.getImplMethodName();
        final String fieldName = convertToFieldName(implMethodName);
        final Field field = getField(fieldName, serializedLambda);
        if (field == null) {
            throw new RuntimeException("No such class 「" + implClass + "」 field 「" + fieldName + "」.");
        }
        return field;
    }

    static Field getField(String fieldName, SerializedLambda serializedLambda) {
        try {
            String declaredClass = serializedLambda.getImplClass().replace("/", ".");
            Class<?> aClass = ClassUtils.forName(declaredClass, getDefaultClassLoader());
            return ReflectUtil.getField(aClass, fieldName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("get class field exception.", e);
        }
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = EasyLambda.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    static String convertToFieldName(String getterMethodName) {
        // 获取方法名
        String prefix = null;
        if (getterMethodName.startsWith("get")) {
            prefix = "get";
        } else if (getterMethodName.startsWith("is")) {
            prefix = "is";
        }
        if (prefix == null) {
            throw new IllegalArgumentException("invalid getter method: " + getterMethodName);
        }
        // 截取get/is之后的字符串并转换首字母为小写
        return Introspector.decapitalize(getterMethodName.replace(prefix, ""));
    }

    static <T, R> SerializedLambda getSerializedLambda(SFunction<T, R> function) {
        try {
            return ReflectUtil.invoke(function, "writeReplace");
        } catch (Exception e) {
            throw new RuntimeException("get SerializedLambda exception.", e);
        }
    }
}
