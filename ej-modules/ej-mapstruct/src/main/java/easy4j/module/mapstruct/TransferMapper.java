package easy4j.module.mapstruct;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TransferMapper {


    private TransferMapper() {
    }

    // String 转 Integer
    public static Integer stringToInteger(String str) {
        return str != null && !str.isEmpty() ? Integer.parseInt(str) : null;
    }

    // Integer 转 String
    public static String integerToString(Integer num) {
        return num != null ? String.valueOf(num) : null;
    }

    // String 转 Double
    public static Double stringToDouble(String str) {
        return str != null && !str.isEmpty() ? Double.parseDouble(str) : null;
    }

    // Double 转 String
    public static String doubleToString(Double num) {
        return num != null ? String.valueOf(num) : null;
    }

    // String 转 Float
    public static Float stringToFloat(String str) {
        return str != null && !str.isEmpty() ? Float.parseFloat(str) : null;
    }

    // Float 转 String
    public static String floatToString(Float num) {
        return num != null ? String.valueOf(num) : null;
    }

    // String 转 Character
    public static Character stringToCharacter(String str) {
        return str != null && !str.isEmpty() ? str.charAt(0) : null;
    }

    // Character 转 String
    public static String characterToString(Character c) {
        return c != null ? String.valueOf(c) : null;
    }

    // String 转 Long
    public static Long stringToLong(String str) {
        return str != null && !str.isEmpty() ? Long.parseLong(str) : null;
    }

    // Long 转 String
    public static String longToString(Long num) {
        return num != null ? String.valueOf(num) : null;
    }

    // String 转 Date
    public static Date stringToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return DateUtil.parse(dateStr).toJdkDate();
        } catch (Exception e) {
            throw new IllegalArgumentException("日期解析错误， " , e);
        }
    }

    // Date 转 String
    public static String dateToString(Date date) {
        return date != null ? DateUtil.format(date,DatePattern.NORM_DATETIME_PATTERN) : null;
    }

    // String 转 BigDecimal
    public static BigDecimal stringToBigDecimal(String str) {
        return str != null && !str.isEmpty() ? new BigDecimal(str) : null;
    }

    // BigDecimal 转 String
    public static String bigDecimalToString(BigDecimal num) {
        return num != null ? num.stripTrailingZeros().toPlainString() : null;
    }


    // LocalDate 转 String
    public static String localDateToString(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return LocalDateTimeUtil.format(localDate, DatePattern.NORM_DATE_PATTERN);
    }

    // String 转 LocalDate
    public static LocalDate stringToLocalDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        return LocalDateTimeUtil.parseDate(dateStr);
    }

    // LocalDateTime 转 String
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return LocalDateTimeUtil.format(localDateTime, DatePattern.NORM_DATETIME_PATTERN);
    }

    // String 转 LocalDateTime
    public static LocalDateTime stringToLocalDateTime(String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        return LocalDateTimeUtil.parse(dateTimeStr);
    }

    // LocalTime 转 String
    public static String localTimeToString(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return localTime.format(dateTimeFormatter);
    }

    // String 转 LocalTime
    public static LocalTime stringToLocalTime(String timeStr) {
        if (timeStr == null || StrUtil.isBlank(timeStr)) {
            return null;
        }
        return LocalTime.parse(timeStr);
    }
}