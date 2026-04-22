package io.github.lbkones.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字符串工具类，提供丰富的字符串处理方法
 * 包含基础操作、高级处理、安全加密等功能
 */
public final class StrUtils {

    // 私有构造方法，防止实例化
    private StrUtils() {
        throw new AssertionError("工具类不能实例化");
    }

    /**
     * =====================
     * 基础字符串操作
     * =====================
     */

    /**
     * 判断字符串是否为空（null 或长度为0）
     * @param str 待检查的字符串
     * @return 为空返回true，否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否不为空（非null且长度大于0）
     * @param str 待检查的字符串
     * @return 不为空返回true，否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否空白（null、长度为0或仅包含空白字符）
     * @param str 待检查的字符串
     * @return 为空白返回true，否则返回false
     */
    public static boolean isBlank(String str) {
        if (isEmpty(str)) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否不空白
     * @param str 待检查的字符串
     * @return 不空白返回true，否则返回false
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 去除字符串首尾空白
     * @param str 待处理的字符串
     * @return 去除首尾空白后的字符串，原字符串为null返回null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 去除字符串中所有空白字符
     * @param str 待处理的字符串
     * @return 去除所有空白后的字符串，原字符串为null返回null
     */
    public static String trimAll(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("\\s+", "");
    }

    /**
     * 将字符串转换为小写
     * @param str 待转换的字符串
     * @return 转换为小写后的字符串，原字符串为null返回null
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * 将字符串转换为大写
     * @param str 待转换的字符串
     * @return 转换为大写后的字符串，原字符串为null返回null
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }

    /**
     * =====================
     * 字符串处理与转换
     * =====================
     */

    /**
     * 截取字符串（安全版，不会抛出异常）
     * @param str 待截取的字符串
     * @param start 开始位置
     * @param end 结束位置
     * @return 截取后的字符串，原字符串为null返回null
     */
    public static String substring(String str, int start, int end) {
        if (isEmpty(str) || start < 0 || end < 0 || start > end) {
            return str;
        }
        if (start >= str.length()) {
            return "";
        }
        if (end > str.length()) {
            end = str.length();
        }
        return str.substring(start, end);
    }

    /**
     * 按分隔符分割字符串
     * @param str 待分割的字符串
     * @param delimiter 分隔符
     * @return 分割后的字符串数组，原字符串为null返回null
     */
    public static String[] split(String str, String delimiter) {
        if (isEmpty(str) || isEmpty(delimiter)) {
            return new String[]{str};
        }
        return str.split(Pattern.quote(delimiter));
    }

    /**
     * 按分隔符分割字符串并过滤空值
     * @param str 待分割的字符串
     * @param delimiter 分隔符
     * @return 分割并过滤后的字符串列表
     */
    public static List<String> splitAndFilter(String str, String delimiter) {
        if (isBlank(str) || isBlank(delimiter)) {
            return List.of();
        }
        return Arrays.stream(str.split(Pattern.quote(delimiter)))
                .filter(s -> isNotBlank(s))
                .collect(Collectors.toList());
    }

    /**
     * 将字符串列表连接成一个字符串
     * @param list 字符串列表
     * @param delimiter 分隔符
     * @return 连接后的字符串
     */
    public static String join(List<String> list, String delimiter) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (delimiter == null) {
            delimiter = "";
        }
        return String.join(delimiter, list);
    }

    /**
     * 重复字符串指定次数
     * @param str 待重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int count) {
        if (isEmpty(str) || count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * =====================
     * 高级字符串处理
     * =====================
     */

    /**
     * 格式化字符串（类似String.format，但更安全）
     * @param format 格式字符串
     * @param args 参数
     * @return 格式化后的字符串
     */
    public static String format(String format, Object... args) {
        if (isEmpty(format)) {
            return format;
        }
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return format; // 格式化失败返回原字符串
        }
    }

    /**
     * 检查字符串是否匹配正则表达式
     * @param str 待检查的字符串
     * @param regex 正则表达式
     * @return 匹配返回true，否则返回false
     */
    public static boolean matches(String str, String regex) {
        if (isEmpty(str) || isEmpty(regex)) {
            return false;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * 从字符串中提取匹配正则表达式的内容
     * @param str 待提取的字符串
     * @param regex 正则表达式
     * @return 匹配结果列表
     */
    public static List<String> extract(String str, String regex) {
        if (isBlank(str) || isBlank(regex)) {
            return List.of();
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        List<String> results = new ArrayList<>();
        while (matcher.find()) {
            results.add(matcher.group());
        }
        return results;
    }

    /**
     * 转义HTML特殊字符
     * @param str 待转义的字符串
     * @return 转义后的字符串
     */
    public static String escapeHtml(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 反转义HTML特殊字符
     * @param str 待反转义的字符串
     * @return 反转义后的字符串
     */
    public static String unescapeHtml(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
    }

    /**
     * =====================
     * 字符串安全与加密
     * =====================
     */

    /**
     * 生成字符串的MD5哈希值
     * @param str 待加密的字符串
     * @return MD5哈希值（16进制字符串）
     */
    public static String md5(String str) {
        if (isEmpty(str)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 生成字符串的SHA-256哈希值
     * @param str 待加密的字符串
     * @return SHA-256哈希值（16进制字符串）
     */
    public static String sha256(String str) {
        if (isEmpty(str)) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }

    /**
     * =====================
     * 其他实用方法
     * =====================
     */

    /**
     * 获取字符串的长度，安全处理null
     * @param str 待获取长度的字符串
     * @return 字符串长度，null返回0
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * 检查字符串是否以指定前缀开头
     * @param str 待检查的字符串
     * @param prefix 前缀
     * @return 以指定前缀开头返回true，否则返回false
     */
    public static boolean startsWith(String str, String prefix) {
        return str != null && prefix != null && str.startsWith(prefix);
    }

    /**
     * 检查字符串是否以指定后缀结尾
     * @param str 待检查的字符串
     * @param suffix 后缀
     * @return 以指定后缀结尾返回true，否则返回false
     */
    public static boolean endsWith(String str, String suffix) {
        return str != null && suffix != null && str.endsWith(suffix);
    }

    /**
     * 生成指定长度的随机字符串（包含大小写字母和数字）
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        if (length <= 0) {
            return "";
        }
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 比较两个字符串是否相等（安全处理null）
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 相等返回true，否则返回false
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 比较两个字符串是否相等（忽略大小写，安全处理null）
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 相等返回true，否则返回false
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return str1 == str2;
        }
        return str1.equalsIgnoreCase(str2);
    }
}