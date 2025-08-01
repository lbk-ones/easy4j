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
package easy4j.infra.common.header;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.json.JacksonUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

/**
 * 检查工具类
 *
 * @author bokun
 */
public class CheckUtils {

    /**
     * 使用到了 中文注解
     *
     * @param t
     * @param message
     * @param <T>
     * @see io.swagger.v3.oas.annotations.media.Schema
     */
    @SafeVarargs
    public static <T> void checkByLambda(T t, Func1<T, ?>... message) {
        Set<String> resultList = new HashSet<>();
        Class<?> aClass = t.getClass();
        for (Func1<T, ?> trFunction : message) {
            String fieldName = LambdaUtil.getFieldName(trFunction);
            Field field = ReflectUtil.getField(aClass, fieldName);
            Object value = ReflectUtil.getFieldValue(t, field);
            if (ObjectUtil.isEmpty(value)) {
                Schema annotation = field.getAnnotation(Schema.class);
                if (Objects.nonNull(annotation)) {
                    String description = annotation.description();
                    if (StrUtil.isNotBlank(description)) {
                        description += "【" + fieldName + "】";
                        resultList.add(description);
                    } else {
                        resultList.add(field.getName());
                    }
                } else {
                    resultList.add(field.getName());
                }
            }
        }
        if (!resultList.isEmpty()) {
            String join = String.join("，", resultList);
            throw new EasyException("A00004," + join);
        }
    }

