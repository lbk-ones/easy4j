package easy4j.infra.rpc.serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import easy4j.infra.rpc.exception.RpcException;
import easy4j.infra.rpc.utils.ByteArrayDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.nio.charset.StandardCharsets.UTF_8;
@Slf4j
public class JacksonSerializable implements ISerializable {

    private final ObjectMapper objectMapper;

    public JacksonSerializable() {
        objectMapper = new ObjectMapper();
        extracted();
    }

    private void extracted() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 空对象不报错
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期不转时间戳
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true); // bigdecimal 转 string

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
        timeModule.addSerializer(long.class, new ToStringSerializer());
        timeModule.addSerializer(Long.class, new ToStringSerializer());
        timeModule.addSerializer(BigInteger.class, new ToStringSerializer());
        timeModule.addSerializer(byte[].class, new ByteArraySerializer());
        // 反序列化配置
        timeModule.addDeserializer(byte[].class, new ByteArrayDeserializer());
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
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] serializable(Object object) {
        if (object == null) {
            return null;
        }
        String json = "";
        try {
            json = toJson(object);
        } catch (Exception e) {
            log.error("json serialize exception.", e);
        }
        return json.getBytes(UTF_8);
    }

    @Override
    public <T> T deserializable(byte[] object, Class<T> tClass) {
        String s = new String(object, UTF_8);
        try {
            return objectMapper.readValue(s, tClass);
        } catch (JsonProcessingException e) {
            throw new RpcException(e);
        }
    }
}
