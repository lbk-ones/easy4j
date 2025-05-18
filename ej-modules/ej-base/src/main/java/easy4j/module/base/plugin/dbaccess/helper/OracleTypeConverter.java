package easy4j.module.base.plugin.dbaccess.helper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class OracleTypeConverter {
    // 日期时间格式模板（与 Oracle 函数匹配）
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSS"; // 微秒级
    private static final String ZONED_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss Z"; // 时区格式如 +08:00

    // 日期时间格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
    private static final DateTimeFormatter ZONED_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(ZONED_DATETIME_FORMAT);

    /**
     * 根据 Java 类型和值生成 Oracle 插入语句的转换表达式
     * @param javaType Java 类型的 Class 对象
     * @param value 值（需为字符串或可转换为字符串的对象）
     * @return Oracle 转换表达式（如 TO_TIMESTAMP('2024-05-20 15:30:00', 'YYYY-MM-DD HH24:MI:SS')）
     */
    public static String convertToOracleExpression(Class<?> javaType, Object value) {
        if (value == null) {
            return "NULL"; // 空值直接返回 NULL
        }

        // 字符串类型处理
        if (javaType == String.class) {
            String strValue = (String) value;
            if (isDateString(strValue)) { // 假设能简单判断是否为日期格式（实际需更严谨校验）
                return String.format("TO_DATE('%s', 'YYYY-MM-DD')", strValue);
            } else if (strValue.length() > 4000) { // 长文本转 CLOB
                return String.format("TO_CLOB('%s')", escapeSqlString(strValue));
            } else {
                return String.format("'%s'", escapeSqlString(strValue));
            }
        }

        // 整数/长整型
        if (javaType == Integer.class || javaType == Long.class || javaType == int.class || javaType == long.class) {
            return value.toString();
        }

        // 布尔类型
        if (javaType == Boolean.class || javaType == boolean.class) {
            return (Boolean) value ? "1" : "0";
        }

        // BigDecimal
        if (javaType == BigDecimal.class) {
            return ((BigDecimal) value).toPlainString(); // 保留小数位（如 100.50）
        }

        // java.util.Date
        if (javaType == java.util.Date.class) {
            java.util.Date date = (java.util.Date) value;
            String formatted = new java.text.SimpleDateFormat(DATETIME_FORMAT).format(date);
            return String.format("TO_DATE('%s', 'YYYY-MM-DD HH24:MI:SS')", formatted);
        }

        // java.sql.Timestamp
        if (javaType == Timestamp.class) {
            Timestamp ts = (Timestamp) value;
            String formatted = new java.text.SimpleDateFormat(TIMESTAMP_FORMAT).format(ts);
            return String.format("TO_TIMESTAMP('%s', 'YYYY-MM-DD HH24:MI:SS.FF6')", formatted);
        }

        // Java 8 时间类型
        if (javaType == LocalDate.class) {
            LocalDate localDate = (LocalDate) value;
            String formatted = localDate.format(DATE_FORMATTER);
            return String.format("TO_DATE('%s', 'YYYY-MM-DD')", formatted);
        }
        if (javaType == LocalDateTime.class) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            String formatted = localDateTime.format(DATETIME_FORMATTER);
            return String.format("TO_TIMESTAMP('%s', 'YYYY-MM-DD HH24:MI:SS')", formatted);
        }
        if (javaType == ZonedDateTime.class) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            String formatted = zonedDateTime.format(ZONED_DATETIME_FORMATTER);
            return String.format("TO_TIMESTAMP_TZ('%s', 'YYYY-MM-DD HH24:MI:SS TZH:TZM')", formatted);
        }

        // 二进制数据（简化处理，实际需转换为十六进制）
        if (javaType == byte[].class) {
            byte[] bytes = (byte[]) value;
            String hex = bytesToHex(bytes);
            return String.format("UTL_RAW.CAST_TO_RAW('%s')", hex);
        }

        // 未知类型默认作为字符串处理
        return String.format("'%s'", escapeSqlString(value.toString()));
    }

    /**
     * 转义 SQL 字符串中的单引号（' → ''）
     */
    private static String escapeSqlString(String str) {
        return str.replace("'", "''");
    }

    /**
     * 简单判断字符串是否为日期格式（示例逻辑，实际需更严谨）
     */
    private static boolean isDateString(String str) {
        try {
            LocalDate.parse(str, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}