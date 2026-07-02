package io.github.lbkones.encryption.util;

import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lbkones.encryption.annotation.MaskField;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 字段脱敏工具类
 */
public class MaskingUtil {

    private static final ObjectMapper objectMapper = EncryptionJson.getMapper();

    /**
     * 类是否需要脱敏的缓存
     * true  = 该类本身或其字段中存在需要脱敏的字段
     * false = 该类完全不需要脱敏，递归时可直接跳过
     */
    private static final Map<Class<?>, Boolean> CLASS_NEED_MASK_CACHE = new ConcurrentReferenceHashMap<>();

    /**
     * 对响应体进行字段脱敏处理（递归）
     */
    public static <T> T maskFields(T object) {
        if (object == null) {
            return null;
        }
        doMask(object, new IdentityHashMap<>());
        return object;
    }

    /**
     * 递归脱敏核心方法
     *
     * @param object  当前处理对象
     * @param visited 已访问对象集合，防止循环引用
     */
    private static void doMask(Object object, IdentityHashMap<Object, Boolean> visited) {
        if (object == null) {
            return;
        }

        Class<?> clazz = object.getClass();

        // 基础类型、包装类、String 直接跳过（String 本身由上层处理）
        if (isSkippableType(clazz)) {
            return;
        }

        // 处理集合
        if (object instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) object) {
                doMask(item, visited);
            }
            return;
        }

        // 处理 Map
        if (object instanceof Map<?, ?>) {
            for (Object value : ((Map<?, ?>) object).values()) {
                doMask(value, visited);
            }
            return;
        }

        // 处理数组
        if (clazz.isArray()) {
            if (!clazz.getComponentType().isPrimitive()) {
                for (Object item : (Object[]) object) {
                    doMask(item, visited);
                }
            }
            return;
        }

        // 防止循环引用
        if (visited.containsKey(object)) {
            return;
        }
        visited.put(object, Boolean.TRUE);

        // 查缓存：该类确认不需要脱敏则跳过
        if (Boolean.FALSE.equals(CLASS_NEED_MASK_CACHE.get(clazz))) {
            return;
        }

        Field[] fields = ReflectUtil.getFields(clazz);
        boolean classHasMask = false;

        for (Field field : fields) {
            Object value = ReflectUtil.getFieldValue(object,field);
            MaskField annotation = field.getAnnotation(MaskField.class);
            if (annotation != null && value instanceof String) {
                // 对 String 字段进行脱敏
                String masked = maskString((String) value,
                        annotation.prefixLength(),
                        annotation.suffixLength(), annotation.padding());
                ReflectUtil.setFieldValue(object,field,masked);
                classHasMask = true;
            } else if (value != null && !isSkippableType(field.getType())) {
                // 递归处理非基础类型字段
                doMask(value, visited);
            }
        }

        // 更新缓存：只有当该类本身没有 @MaskField 时才写入 false
        // 有 @MaskField 的类写入 true，下次仍会进入字段处理
        CLASS_NEED_MASK_CACHE.putIfAbsent(clazz, classHasMask);
    }

    /**
     * 判断是否为可跳过的类型（不需要递归进去）
     * 基础类型、包装类型、String、枚举、常见 JDK 值类型
     */
    private static boolean isSkippableType(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz.isEnum()
                || clazz == String.class
                || clazz == Class.class
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || Date.class.isAssignableFrom(clazz)
                || clazz.getName().startsWith("java.time.")
                || clazz.getName().startsWith("java.lang.")
                || clazz.getName().startsWith("java.math.");
    }

    /**
     * 对字符串进行脱敏
     *
     * @param value        原始值
     * @param prefixLength 保留前多少位
     * @param suffixLength 保留后多少位
     * @param padding 填充符号
     * @return 脱敏后的值
     */
    public static String maskString(String value, int prefixLength, int suffixLength, String padding) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        int length = value.length();

        // 位数不够，直接返回原值
        if (prefixLength + suffixLength >= length) {
            return value;
        }

        prefixLength = Math.max(0, prefixLength);
        suffixLength = Math.max(0, suffixLength);

        int maskLength = length - prefixLength - suffixLength;

        return value.substring(0, prefixLength)
                + padding.repeat(maskLength)
                + (suffixLength > 0 ? value.substring(length - suffixLength) : "");
    }

    /**
     * 对对象进行脱敏处理，返回脱敏后的 JSON 字符串
     */
    public static String maskToJson(Object object) {
        try {
            Object masked = maskFields(object);
            return objectMapper.writeValueAsString(masked);
        } catch (Exception e) {
            throw new RuntimeException("Failed to mask to JSON", e);
        }
    }
}
