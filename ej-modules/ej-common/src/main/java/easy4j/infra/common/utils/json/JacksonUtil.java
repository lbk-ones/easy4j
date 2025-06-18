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
package easy4j.infra.common.utils.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jackson通用工具类，封装常用的JSON序列化和反序列化操作
 */
public class JacksonUtil {

    @Getter
    private static final ObjectMapper mapper;

    @Getter
    private static final ObjectMapper mapper2;

    static {
        mapper = new ObjectMapper();
        // 配置序列化选项
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 忽略null值
        extracted(mapper);

        mapper2 = new ObjectMapper();
        // 配置序列化选项
        //mapper2.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 保留null值
        extracted(mapper2);
    }

    private static void extracted(ObjectMapper objectMapper) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 空对象不报错
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期不转时间戳

        // 配置反序列化选项
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性

        // 日期时间格式化
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

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
        objectMapper.registerModule(timeModule);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    }

    /**
     * 对象转JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    public static String toJsonContainNull(Object obj) {
        try {
            return mapper2.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON字符串转对象
     *
     * @param json  JSON字符串
     * @param clazz 目标对象类型
     * @param <T>   泛型
     * @return 对象实例
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON字符串转复杂对象（如泛型集合、嵌套对象等）
     *
     * @param json          JSON字符串
     * @param typeReference 类型引用
     * @param <T>           泛型
     * @return 对象实例
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON字符串转List
     *
     * @param json         JSON字符串
     * @param elementClass 元素类型
     * @param <T>          泛型
     * @return List集合
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, elementClass);
        try {
            return mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * JSON字符串转Map
     *
     * @param json       JSON字符串
     * @param keyClass   键类型
     * @param valueClass 值类型
     * @param <K>        键泛型
     * @param <V>        值泛型
     * @return Map实例
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyClass, Class<V> valueClass) {
        JavaType javaType = mapper.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        try {
            return mapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从JSON字符串中获取指定路径的值
     *
     * @param json JSON字符串
     * @param path JSON路径（如 "user.name"）
     * @return 值
     */
    public static Object getValue(String json, String path) {
        try {
            JsonNode root = mapper.readTree(json);
            String[] paths = path.split("\\.");
            JsonNode currentNode = root;
            for (String p : paths) {
                if (currentNode == null || currentNode.isMissingNode()) {
                    return null;
                }
                currentNode = currentNode.get(p);
            }
            return currentNode.isValueNode() ? currentNode.asText() : currentNode;
        } catch (IOException e) {
            throw new RuntimeException("JSON解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 深拷贝对象
     *
     * @param source 源对象
     * @param clazz  目标类型
     * @param <T>    泛型
     * @return 拷贝后的对象
     */
    public static <T> T deepCopy(Object source, Class<T> clazz) {
        if (source == null) {
            return null;
        }
        String json = toJson(source);
        return toObject(json, clazz);
    }

    /**
     * 美化JSON字符串（格式化输出）
     *
     * @param json JSON字符串
     * @return 美化后的JSON字符串
     */
    public static String prettyPrint(String json) {
        try {
            Object obj = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON格式化失败: " + e.getMessage(), e);
        }
    }

    /**
     * 判断字符串是否为有效JSON
     *
     * @param json JSON字符串
     * @return 是否有效
     */
    public static boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static Object readValue(String json) {

        try {
            return mapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON格式化失败: " + e.getMessage(), e);
        }

    }


//    public static void main(String[] args) {
//        Map<String, Object> paramMap = Maps.newHashMap();
//        paramMap.put("test1", "2323");
//        paramMap.put("test3", new Date());
//        paramMap.put("test4", LocalDate.now());
//        paramMap.put("test5", LocalDateTime.now());
//        paramMap.put("test6", new BigDecimal("2323.161662345236513453245"));
//        paramMap.put("test7", null);
//        paramMap.put("test8", true);
//        paramMap.put("test9", 123.9);
//        String json = toJson(paramMap);
//        System.out.println(json);
//
//
//    }

    public static String writeValueAsString(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static String compress(String jsonString) {

        Object json;
        try {
            json = mapper.readValue(jsonString, Object.class);
            StringWriter writer = new StringWriter();
            JsonFactory factory = mapper.getFactory();

            try (JsonGenerator generator = factory.createGenerator(writer)) {
                // 禁用自动缩进
                generator.setPrettyPrinter(null);
                mapper.writeValue(generator, json);
            }
            return writer.toString();
        } catch (Exception ignored) {

        }
        return jsonString;
    }

}
