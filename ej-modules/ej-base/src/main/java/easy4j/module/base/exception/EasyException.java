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
package easy4j.module.base.exception;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.annotations.Desc;
import easy4j.module.base.plugin.i18n.I18nUtils;
import easy4j.module.base.utils.ListTs;
import jodd.util.StringPool;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * EasyException
 *
 * @author bokun.li
 * @date 2025-05
 */
@Getter
public class EasyException extends RuntimeException {

    private final int error = 1;

    public EasyException(String message) {
        super(message);
    }

    /**
     * message参数占位
     * 比如 A00031=这是消息{0},{0}
     * 可以 throw EasyException.wrap("A00031","test","test2")
     *
     * @param message
     * @param args
     * @return
     */
    public static EasyException wrap(String message, String... args) {
        StringBuilder messageBuilder = new StringBuilder(message);
        for (String arg : args) {
            String s = arg.replaceAll(",", "，");
            if (StrUtil.isNotBlank(s)) {
                messageBuilder.append(",").append(s);
            }
        }
        message = messageBuilder.toString();
        return new EasyException(message);
    }


    public static EasyException throwExc(String message) throws EasyException {
        if (StrUtil.isBlank(message)) {
            throw new EasyException(I18nUtils.getOperateErrorStr());
        } else {
            throw new EasyException(message);
        }
    }

    /**
     * 参数为空则抛出异常
     *
     * @param object
     * @param paramName
     * @return
     */
    @Desc("为空抛出异常")
    public static void isNullThrow(Object object, String paramName) {
        String s = handlerDotMessage(paramName);
        if (Objects.isNull(object)) {
            throw new EasyException(I18nUtils.getMessage("A00004", s));
        } else if (StrUtil.isBlankIfStr(object)) {
            throw new EasyException(I18nUtils.getMessage("A00004", s));
        } else {
            if (object instanceof Collection) {
                Collection<?> object1 = (Collection<?>) object;
                if (object1.isEmpty()) {
                    throw new EasyException(I18nUtils.getMessage("A00004", s));
                }
            }
        }
    }

    @Desc("为true抛出异常")
    public static void isTrueThrow(boolean flag, String msgKey, String msgContent) {
        if (flag) {
            if (StrUtil.isBlank(msgKey)) {
                throw new EasyException(I18nUtils.getOperateErrorStr());
            } else {
                throw new EasyException(I18nUtils.getMessage(msgKey, msgContent));
            }
        }
    }

    @Desc("检查参数是否为空")
    public static <T extends Annotation> void checkParamsByAnnotation(Object object, Class<T> annotation, Predicate<T> predicate, Function<Field, String> functionGet) {

        if (!(object instanceof Map) && Objects.nonNull(object)) {

            Field[] fields = ReflectUtil.getFields(object.getClass());

            List<String> params = ListTs.newArrayList();
            for (Field field : fields) {
                T annotation1 = field.getAnnotation(annotation);
                if (Objects.nonNull(annotation1) && predicate.test(annotation1)) {
                    String apply = functionGet.apply(field);
                    params.add(apply);
                }
            }

            String join = String.join("，", params);

            String s = handlerDotMessage(join);

            throw new EasyException(I18nUtils.getMessage("A00004", s));

        }


    }

    private static String handlerDotMessage(String message) {
        String s12 = message;
        if (StrUtil.isNotBlank(message)) {
            String[] split = message.split(StringPool.COMMA);
            if (split.length > 1) {
                String s = split[0];
                String s1 = s + StringPool.COMMA;
                List<String> objects = ListTs.newArrayList();
                for (int i = 0; i < split.length; i++) {
                    if (i > 0) {
                        objects.add(split[i]);
                    }
                }
                s12 = s1 + String.join("，", objects);
            }
        }
        return s12;
    }
}
