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
package easy4j.module.base.utils.xml;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import easy4j.module.base.annotations.Desc;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson XML处理工具类，支持驼峰转下划线、全部大写、保留空值
 */
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


}
