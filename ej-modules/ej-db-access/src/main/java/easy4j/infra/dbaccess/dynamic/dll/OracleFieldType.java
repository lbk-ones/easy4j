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
 * Oracle 字段类型枚举，包含版本支持、简介及对应Java类型
 * 枚举的顺序很重要，第一个类型代表同类默认值
 *
 * @author bokun.li
 * @date 2025/8/20
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OracleFieldType {
    // 整数类型（Oracle 无原生整数类型，统一用 NUMBER 模拟，通过精度区分）
    NUMBER_INT("NUMBER(10)", "", "", "10位整数，范围-2147483648~2147483647（对应 Java int/Integer）", int.class, Integer.class),
    NUMBER_SHORT("NUMBER(5)", "", "", "5位整数，范围-32768~32767（对应 Java short/Short）", short.class, Short.class),
    NUMBER_BYTE("NUMBER(3)", "", "", "3位整数，范围-128~127（对应 Java byte/Byte）", byte.class, Byte.class),
    NUMBER_LONG("NUMBER(19)", "", "", "19位整数，范围-9223372036854775808~9223372036854775807（对应 Java long/Long）", long.class, Long.class),
    INTEGER("INTEGER", "", "", "Oracle 整数别名，等价于 NUMBER(38)（兼容语法，建议用 NUMBER 指定精度）", int.class, Integer.class, long.class, Long.class),
    INT("INT", "", "", "INTEGER 的别名，等价于 NUMBER(38)（兼容语法）", int.class, Integer.class),
    SMALLINT("SMALLINT", "", "", "Oracle 小整数别名，等价于 NUMBER(38)（兼容语法，建议用 NUMBER(5) 精准匹配 short）", short.class, Short.class),
    //BIGINT("BIGINT", "", "12c+", "Oracle 12c+ 新增大整数别名，等价于 NUMBER(38)（建议用 NUMBER(19) 精准匹配 long）", long.class, Long.class),
    IDENTITY_INT("NUMBER(10) GENERATED ALWAYS AS IDENTITY", "", "12c+", "12c+ 自增10位整数（对应 Java int/Integer，替代序列+触发器）", int.class, Integer.class),
    IDENTITY_LONG("NUMBER(19) GENERATED ALWAYS AS IDENTITY", "", "12c+", "12c+ 自增19位整数（对应 Java long/Long）", long.class, Long.class),

    // 小数类型（高精度/浮点型）
    NUMBER_DECIMAL("NUMBER", "NUMBER({0},{1})", "", "高精度定点数，M为总位数（1~38），D为小数位数（0~M）（对应 Java BigDecimal，适合财务数据）", BigDecimal.class),
    DECIMAL("DECIMAL", "DECIMAL({0},{1})", "", "NUMBER 的别名，高精度定点数（兼容语法）", BigDecimal.class),
    NUMERIC("NUMERIC", "NUMERIC({0},{1})", "", "NUMBER 的别名，高精度定点数（兼容语法）", BigDecimal.class),
    FLOAT("FLOAT", "", "", "4字节单精度浮点数，精度约6~7位有效数字（对应 Java float/Float）", float.class, Float.class),
    DOUBLE_PRECISION("DOUBLE PRECISION", "", "", "4字节单精度浮点数，精度约6~7位有效数字（对应 Java float/Float）", double.class, Double.class),
    BINARY_FLOAT("BINARY_FLOAT", "", "", "4字节单精度浮点数，精度约6~7位有效数字（对应 Java float/Float）", float.class, Float.class),
    BINARY_DOUBLE("BINARY_DOUBLE", "", "", "8字节双精度浮点数，精度约15~17位有效数字（对应 Java double/Double）", double.class, Double.class),

    // 字符串类型
    VARCHAR2("VARCHAR2", "VARCHAR2({0})", "", "可变长度字符串，M为最大长度（1~4000，12c+ 扩展到32767），无空格填充（对应 Java String/char/Character）", String.class, char.class, Character.class),
    CHAR("CHAR", "CHAR({0})", "", "固定长度字符串，M为长度（1~2000，默认1），不足补空格（对应 Java String/char/Character）", String.class, char.class, Character.class),
    CLOB("CLOB", "", "", "大文本类型，存储超过4000字符的文本（对应 Java String，替代过时的 LONG 类型）", String.class),
    LONG("LONG", "", "", "过时大文本类型（Oracle 不推荐，建议用 CLOB 替代），对应 Java String", String.class),

    // 二进制类型
    BLOB("BLOB", "", "", "二进制大对象，存储图片、文件等二进制数据（对应 Java byte[]）", byte[].class),
    RAW("RAW", "RAW({0})", "", "固定长度二进制数据，M为长度（1~2000）（对应 Java byte[]，适合短二进制）", byte[].class),
    LONG_RAW("LONG RAW", "", "", "过时二进制类型（Oracle 不推荐，建议用 BLOB 替代），对应 Java byte[]", byte[].class),

    // 日期时间类型
    TIMESTAMP("TIMESTAMP", "", "", "带小数秒的日期时间，{0}为小数位数（0~9，默认6），精度到毫秒/微秒（对应 Java Date/LocalDateTime）", Date.class, java.sql.Timestamp.class, LocalDateTime.class),
    DATE("DATE", "", "", "日期时间类型，格式'YYYY-MM-DD HH24:MI:SS'，精度到秒（对应 Java Date/ LocalDate/LocalTime/LocalDateTime）", Date.class, java.sql.Date.class, LocalDate.class, LocalTime.class, LocalDateTime.class),
    TIMESTAMP_TZ("TIMESTAMP WITH TIME ZONE", "", "", "带时区的TIMESTAMP，自动处理时区转换（对应 Java Date/LocalDateTime）", Date.class, java.sql.Timestamp.class, LocalDateTime.class),
    TIMESTAMP_LTZ("TIMESTAMP WITH LOCAL TIME ZONE", "", "", "带本地时区的TIMESTAMP，存储为数据库时区（对应 Java Date/LocalDateTime）", Date.class, java.sql.Timestamp.class, LocalDateTime.class),

    // 布尔类型（Oracle 无原生布尔，用 NUMBER(1) 模拟）
    NUMBER_BOOLEAN("NUMBER(1)", "", "", "模拟布尔类型，用 0=FALSE、1=TRUE 表示（对应 Java boolean/Boolean）", boolean.class, Boolean.class);
    //BOOLEAN("BOOLEAN", "BOOLEAN", "PL/SQL", "仅 PL/SQL 支持的布尔类型，SQL 层需用 NUMBER(1) 模拟（对应 Java boolean/Boolean）", boolean.class, Boolean.class),

    // 特殊类型
    //JSON("JSON", "JSON", "12c+", "12c+ 原生JSON类型，验证JSON语法，优化存储（对应 Java String）", String.class),
    //JSONB("JSONB", "JSONB", "21c+", "21c+ 二进制JSON类型，支持索引和高效查询（对应 Java String，Oracle 推荐JSON类型）", String.class),
    //UUID("UUID", "UUID", "12c+", "12c+ 原生UUID类型，格式'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'（对应 Java String）", String.class),
    //INTERVAL_YM("INTERVAL YEAR({0}) TO MONTH", "INTERVAL YEAR({0}) TO MONTH", "", "年月间隔类型，{0}为年份精度（1~9）（对应 Java String）", String.class),
    //INTERVAL_DS("INTERVAL DAY({0}) TO SECOND({1})", "INTERVAL DAY({0}) TO SECOND({1})", "", "日秒间隔类型，{0}为天数精度（1~9），{1}为小数秒精度（0~9）（对应 Java String）", String.class);

    private final String fieldType;          // Oracle 字段类型名称（标准名）
    private final String fieldTypeTemplate;  // 字段类型模板（带参数占位符，如 VARCHAR2({0})）
    private final String supportedVersions;  // 支持的 Oracle 版本（空表示全版本支持）
    private final String description;        // 类型简介（含Java类型对应说明）
    private final Class<?>[] javaTypes;      // 对应的 Java 类型（含基础类型+包装类）

    OracleFieldType(String fieldType, String fieldTypeTemplate, String supportedVersions, String description, Class<?>... javaTypes) {
        this.fieldType = fieldType;
        this.fieldTypeTemplate = fieldTypeTemplate;
        this.supportedVersions = supportedVersions;
        this.description = description;
        this.javaTypes = javaTypes;
    }

    /**
     * 根据 Oracle 字段类型名（忽略大小写）获取枚举
     *
     * @param dataType 字段类型名（如 "varchar2"、"number(10)"）
     * @return 对应的枚举，无匹配则返回 null
     */
    public static OracleFieldType getFromDataType(String dataType) {
        if (StrUtil.isBlank(dataType)) {
            return null;
        }
        // 处理带精度的类型（如 "number(10)" 截取为 "number" 匹配）
        String cleanType = dataType.trim().toUpperCase().split("\\(")[0];
        for (OracleFieldType value : OracleFieldType.values()) {
            String valueType = value.getFieldType().toUpperCase().split("\\(")[0];
            if (StrUtil.equals(cleanType, valueType)) {
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
    public static OracleFieldType getByClass(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        String cacheKey = "OracleFieldType:" + aClass.getName();
        Object cached = ObjectHolder.INSTANCE.getObject(cacheKey);
        if (cached != null) {
            return (OracleFieldType) cached;
        }

        // 优先匹配直接对应类型
        for (OracleFieldType value : OracleFieldType.values()) {
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

        // 匹配父类/接口（如 java.sql.Date 继承自 Date）
        for (OracleFieldType value : OracleFieldType.values()) {
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