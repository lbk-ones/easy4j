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

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DB2FieldType {
    // 整数类型
    SMALLINT("SMALLINT", "", "", "16位整数，范围-32768~32767（对应 Java short/Short）", short.class, Short.class),
    INTEGER("INTEGER", "", "", "32位整数，范围-2147483648~2147483647（对应 Java int/Integer）", int.class, Integer.class),
    INT("INT", "", "", "INTEGER 的别名（对应 Java int/Integer）", int.class, Integer.class),
    BIGINT("BIGINT", "", "", "64位整数，范围-9223372036854775808~9223372036854775807（对应 Java long/Long）", long.class, Long.class),

    // 小数类型（定点数）
    DECIMAL("DECIMAL", "DECIMAL({0},{1})", "", "高精度定点数，M为总位数（1~31），D为小数位数（0~M）（对应 Java BigDecimal，适合财务数据）", BigDecimal.class),
    DEC("DEC", "DEC({0},{1})", "", "DECIMAL 的别名（对应 Java BigDecimal）", BigDecimal.class),
    NUMERIC("NUMERIC", "NUMERIC({0},{1})", "", "DECIMAL 的别名（对应 Java BigDecimal）", BigDecimal.class),

    // 小数类型（浮点数）
    FLOAT("FLOAT", "", "", "8字节双精度浮点数，精度约15~17位有效数字（对应 Java double/Double）", double.class, Double.class),
    REAL("REAL", "", "", "4字节单精度浮点数，精度约6~7位有效数字（对应 Java float/Float）", float.class, Float.class),
    DOUBLE("DOUBLE", "", "", "8字节双精度浮点数，精度约15~17位有效数字（对应 Java double/Double）", double.class, Double.class),
    DOUBLE_PRECISION("DOUBLE PRECISION", "", "", "DOUBLE 的别名，8字节双精度浮点数（对应 Java double/Double）", double.class, Double.class),

    // 小数类型（十进制浮点）
    DECFLOAT("DECFLOAT", "DECFLOAT({0})", "9.5+", "十进制浮点数类型，支持高精度科学计算，{0}为精度（16或34）（对应 Java BigDecimal）", BigDecimal.class),

    // 字符串类型
    VARCHAR("VARCHAR", "VARCHAR({0})", "", "可变长度字符串，M为最大长度（1~32672），无空格填充（对应 Java String/char/Character）", String.class, char.class, Character.class),
    CHAR("CHAR", "CHAR({0})", "", "固定长度字符串，M为长度（1~254），不足补空格（对应 Java String/char/Character）", String.class, char.class, Character.class),
    CLOB("CLOB", "", "", "大文本类型，存储超过32672字符的文本，单个最大2GB（对应 Java String）", String.class),

    // 二进制类型
    BLOB("BLOB", "", "", "二进制大对象，存储图片、文件等二进制数据，单个最大2GB（对应 Java byte[]）", byte[].class),
    BINARY("BINARY", "BINARY({0})", "", "固定长度二进制数据，M为长度（1~254）（对应 Java byte[]，适合短二进制）", byte[].class),
    VARBINARY("VARBINARY", "VARBINARY({0})", "", "可变长度二进制数据，M为最大长度（1~32672）（对应 Java byte[]）", byte[].class),

    // 日期时间类型
    TIMESTAMP("TIMESTAMP", "", "", "日期时间类型，格式'YYYY-MM-DD HH:MM:SS.NNNNNN'，精度到微秒（对应 Java Date/LocalDateTime）", Date.class, java.sql.Timestamp.class, LocalDateTime.class),
    DATE("DATE", "", "", "日期类型，格式'YYYY-MM-DD'，精度到天（对应 Java Date/LocalDate）", Date.class, java.sql.Date.class, LocalDate.class),
    TIME("TIME", "", "", "时间类型，格式'HH:MM:SS'（对应 Java LocalTime）", LocalTime.class),

    // 布尔类型（DB2 无原生布尔，通常用 SMALLINT 模拟）
    SMALLINT_BOOLEAN("SMALLINT", "", "", "模拟布尔类型，用 0=FALSE、1=TRUE 表示（对应 Java boolean/Boolean）", boolean.class,byte.class, Boolean.class),

    // 特殊类型
    XML("XML", "", "9.1+", "XML 类型，存储 XML 数据，支持 XPath 查询（对应 Java String）", String.class),
    GRAPHIC("GRAPHIC", "GRAPHIC({0})", "", "双字节字符类型（DBCS），M为长度（1~127）（对应 Java String）", String.class),
    VARGRAPHIC("VARGRAPHIC", "VARGRAPHIC({0})", "", "可变长度双字节字符类型（DBCS），M为最大长度（1~16336）（对应 Java String）", String.class),
    DBCLOB("DBCLOB", "", "", "大型双字节字符对象（DBCS），单个最大1GB（对应 Java String）", String.class);

    // 注意：以下类型 DB2 不支持
    // JSON - DB2 官方不提供原生 JSON 类型支持，可用 VARCHAR/CLOB 存储 JSON 字符串
    // BOOLEAN - DB2 不支持布尔类型，使用 SMALLINT(0/1) 模拟

    private final String fieldType;          // DB2 字段类型名称（标准名）
    private final String fieldTypeTemplate;  // 字段类型模板（带参数占位符，如 VARCHAR({0})）
    private final String supportedVersions;  // 支持的 DB2 版本（空表示全版本支持）
    private final String description;        // 类型简介（含Java类型对应说明）
    private final Class<?>[] javaTypes;      // 对应的 Java 类型（含基础类型+包装类）

    DB2FieldType(String fieldType, String fieldTypeTemplate, String supportedVersions, String description, Class<?>... javaTypes) {
        this.fieldType = fieldType;
        this.fieldTypeTemplate = fieldTypeTemplate;
        this.supportedVersions = supportedVersions;
        this.description = description;
        this.javaTypes = javaTypes;
    }

    /**
     * 根据 DB2 字段类型名（忽略大小写）获取枚举
     *
     * @param dataType 字段类型名（如 "varchar"、"decimal(10,2)"）
     * @return 对应的枚举，无匹配则返回 null
     */
    public static DB2FieldType getFromDataType(String dataType) {
        if (StrUtil.isBlank(dataType)) {
            return null;
        }
        // 处理带精度的类型（如 "decimal(10,2)" 截取为 "decimal" 匹配）
        String cleanType = dataType.trim().toUpperCase().split("\\(")[0];
        for (DB2FieldType value : DB2FieldType.values()) {
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
    public static DB2FieldType getByClass(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        String cacheKey = "DB2FieldType:" + aClass.getName();
        Object cached = ObjectHolder.INSTANCE.getObject(cacheKey);
        if (cached != null) {
            return (DB2FieldType) cached;
        }

        // 优先匹配直接对应类型
        for (DB2FieldType value : DB2FieldType.values()) {
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
        for (DB2FieldType value : DB2FieldType.values()) {
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