    public static String joinElementsAfterPosition(String[] array, int position, boolean boundary) {
        if (array == null || position < 0 || position >= array.length) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = position; i < array.length; i++) {
            if (boundary) {
                result.append(array[i]);
                if (i < array.length - 1) {
                    result.append(".");
                }
            } else {
                if (i > position) {
                    result.append(array[i]);
                    if (i < array.length - 1) {
                        result.append(".");
                    }
                }
            }

        }
        return result.toString();
    }

    public static Object getValueByPath(Object obj, String path) {
        return getValueByPath(obj, path, false);
    }

    /**
     * 根据路径表达式从对象中获取值，支持 list.[].wt 和 list.[0].wt 等形式
     *
     * @param obj       要从中获取值的对象
     * @param path      路径表达式 list.wt | list.[0].wt | list.[].wt.name | list.[0].wt.name.city
     * @param fastError 快速失败 list.[].wt 如果list集合有属性中的对象wt为空那么直接返回null
     * @return 获取到的值，如果未找到则返回 null
     * @author bokun
     */
    public static Object getValueByPath(Object obj, String path, boolean fastError) {
        if (obj == null || path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = obj;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (current == null || StrUtil.isBlank(part)) {
                return null;
            }
            boolean autoFix = false;
            // auto fix
            if (current instanceof List && !StrUtil.startWith(part, "[")) {
                part = "[]";
                autoFix = true;
            }
            if (part.startsWith("[")) {
                // allow list.[].[].wt
                if (current instanceof List) {
                    List<?> list = (List<?>) current;
                    if (part.equals("[]")) {
                        List<Object> returnRe = ListTs.newArrayList();
                        // handler list.[].wt.yy.[].ht
                        for (Object item : list) {
                            String s = joinElementsAfterPosition(parts, i, autoFix);
                            // if the value is null then the property does not exist
                            Object valueByPath = getValueByPath(item, s, fastError);
                            if (!ObjectUtil.isEmpty(valueByPath)) {
                                if (valueByPath instanceof Collection) {
                                    Collection<?> valueByPath1 = (Collection<?>) valueByPath;
                                    returnRe.addAll(valueByPath1);
                                } else {
                                    returnRe.add(valueByPath);
                                }
                            } else {
                                if (fastError) {
                                    return null;
                                }
                            }
                        }
                        return returnRe;
                    }

                    // handler list.[0].wt
                    try {
                        int index = Integer.parseInt(part.substring(1, part.length() - 1));
                        if (index < list.size()) {
                            current = list.get(index);
                        } else {
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                try {
                    if (current instanceof Map) {
                        Map<?, ?> current1 = (Map<?, ?>) current;
                        current = current1.get(part);
                    } else {
                        current = ReflectUtil.getFieldValue(current, part);
                    }
                } catch (Throwable e) {
                    current = null;
                }
            }
        }
        return current;
    }

    public static void checkParam(Object t, @NotNull String fieldName) {
        checkByPathWith(t, fieldName, "");
    }

    public static void checkByPath(Object t, String... message) {
        checkByPathWith(t, null, message);
    }

    /**
     * 根据路径检查参数是否为空
     *
     * @param t
     * @param message list.[].user.address.city 获取 list.[0].user.name.[].ahha
     */
    private static void checkByPathWith(Object t, String fname, String... message) {
        if (ObjectUtil.isEmpty(t)) {
            if (fname != null) {
                throw new EasyException("A00004," + fname);
            } else {
                throw new EasyException("A00004");
            }
        }
        Set<String> resultList = new HashSet<>();
        if (t instanceof Iterator) {
            Iterator<?> t1 = (Iterator<?>) t;
            while (t1.hasNext()) {
                Object next = t1.next();
                checkObj(resultList, next, message);
            }
        } else {
            checkObj(resultList, t, message);
        }
        if (!resultList.isEmpty()) {
            String join = String.join("，", resultList);
            throw new EasyException("A00004," + join);
        }
    }

    private static void checkObj(Set<String> resultList, Object next, String[] message) {
        if (Object.class.getName().equals(next.getClass().getName())) {
            return;
        }
        for (String fieldName : message) {
            if (StrUtil.isBlank(fieldName)) {
                continue;
            }
            Object valueByPath = getValueByPath(next, fieldName, true);
            if (ObjectUtil.isEmpty(valueByPath)) {
                resultList.add(fieldName);
            }
        }
    }

    /**
     * 检查是否为true 如果是true那么则抛出异常
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @Desc("检查是否为true 如果是true那么则抛出异常,msgCode是i18n代码，args是i18n占位符填充")
    public static void checkTrue(boolean flag, String msgCode, String... args) {
        if (flag && StrUtil.isNotBlank(msgCode)) {
            throw EasyException.wrap(msgCode, args);
        }
    }

    /**
     * 检查rpc结果
     *
     * @author bokun.li
     * @date 2025/6/18
     */
    @Desc("检查rpc结果，如果远程调用结果有问题，那么就兼容返回错误信息")
    public static <T> void checkRpcRes(EasyResult<T> easyResult) {
        if (easyResult == null) {
            throw EasyException.wrap(BusCode.A00045, "rpc result is null");
        }
        checkTrue(!easyResult.isSuccess(), BusCode.A00045, easyResult.getMsgAndError());
    }

    /**
     * 检查rpc结果data是否为空,同时检查异常信息
     *
     * @author bokun.li
     * @date 2025/6/27
     */
    @Desc("检查rpc结果data是否为空,同时检查异常信息")
    public static <T> void checkRpcData(EasyResult<T> easyResult) {
        if (easyResult == null) {
            throw EasyException.wrap(BusCode.A00045, "rpc result is null");
        }

        checkTrue(!easyResult.isSuccess(), BusCode.A00045, easyResult.getMsgAndError());

        T data = easyResult.getData();
        if (ObjectUtil.isEmpty(data)) {
            throw EasyException.wrap(BusCode.A00045, "data cannot be empty!");
        }
    }

    /**
     * 检查一个对象是否为空，为空则抛出异常
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @Desc("检查一个对象是否为空，为空则抛出异常,msgCode是i18n代码，args是i18n占位符填充")
    public static void checkObjIsNull(Object obj, String msgCode, String... args) {
        if (ObjectUtil.isEmpty(obj) && StrUtil.isNotBlank(msgCode)) {
            throw EasyException.wrap(msgCode, args);
        }
    }

    @Desc("检查一个对象是否为空，为空则抛出异常")
    public static void notNull(Object obj) {
        if (ObjectUtil.isEmpty(obj)) {
            throw EasyException.wrap("[Check failed] - this argument is required; it must not be null");
        }
    }

    @Desc("检查一个对象是否为空，为空则抛出异常,msgCode是i18n代码，args是i18n占位符填充")
    public static void notNull(Object obj, String msgOrMsgCode, String... args) {
        if (ObjectUtil.isEmpty(obj) && StrUtil.isNotBlank(msgOrMsgCode)) {
            throw EasyException.wrap(msgOrMsgCode, args);
        }
    }


    /**
     * 检查是否为空 如果是空那么则抛出异常
     *
     * @author bokun.li
     * @date 2025-06-15
     */
    @Desc("检查一个对象是否为空，为空则抛出异常：参数{name}不能为空")
    public static void checkParamNotNull(Object obj, String name) {
        if (ObjectUtil.isEmpty(obj)) {
            throw EasyException.wrap(BusCode.A00004, name);
        }
    }

    public static <T> T convertRpcRes(EasyResult<Object> securitySessionEasyResult, Class<T> securitySessionClass) {
        Object data = securitySessionEasyResult.getData();
        if (ObjectUtil.isNotEmpty(data)) {
            return JacksonUtil.toObject(JacksonUtil.toJson(data), securitySessionClass);
        }
        return null;
    }


    public static void checkInsert(boolean flag) {
        if (flag) {
            throw new EasyException(BusCode.A00048);
        }
    }

    public static void checkInsert(boolean flag, String message) {
        if (!flag) {
            throw EasyException.wrap(BusCode.A00048, message);
        }
    }

    public static void checkInsert(Supplier<Boolean> consumer) {
        try {
            Boolean aBoolean = consumer.get();
            if (null != aBoolean && !aBoolean) {
                throw EasyException.wrap(BusCode.A00048);

            }
        } catch (Throwable e) {
            throw EasyException.wrap(BusCode.A00048, e.getMessage());

        }
    }

    public static void checkInsert(Supplier<Boolean> consumer, String message) {
        try {
            Boolean aBoolean = consumer.get();
            if (null != aBoolean && !aBoolean) {
                throw EasyException.wrap(BusCode.A00048);
            }
        } catch (Throwable e) {
            throw EasyException.wrap(BusCode.A00048, message + e.getMessage());

        }
    }

    public static void checkUpdate(boolean flag) {
        if (!flag) {
            throw EasyException.wrap(BusCode.A00049);
        }
    }

    public static void checkUpdate(boolean flag, String message) {
        if (!flag) {
            throw EasyException.wrap(BusCode.A00049, message);
        }
    }

    public static void checkUpdate(Supplier<Boolean> consumer, String message) {
        try {
            Boolean aBoolean = consumer.get();
            if (null != aBoolean && !aBoolean) {
                throw EasyException.wrap(BusCode.A00049, message);
            }
        } catch (Throwable e) {
            throw EasyException.wrap(BusCode.A00049, message + e.getMessage());

        }
    }

    public static void checkUpdate(Supplier<Boolean> consumer) {
        try {
            Boolean aBoolean = consumer.get();
            if (null != aBoolean && !aBoolean) {
                throw EasyException.wrap(BusCode.A00049);
            }
        } catch (Throwable e) {
            throw EasyException.wrap(BusCode.A00049, e.getMessage());
        }
    }

    public static void checkDelete(boolean flag) {
        if (!flag) {
            throw EasyException.wrap(BusCode.A00050);
        }
    }

    public static void checkDelete(boolean flag, String message) {
        if (!flag) {
            throw EasyException.wrap(BusCode.A00050, message);
        }
    }

    public static void checkDelete(Supplier<Boolean> consumer) {
        try {
            Boolean aBoolean = consumer.get();
            if (null != aBoolean && !aBoolean) {
                throw EasyException.wrap(BusCode.A00050);
            }
        } catch (Throwable e) {
            throw EasyException.wrap(BusCode.A00050, e.getMessage());
        }
    }

    public static void checkDelete(Supplier<Boolean> consumer, String message) {
        try {
            Boolean aBoolean = consumer.get();
            if (null != aBoolean && !aBoolean) {
                throw EasyException.wrap(BusCode.A00050, message);
            }
        } catch (Throwable e) {
            throw EasyException.wrap(BusCode.A00050, message + e.getMessage());
        }
    }
}
