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
package easy4j.infra.common.utils.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import easy4j.infra.common.annotations.Desc;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jackson XML处理工具类，支持驼峰转下划线、全部大写、保留空值
 */
@Slf4j
public class JacksonXmlUtil {

    public static ObjectMapper xmlMapper;

    static {
        xmlMapper = new XmlMapper();
//        xmlMapper.setPropertyNamingStrategy(new UpperSnakeCaseNamingStrategy());
//            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 包括null值
        xmlMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 空对象不报错
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期不转时间戳

        // 配置反序列化选项
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性

        // 日期时间格式化
        xmlMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // Java 8日期时间支持
        JavaTimeModule timeModule = new JavaTimeModule();
        // 序列化配置
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        // 反序列化配置
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        xmlMapper.registerModule(timeModule);

    }

    public static <T> T parseXmlToObject(String xml, Class<T> clazz) {
        try {
            return xmlMapper.readValue(xml, clazz);
        } catch (Exception e) {
            throw new RuntimeException("XML反序列化失败: " + e.getMessage(), e);
        }
    }

    public static <T> T parseXmlToObject(String xml, TypeReference<T> tTypeReference) {
        try {
            return xmlMapper.readValue(xml, tTypeReference);
        } catch (Exception e) {
            throw new RuntimeException("XML反序列化失败: " + e.getMessage(), e);

        }
    }

    public static String toXml(Object object) {
        try {
            return xmlMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("XML序列化失败: " + e.getMessage(), e);

        }
    }

    @Desc("简单生成一下代码")
    public static void printXmlBean(Class<?> aclass) {
        Field[] fields = ReflectUtil.getFields(aclass);
        for (Field field : fields) {
            String name = field.getName();
            String t = "@JacksonXmlProperty(localName = \"" + StrUtil.toUnderlineCase(name).toUpperCase() + "\")";
            String t2 = "public String \"" + name + "\";";
            System.out.println(t);
            System.out.println(t2);
        }
    }


    public static void checkXml(Object object) {
        if (null != object && !object.getClass().getName().equals("java.lang.Object")) {
            try {
                Field[] fields = ReflectUtil.getFields(object.getClass());
                for (Field field : fields) {
                    if (
                            !Modifier.isStatic(field.getModifiers()) &&
                                    !Modifier.isFinal(field.getModifiers()) &&
                                    !Modifier.isTransient(field.getModifiers())
                    ) {
                        Object fieldValue = ReflectUtil.getFieldValue(object, field);
                        if (Objects.nonNull(fieldValue)) {
                            if (fieldValue instanceof String) {
                                String var1 = (String) fieldValue;
                                if (StrUtil.isNotBlank(var1)) {
                                    String regex = "<!\\[CDATA\\[[\\s\\S]*?]]>";
                                    boolean matches = Pattern.matches(regex, var1);
                                    if (!matches) {
                                        String regex2 = "['\"&<>]";
                                        Pattern compile = Pattern.compile(regex2);
                                        Matcher matcher = compile.matcher(var1);
                                        if (matcher.find()) {
                                            String s = "<![CDATA[" + var1 + "]]>";
                                            ReflectUtil.setFieldValue(object, field, s);
                                        }
                                    }
                                }
                            } else if (fieldValue instanceof Iterable) {
                                Iterable<?> fieldValue1 = (Iterable<?>) fieldValue;
                                if (CollUtil.isNotEmpty(fieldValue1)) {
                                    for (Object o : fieldValue1) {
                                        checkXml(o);
                                    }
                                }
                            } else if (fieldValue instanceof Map) {
                                Map<?, ?> fieldValue1 = (Map<?, ?>) fieldValue;
                                if (CollUtil.isNotEmpty(fieldValue1)) {
                                    for (Object value : fieldValue1.values()) {
                                        checkXml(value);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("xml检查出现异常--->" + e.getMessage());
            }

        }

    }

}
