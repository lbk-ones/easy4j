package easy4j.infra.dbaccess.dynamic.dll;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import easy4j.infra.common.utils.ObjectHolder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * H2 数据库字段类型枚举，包含版本支持、简介及对应Java类型
 * 枚举的顺序很重要，第一个类型代表同类默认值
 *
 * @author bokun.li
 * @date 2025/9/10
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum H2SqlFieldType {
    // ========================= 整数类型 =========================
    INT("INT", null, "", "4字节有符号整数，范围-2147483648~2147483647（同 PostgreSQL INT4/MySQL INT）", int.class, Integer.class),
    INTEGER("INTEGER", null, "", "INT 的别名，4字节有符号整数", int.class, Integer.class),
    SMALLINT("SMALLINT", null, "", "2字节有符号整数，范围-32768~32767", short.class, Short.class),
    TINYINT("TINYINT", null, "", "1字节有符号整数，范围-128~127", byte.class, Byte.class),
    BIGINT("BIGINT", null, "", "8字节有符号整数，范围-9223372036854775808~9223372036854775807", long.class, Long.class),
    //AUTO_INCREMENT("AUTO_INCREMENT", null, "", "自增整数（需配合 INT/BIGINT 使用，如 INT AUTO_INCREMENT）", int.class, Integer.class, long.class, Long.class),

    // ========================= 小数类型 =========================
    DECIMAL("DECIMAL", "DECIMAL({0},{1})", "", "高精度定点数，{0}=总位数(1~38)，{1}=小数位数(0~{0})，适合财务数据", BigDecimal.class),
    NUMERIC("NUMERIC", "NUMERIC({0},{1})", "", "DECIMAL 的别名，高精度定点数", BigDecimal.class),
    FLOAT("FLOAT", null, "", "单精度浮点数，精度约6~7位有效数字（4字节）", float.class, Float.class),
    DOUBLE("DOUBLE", null, "", "双精度浮点数，精度约15~17位有效数字（8字节）", double.class, Double.class),
    REAL("REAL", null, "", "DOUBLE 的别名（H2 中 REAL 等价于 DOUBLE，与 PostgreSQL 不同）", double.class, Double.class),

    // ========================= 字符串类型 =========================
    VARCHAR("VARCHAR", "VARCHAR({0})", "", "可变长度字符串，{0}=最大长度(1~65535，默认255)，无空格填充", String.class),
    CHARACTER_VARYING("CHARACTER VARYING", "CHARACTER VARYING({0})", "", "无限长度文本（无长度限制），存储大文本数据", String.class),
    CHAR("CHAR", "CHAR({0})", "", "固定长度字符串，{0}=长度(1~255，默认1)，不足补空格", char.class, Character.class),
    CHARACTER("CHARACTER", "CHARACTER({0})", "", "和CHAR一样", char.class, Character.class),
    TEXT("TEXT", null, "", "无限长度文本（无长度限制），存储大文本数据", String.class),
    CLOB("CLOB", null, "", "字符大对象，等价于 TEXT，适合超大型文本", String.class),
    CHARACTER_LARGE_OBJECT("CHARACTER LARGE OBJECT", null, "", "字符大对象，等价于 TEXT，适合超大型文本", String.class),

    // ========================= 二进制类型 =========================
    BINARY("BINARY", "BINARY({0})", "", "固定长度二进制数据，{0}=长度(1~65535)", byte[].class),
    VARBINARY("VARBINARY", "VARBINARY({0})", "", "可变长度二进制数据，{0}=最大长度(1~65535)", byte[].class),
    BLOB("BLOB", null, "", "二进制大对象，无长度限制，存储图片、文件等二进制数据", byte[].class),

    // ========================= 日期时间类型 =========================
    TIMESTAMP("TIMESTAMP", null, "", "日期时间类型，格式'YYYY-MM-DD HH:MM:SS'，支持毫秒", Date.class, java.sql.Date.class, java.sql.Timestamp.class, Timestamp.class, LocalDateTime.class),
    DATETIME("DATETIME", null, "", "TIMESTAMP 的别名，日期时间类型", Date.class, LocalDateTime.class, Timestamp.class),
    TIMESTAMP_WITH_TIME_ZONE("TIMESTAMP WITH TIME ZONE", null, "", "日期时间类型，格式'YYYY-MM-DD HH:MM:SS'，支持毫秒", Date.class, Timestamp.class, LocalDateTime.class),
    DATE("DATE", null, "", "日期类型，格式'YYYY-MM-DD'，范围0001-01-01~9999-12-31", Date.class, LocalDate.class),
    TIME("TIME", null, "", "时间类型，格式'HH:MM:SS'，支持毫秒（如'12:34:56.789'）", Time.class, LocalTime.class),

    // ========================= 布尔类型 =========================
    BOOLEAN("BOOLEAN", null, "", "布尔类型，取值 TRUE/FALSE/NULL（原生支持，无需模拟）", boolean.class, Boolean.class),
    BOOL("BOOL", null, "", "BOOLEAN 的别名", boolean.class, Boolean.class),
    BIT("BIT", "", "", "", boolean.class, Boolean.class),

    // ========================= 特殊类型 =========================,
    //BIT_VARYING("BIT VARYING", "BIT VARYING({0})", "", "可变长度位序列，{0}=最大位数(1~64)", byte[].class),
    //VARBIT("VARBIT", "VARBIT({0})", "", "BIT VARYING 的别名", byte[].class),
    JSON("JSON", null, "1.4.190+", "JSON 类型，验证语法并优化存储（H2 1.4.190+ 原生支持）", String.class),
    UUID("UUID", null, "", "UUID 类型，自动生成/存储 UUID 格式字符串（如'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'）", String.class),
    GEOMETRY("GEOMETRY", null, "", "GEOMETRY'）", String.class);

    private final String fieldType;          // H2 字段类型名称（标准名）
    private final String fieldTypeTemplate;  // 字段类型模板（多参数填模板，单参数/无参数为null）
    private final String supportedVersions;  // 支持的 H2 版本（空表示全版本支持）
    private final String description;        // 类型简介（含与其他数据库差异说明）
    private final Class<?>[] javaTypes;      // 对应的 Java 类型（严格匹配指定范围）

    /**
     * 构造函数
     *
     * @param fieldType         H2 标准类型名
     * @param fieldTypeTemplate 类型模板（多参数如DECIMAL({0},{1})，否则为null）
     * @param supportedVersions 支持版本
     * @param description       描述
     * @param javaTypes         对应Java类型
     */
    H2SqlFieldType(String fieldType, String fieldTypeTemplate, String supportedVersions, String description, Class<?>... javaTypes) {
        this.fieldType = fieldType;
        this.fieldTypeTemplate = fieldTypeTemplate;
        this.supportedVersions = supportedVersions;
        this.description = description;
        this.javaTypes = javaTypes;
    }

    /**
     * 根据 H2 字段类型名（忽略大小写）获取枚举
     *
     * @param dataType 字段类型名（如 "varchar"、"bigint"）
     * @return 对应的枚举，无匹配则返回 null
     */
    public static H2SqlFieldType getFromDataType(String dataType) {
        if (StrUtil.isBlank(dataType)) {
            return null;
        }

        for (H2SqlFieldType value : H2SqlFieldType.values()) {
            if (StrUtil.equalsIgnoreCase(dataType, value.getFieldType())) {
                return value;
            }
        }

        // 处理 "INT AUTO_INCREMENT" 这类组合类型（提取核心类型）
        String coreType = dataType.trim().split("\\s+")[0];
        for (H2SqlFieldType value : H2SqlFieldType.values()) {
            if (StrUtil.equalsIgnoreCase(coreType, value.getFieldType())) {
                return value;
            }
        }


        return null;
    }

    /**
     * 根据 Java 类型匹配枚举（按枚举顺序优先匹配，结果缓存到 ObjectHolder）
     *
     * @param aClass Java 类型
     * @return 对应的枚举，无匹配则返回 null
     */
    public static H2SqlFieldType getByClass(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        String cacheKey = "H2SqlFieldType:" + aClass.getName();
        Object cached = ObjectHolder.INSTANCE.getObject(cacheKey);
        if (cached != null) {
            return (H2SqlFieldType) cached;
        }

        // 1. 精确匹配 Java 类型
        for (H2SqlFieldType value : H2SqlFieldType.values()) {
            Class<?>[] javaTypes = value.getJavaTypes();
            if (javaTypes == null) continue;
            for (Class<?> type : javaTypes) {
                if (type == aClass) {
                    ObjectHolder.INSTANCE.setObject(cacheKey, value);
                    return value;
                }
            }
        }

        // 2. 父类/接口匹配（处理继承关系）
        for (H2SqlFieldType value : H2SqlFieldType.values()) {
            Class<?>[] javaTypes = value.getJavaTypes();
            if (javaTypes == null) continue;
            for (Class<?> type : javaTypes) {
                Class<?> superClass = aClass;
                while (superClass != null && superClass != Object.class) {
                    if (type.isAssignableFrom(superClass)) {
                        ObjectHolder.INSTANCE.setObject(cacheKey, value);
                        return value;
                    }
                    superClass = superClass.getSuperclass();
                }
            }
        }

        return null;
    }
}