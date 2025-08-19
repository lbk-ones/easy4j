package easy4j.infra.dbaccess.dynamic.dll;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import easy4j.infra.common.utils.ObjectHolder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

/**
 * PostgreSQL 字段类型枚举，包含版本支持、简介及对应Java类型
 * 枚举的顺序很重要，第一个类型代表同类默认值
 *
 * @author bokun.li
 * @date 2025/8/19
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PgSQLFieldType {
    // 整数类型（PostgreSQL 无 unsigned，需通过 CHECK 约束实现）
    INT("INT", "", "", "INT4 的别名，4字节有符号整数", int.class, Integer.class),
    INT4("INT4", "", "", "4字节有符号整数，范围-2147483648~2147483647（同 MySQL INT）", int.class, Integer.class),
    INT2("INT2", "", "", "2字节有符号整数，范围-32768~32767（同 MySQL SMALLINT）", short.class, Short.class),
    BIGINT("BIGINT", "", "", "INT8 的别名，8字节有符号整数", Long.class, long.class),
    INT8("INT8", "", "", "8字节有符号整数，范围-9223372036854775808~9223372036854775807（同 MySQL BIGINT）", long.class, Long.class),
    SMALLINT("SMALLINT", "", "", "INT2 的别名，2字节有符号整数", short.class, Short.class, byte.class, Byte.class),
    SERIAL("SERIAL", "", "", "自增4字节整数（隐式关联序列），范围1~2147483647", int.class, Integer.class),
    BIGSERIAL("BIGSERIAL", "", "", "自增8字节整数（隐式关联序列），范围1~9223372036854775807", long.class, Long.class),
    SMALLSERIAL("SMALLSERIAL", "", "", "自增2字节整数（隐式关联序列），范围1~32767", short.class, Short.class),

    // 小数类型
    DECIMAL("DECIMAL", "DECIMAL({0},{1})", "", "NUMERIC 的别名，高精度定点数", BigDecimal.class),
    NUMERIC("NUMERIC", "NUMERIC({0},{1})", "", "高精度定点数，M为总位数（1~1000），D为小数位数（0~M），适合财务数据（同 MySQL DECIMAL）", BigDecimal.class),
    REAL("REAL", "", "", "4字节单精度浮点数，精度约6~7位有效数字（同 MySQL FLOAT）", float.class, Float.class),
    DOUBLE_PRECISION("DOUBLE PRECISION", "", "", "8字节双精度浮点数，精度约15~17位有效数字（同 MySQL DOUBLE）", double.class, Double.class),

    // 字符串类型
    VARCHAR("VARCHAR", "VARCHAR({0})", "", "可变长度字符串，M为最大长度（1~65535，默认无限长），无空格填充（同 MySQL VARCHAR）", String.class),
    CHAR("CHAR", "CHAR({0})", "", "固定长度字符串，M为长度（1~255，默认1），不足补空格（同 MySQL CHAR）", String.class, char.class, Character.class),
    TEXT("TEXT", "", "", "无限长度文本（无M限制），存储大文本数据（覆盖 MySQL TINYTEXT~LONGTEXT 功能）", String.class),
    BPCHAR("BPCHAR", "BPCHAR({0})", "", "CHAR 的内部别名（Blank-Padded Char），固定长度字符串", String.class),

    // 二进制类型
    BYTEA("BYTEA", "", "", "存储二进制数据（覆盖 MySQL BLOB 系列类型功能），无显式长度限制（受表空间限制）", byte[].class),

    // 日期时间类型
    TIMESTAMP("TIMESTAMP", "", "", "日期时间类型，格式'YYYY-MM-DD HH:MM:SS'，范围4713 BC~294276 AD（无时区）", Date.class, java.sql.Date.class, java.sql.Timestamp.class, LocalDateTime.class),
    TIMESTAMPTZ("TIMESTAMP WITH TIME ZONE", "", "", "带时区的日期时间类型，自动转换为数据库时区存储（PostgreSQL 特色）", Date.class, java.sql.Timestamp.class, LocalDateTime.class),
    DATE("DATE", "", "", "日期类型，格式'YYYY-MM-DD'，范围4713 BC~294276 AD（同 MySQL DATE）", java.sql.Date.class, LocalDate.class),
    TIME("TIME", "", "", "时间类型，格式'HH:MM:SS'，范围00:00:00~23:59:59.999999（无时区）", java.sql.Time.class, LocalTime.class),
    TIMETZ("TIME WITH TIME ZONE", "", "", "带时区的时间类型（PostgreSQL 特色）", java.sql.Time.class, LocalTime.class),
    INTERVAL("INTERVAL", "", "", "时间间隔类型，存储时间段（如'1 day 2 hours'），PostgreSQL 特色类型", String.class),

    // 布尔类型
    BOOLEAN("BOOLEAN", "", "", "布尔类型，取值 TRUE/FALSE/NULL（MySQL 需用 TINYINT 模拟，PostgreSQL 原生支持）", boolean.class, Boolean.class),
    BOOL("BOOL", "", "", "BOOLEAN 的别名，布尔类型", boolean.class, Boolean.class),

    // 特殊类型
    BIT("BIT", "BIT({0})", "", "固定长度位序列，M为位数（1~8388607），不足补0（同 MySQL BIT）", byte[].class, Boolean.class),
    BIT_VARYING("BIT VARYING", "BIT VARYING({0})", "", "可变长度位序列，M为最大位数（1~8388607）", byte[].class),
    VARBIT("VARBIT", "VARBIT({0})", "", "BIT VARYING 的别名，可变长度位序列", byte[].class),

    //ENUM("ENUM", "ENUM({0})", "9.1+", "枚举类型，需预定义值（如 ENUM('男','女')），值不可修改（MySQL ENUM 可插入未定义值）", String.class),
    JSON("JSON", "", "9.2+", "存储JSON格式数据，仅验证语法，不优化查询", String.class),
    JSONB("JSONB", "", "9.4+", "二进制JSON类型，验证语法+优化存储/查询（支持索引，PostgreSQL 推荐JSON类型）", String.class),
    UUID("UUID", "", "8.3+", "存储UUID（通用唯一识别码），格式'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'，自动校验格式", String.class),
    INET("INET", "", "", "存储IPv4/IPv6地址（如'192.168.1.1'），支持网络相关函数（PostgreSQL 特色）", String.class),
    CIDR("CIDR", "", "", "存储无类域间路由地址（如'192.168.1.0/24'），PostgreSQL 特色", String.class);

    private final String fieldType;          // PostgreSQL 字段类型名称（标准名）
    private final String fieldTypeTemplate;  // 字段类型模板（带参数占位符，如 VARCHAR({0})）
    private final String supportedVersions;  // 支持的 PostgreSQL 版本（空表示全版本支持）
    private final String description;        // 类型简介（含与 MySQL 差异说明）
    private final Class<?>[] javaTypes;      // 对应的 Java 类型（可能多个）

    PgSQLFieldType(String fieldType, String fieldTypeTemplate, String supportedVersions, String description, Class<?>... javaTypes) {
        this.fieldType = fieldType;
        this.fieldTypeTemplate = fieldTypeTemplate;
        this.supportedVersions = supportedVersions;
        this.description = description;
        this.javaTypes = javaTypes;
    }

    /**
     * 根据 PostgreSQL 字段类型名（忽略大小写）获取枚举
     *
     * @param dataType 字段类型名（如 "varchar"、"int8"）
     * @return 对应的枚举，无匹配则返回 null
     */
    public static PgSQLFieldType getFromDataType(String dataType) {
        if (StrUtil.isBlank(dataType)) {
            return null;
        }
        for (PgSQLFieldType value : PgSQLFieldType.values()) {
            if (StrUtil.equalsIgnoreCase(dataType, value.getFieldType())) {
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
    public static PgSQLFieldType getByClass(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        String cacheKey = "PostgreSQLFieldType:" + aClass.getName();
        Object cached = ObjectHolder.INSTANCE.getObject(cacheKey);
        if (cached != null) {
            return (PgSQLFieldType) cached;
        }
        for (PgSQLFieldType value : PgSQLFieldType.values()) {
            Class<?>[] javaTypes = value.getJavaTypes();
            if (javaTypes == null) {
                continue;
            }
            for (Class<?> type : javaTypes) {
                if (type == aClass) {
                    ObjectHolder.INSTANCE.setObject(cacheKey, value);
                    return value;
                }
            }
        }
        for (PgSQLFieldType value : PgSQLFieldType.values()) {
            Class<?>[] javaTypes = value.getJavaTypes();
            if (javaTypes == null) {
                continue;
            }
            for (Class<?> type : javaTypes) {
                Class<?> superClass = aClass;
                while (superClass != null) {
                    if (superClass == Object.class) {
                        break;
                    }
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