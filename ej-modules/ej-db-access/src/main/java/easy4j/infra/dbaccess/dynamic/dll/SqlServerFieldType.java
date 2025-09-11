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
 * SQL Server 2022 数据库字段类型枚举，包含版本支持、简介及对应Java类型
 * 枚举的顺序很重要，第一个类型代表同类默认值
 *
 * @author generated
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SqlServerFieldType {
    // ========================= 整数类型 =========================
    INT("INT", null, "", "4字节有符号整数，范围-2147483648~2147483647", int.class, Integer.class),
    SMALLINT("SMALLINT", null, "", "2字节有符号整数，范围-32768~32767", short.class, Short.class),
    TINYINT("TINYINT", null, "", "1字节无符号整数，范围0~255", byte.class, Byte.class),
    BIGINT("BIGINT", null, "", "8字节有符号整数，范围-9223372036854775808~9223372036854775807", long.class, Long.class),
    BIT("BIT", null, "", "存储0、1或NULL，适合表示布尔值，SQL Server 2022支持批量操作优化", boolean.class, Boolean.class),

    // ========================= 小数类型 =========================
    DECIMAL("DECIMAL", "DECIMAL({0},{1})", "", "高精度定点数，{0}=总位数(1~38)，{1}=小数位数(0~{0})，适合财务数据", BigDecimal.class),
    NUMERIC("NUMERIC", "NUMERIC({0},{1})", "", "DECIMAL 的同义词，高精度定点数", BigDecimal.class),
    FLOAT("FLOAT", "FLOAT({0})", "", "浮点数，{0}=精度(1~53)，1~24对应单精度，25~53对应双精度", float.class, Float.class, double.class, Double.class),
    REAL("REAL", null, "", "单精度浮点数，精度约6~7位有效数字", float.class, Float.class),
    MONEY("MONEY", null, "", "货币类型，范围-922337203685477.5808~922337203685477.5807，精度4位小数", BigDecimal.class),
    SMALLMONEY("SMALLMONEY", null, "", "小型货币类型，范围-214748.3648~214748.3647，精度4位小数", BigDecimal.class),
    // ========================= 字符串类型 =========================
    // n最大4000
    NVARCHAR("NVARCHAR", "NVARCHAR({0})", "", "支持Unicode的可变长度字符串，{0}=最大长度(1~4000)", String.class),
    // n最大8000
    VARCHAR("VARCHAR", "VARCHAR({0})", "", "可变长度字符串，{0}=最大长度(1~8000)，超过需用VARCHAR(MAX)", String.class),
    VARCHAR_MAX("VARCHAR(MAX)", null, "2005+", "可变长度字符串，最大存储2^31-1字节，用于存储大文本", String.class),
    CHAR("CHAR", "CHAR({0})", "", "固定长度字符串，{0}=长度(1~8000)，不足补空格", char.class, Character.class),
    NVARCHAR_MAX("NVARCHAR(MAX)", null, "2005+", "支持Unicode的可变长度字符串，用于存储大文本", String.class),
    NCHAR("NCHAR", "NCHAR({0})", "", "支持Unicode的固定长度字符串，{0}=长度(1~4000)", char.class, Character.class),
    @Deprecated
    TEXT("TEXT", null, "", "用于存储大文本数据，建议使用VARCHAR(MAX)替代", String.class),
    @Deprecated
    NTEXT("NTEXT", null, "", "支持Unicode的大文本类型，建议使用NVARCHAR(MAX)替代", String.class),
    SYSNAME("SYSNAME", null, "", "系统名称类型，等价于NVARCHAR(128)，用于存储对象名称", String.class),

    // ========================= 二进制类型 =========================
    BINARY("BINARY", "BINARY({0})", "", "固定长度二进制数据，{0}=长度(1~8000)", byte[].class),
    VARBINARY("VARBINARY", "VARBINARY({0})", "", "可变长度二进制数据，{0}=最大长度(1~8000)", byte[].class),
    VARBINARY_MAX("VARBINARY(MAX)", null, "2005+", "可变长度二进制数据，最大存储2^31-1字节，用于存储大型二进制对象", byte[].class),
    @Deprecated
    IMAGE("IMAGE", null, "", "用于存储大型二进制对象，建议使用VARBINARY(MAX)替代", byte[].class),
    //FILESTREAM("FILESTREAM", null, "2008+", "用于存储大型二进制数据到文件系统，支持事务和大型文件", byte[].class),
    //FILETABLE("FILETABLE", null, "2012+", "特殊表类型，用于存储文件和目录，支持Windows API访问", String.class),

    // ========================= 日期时间类型 默认datetime2 =========================
    DATETIME2("DATETIME2", "DATETIME2({0})", "", "增强的日期时间类型，{0}=小数秒精度(0~7)，范围0001-01-01~9999-12-31", Date.class, LocalDateTime.class, Timestamp.class),
    DATETIME("DATETIME", null, "", "日期时间类型，范围1753-01-01~9999-12-31，精度3.33毫秒", Date.class, java.sql.Date.class, Timestamp.class),
    SMALLDATETIME("SMALLDATETIME", null, "", "日期时间类型，范围1900-01-01~2079-06-06，精度1分钟", Date.class, java.sql.Date.class, Timestamp.class),
    DATE("DATE", null, "2008+", "仅日期类型，格式'YYYY-MM-DD'，范围0001-01-01~9999-12-31", Date.class, java.sql.Date.class, LocalDate.class),
    TIME("TIME", "TIME({0})", "2008+", "仅时间类型，{0}=小数秒精度(0~7)", Time.class, LocalTime.class),
    DATETIMEOFFSET("DATETIMEOFFSET", "DATETIMEOFFSET({0})", "2008+", "带时区的日期时间类型，{0}=小数秒精度(0~7)", Timestamp.class, LocalDateTime.class),
    //DATETIME2("DATETIME2_UTC", "DATETIME2_UTC({0})", "2022+", "SQL Server 2022新增，带UTC时区的日期时间类型，自动转换为UTC存储", LocalDateTime.class, Timestamp.class),

    // ========================= 特殊类型 =========================
    UNIQUEIDENTIFIER("UNIQUEIDENTIFIER", null, "", "存储UUID，128位唯一标识符，支持NEWID()和NEWSEQUENTIALID()函数生成", String.class),
    XML("XML", null, "2005+", "存储XML数据，支持XML语法验证和XQuery查询", String.class),
    //JSON("JSON", null, "2016+", "存储JSON数据，SQL Server 2022增强了JSON验证和索引支持", String.class),
    GEOGRAPHY("GEOGRAPHY", null, "2008+", "存储地理空间数据（椭圆体坐标），支持空间索引和计算", String.class),
    GEOMETRY("GEOMETRY", null, "2008+", "存储平面几何数据（欧几里得坐标），支持空间操作", String.class),
    HIERARCHYID("HIERARCHYID", null, "2008+", "存储层次结构数据，优化树状结构查询", String.class),
    ROWVERSION("ROWVERSION", null, "", "自动生成的二进制数字，用于版本控制，等价于TIMESTAMP", byte[].class),
    // 因为它有歧义所以fieldType换一下,不然其他数据库很多TIMESTAMP代表时间 然后这里来一匹配就出问题了
    @Deprecated
    TIMESTAMP("TIMESTAMP_", null, "", "ROWVERSION的同义词，用于乐观并发控制", byte[].class),
    SQL_VARIANT("SQL_VARIANT", null, "", "可以存储多种数据类型的值（除TEXT、NTEXT、IMAGE、XML、TIMESTAMP和SQL_VARIANT外）", Object.class);
    //ENUM("ENUM", "ENUM({0})", "2022+", "SQL Server 2022新增，枚举类型，{0}为逗号分隔的枚举值列表", String.class, int.class),
    //SENSITIVE_DATA_MASKING("SENSITIVE_DATA_MASKING", null, "2016+", "数据脱敏类型，用于敏感数据保护，基于基础类型", String.class),

    // ========================= 全文搜索类型 =========================
    //FULLTEXT("FULLTEXT", null, "", "全文搜索索引类型，用于大型文本数据的高效搜索", String.class);

    private final String fieldType;          // SQL Server 字段类型名称（标准名）
    private final String fieldTypeTemplate;  // 字段类型模板（多参数填模板，单参数/无参数为null）
    private final String supportedVersions;  // 支持的 SQL Server 版本（空表示全版本支持）
    private final String description;        // 类型简介（含特性说明）
    private final Class<?>[] javaTypes;      // 对应的 Java 类型（严格匹配指定范围）

    /**
     * 构造函数
     *
     * @param fieldType         SQL Server 标准类型名
     * @param fieldTypeTemplate 类型模板（多参数如DECIMAL({0},{1})，否则为null）
     * @param supportedVersions 支持版本
     * @param description       描述
     * @param javaTypes         对应Java类型
     */
    SqlServerFieldType(String fieldType, String fieldTypeTemplate, String supportedVersions, String description, Class<?>... javaTypes) {
        this.fieldType = fieldType;
        this.fieldTypeTemplate = fieldTypeTemplate;
        this.supportedVersions = supportedVersions;
        this.description = description;
        this.javaTypes = javaTypes;
    }

    /**
     * 根据 SQL Server 字段类型名（忽略大小写）获取枚举
     *
     * @param dataType 字段类型名（如 "varchar"、"bigint"）
     * @return 对应的枚举，无匹配则返回 null
     */
    public static SqlServerFieldType getFromDataType(String dataType) {
        if (StrUtil.isBlank(dataType)) {
            return null;
        }
        for (SqlServerFieldType value : SqlServerFieldType.values()) {
            if (StrUtil.equalsIgnoreCase(dataType, value.getFieldType())) {
                return value;
            }
        }
        // 处理特殊情况，如"int identity"、"varchar(max)"等组合类型
        String coreType = dataType.trim().split("\\s+")[0];
        for (SqlServerFieldType value : SqlServerFieldType.values()) {
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
    public static SqlServerFieldType getByClass(Class<?> aClass) {
        if (aClass == null) {
            return null;
        }
        String cacheKey = "SqlServer2022SqlFieldType:" + aClass.getName();
        Object cached = ObjectHolder.INSTANCE.getObject(cacheKey);
        if (cached != null) {
            return (SqlServerFieldType) cached;
        }

        // 1. 精确匹配 Java 类型
        for (SqlServerFieldType value : SqlServerFieldType.values()) {
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
        for (SqlServerFieldType value : SqlServerFieldType.values()) {
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
