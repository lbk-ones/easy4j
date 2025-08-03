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
 * MySQL 字段类型枚举，包含版本支持、简介及对应Java类型
 * M是位数，D是精度
 * 枚举的顺序很重要，第一个类型代表默认值
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MySQLFieldType {
    // 整数类型
    TINYINT("TINYINT", "", "", "1字节有符号整数，范围-128~127；无符号0~255", byte.class, Byte.class),
    SMALLINT("SMALLINT", "", "", "2字节有符号整数，范围-32768~32767；无符号0~65535", short.class, Short.class),
    INT("INT", "", "", "4字节有符号整数，范围-2147483648~2147483647；无符号0~4294967295", int.class, Integer.class),
    MEDIUMINT("MEDIUMINT", "", "", "3字节有符号整数，范围-8388608~8388607；无符号0~16777215", int.class, Integer.class),
    BIGINT("BIGINT", "", "", "8字节有符号整数，范围-9223372036854775808~9223372036854775807；无符号0~18446744073709551615", long.class, Long.class),

    // 小数类型
    FLOAT("FLOAT", "", "", "4字节单精度浮点数，精度约7位小数", float.class, Float.class),
    DOUBLE("DOUBLE", "", "", "8字节双精度浮点数，精度约15位小数", double.class, Double.class),
    DECIMAL("DECIMAL", "DECIMAL({0},{1})", "", "高精度定点数，M为总位数，D为小数位数，适合财务数据", BigDecimal.class),

    // 字符串类型
    VARCHAR("VARCHAR", "VARCHAR({0})", "", "可变长度字符串，MySQL 5.0.3前M最大255，之后最大65535", String.class),
    CHAR("CHAR", "CHAR({0})", "", "固定长度字符串，M范围0~255，不足补空格", String.class, char.class, Character.class),
    TINYTEXT("TINYTEXT", "", "", "小型文本，最大长度255字节", String.class),
    TEXT("TEXT", "", "", "常规文本，最大长度65535字节", String.class),
    MEDIUMTEXT("MEDIUMTEXT", "", "", "中型文本，最大长度16777215字节", String.class),
    LONGTEXT("LONGTEXT", "", "", "大型文本，最大长度4294967295字节", String.class),

    // 二进制类型
    BLOB("BLOB", "", "", "二进制对象，最大65535字节", byte[].class),
    BINARY("BINARY", "BINARY({0})", "", "固定长度固定的二进制数据，M范围0~255", byte[].class),
    VARBINARY("VARBINARY", "VARBINARY({0})", "", "长度可变的二进制数据，最大65535字节", byte[].class),
    TINYBLOB("TINYBLOB", "", "", "小型二进制对象，最大255字节", byte[].class),
    MEDIUMBLOB("MEDIUMBLOB", "", "", "中型二进制对象，最大16777215字节", byte[].class),
    LONGBLOB("LONGBLOB", "", "", "大型二进制对象，最大4294967295字节", byte[].class),

    // 日期时间类型
    DATETIME("DATETIME", "", "", "日期时间类型，格式'YYYY-MM-DD HH:MM:SS'，范围1000-01-01 00:00:00~9999-12-31 23:59:59", java.sql.Date.class, Date.class, java.sql.Timestamp.class, LocalDateTime.class),
    DATE("DATE", "", "", "日期类型，格式'YYYY-MM-DD'，范围1000-01-01~9999-12-31", java.sql.Date.class, LocalDate.class),
    TIME("TIME", "", "", "时间类型，格式'HH:MM:SS'，范围-838:59:59~838:59:59", java.sql.Time.class, LocalTime.class),
    TIMESTAMP("TIMESTAMP", "", "", "时间戳，格式同DATETIME，范围1970-01-01 00:00:01 UTC~2038-01-19 03:14:07 UTC，受时区影响", java.sql.Timestamp.class),
    YEAR("YEAR", "", "", "年份类型，格式YYYY，范围1901~2155（MySQL 5+支持4位年份）", int.class, Integer.class),

    // 特殊类型
    BIT("BIT", "BIT({0})", "5.0.3", "位字段类型，M范围1~64，存储位序列", byte[].class, Boolean.class),
    ENUM("ENUM", "ENUM({0})", "", "枚举类型，只能取预定义值之一,ENUM('val1','val2',...)", String.class),
    SET("SET", "SET({0})", "", "集合类型，可选取多个预定义值（最多64个）SET('val1','val2',...)", String.class),
    JSON("JSON", "", "5.7.8", "存储JSON格式数据，支持验证和索引", String.class);

    // getter方法
    private final String fieldType;      // MySQL字段类型名称
    private final String fieldTypeTemplate;      // MySQL字段类型名称
    private final String supportedVersions;  // 支持的MySQL版本
    private final String description;    // 类型简介
    private final Class<?>[] javaTypes;  // 对应的Java类型（可能有多个）

    MySQLFieldType(String fieldType, String fieldTypeTemplate, String supportedVersions, String description, Class<?>... javaTypes) {
        this.fieldType = fieldType;
        this.fieldTypeTemplate = fieldTypeTemplate;
        this.supportedVersions = supportedVersions;
        this.description = description;
        this.javaTypes = javaTypes;
    }

    public static MySQLFieldType getFromDataType(String dataType) {
        MySQLFieldType[] values = MySQLFieldType.values();
        for (MySQLFieldType value : values) {
            String fieldType1 = value.getFieldType();
            if (StrUtil.equalsIgnoreCase(dataType, fieldType1)) {
                return value;
            }
        }
        return null;
    }

    // Match types in the order of enumeration values
    public static MySQLFieldType getByClass(Class<?> aclass) {
        Object object = ObjectHolder.INSTANCE.getObject("MySQLFieldType:" + aclass.getName());
        if (object != null) return (MySQLFieldType) object;
        MySQLFieldType[] values = MySQLFieldType.values();
        for (MySQLFieldType value : values) {
            Class<?>[] javaTypes1 = value.getJavaTypes();
            if (javaTypes1 == null) {
                continue;
            }
            for (Class<?> aClass : javaTypes1) {
                if (aClass.isAssignableFrom(aclass)) {
                    ObjectHolder.INSTANCE.setObject("MySQLFieldType:" + aclass.getName(), value);
                    return value;
                }
            }
        }
        for (MySQLFieldType value : values) {
            Class<?>[] javaTypes1 = value.getJavaTypes();
            if (javaTypes1 == null) {
                continue;
            }
            for (Class<?> aClass : javaTypes1) {
                Class<?> fClass = aClass;
                while (fClass != null) {
                    if (fClass.getName().equals(Object.class.getName())) {
                        break;
                    }
                    if (fClass.isAssignableFrom(aclass)) {
                        ObjectHolder.INSTANCE.setObject("MySQLFieldType:" + aclass.getName(), value);
                        return value;
                    }
                    fClass = fClass.getSuperclass();
                }
            }
        }
        return null;
    }
}