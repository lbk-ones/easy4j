package io.github.lbkones.pure;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字符串工具类，模仿 Hutool 的 StrUtil
 * 提供常用的字符串处理方法
 */
public class StrUtil {

    private static final String EMPTY = "";
    private static final String NULL_STR = "null";
    private static final int INDEX_NOT_FOUND = -1;

    // ==================== 判断相关 ====================

    /**
     * 判断字符串是否为空（包括 null 和空字符串）
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static String blankToDefault(CharSequence str, String defaultStr) {
        return isBlank(str) ? defaultStr : str.toString();
    }

    /**
     * 判断字符串是否为空白（包括 null、空字符串和只有空格）
     */
    public static boolean isBlank(CharSequence str) {
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
     * 判断字符串是否不为空白
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为 null
     */
    public static boolean isNull(CharSequence str) {
        return str == null;
    }


    public static boolean equals(CharSequence str1, CharSequence str2) {
        return equals(str1, str2, false);
    }
    public static boolean equals(CharSequence str1, CharSequence str2, boolean ignoreCase) {
        if (null == str1) {
            // 只有两个都为null才判断相等
            return str2 == null;
        }
        if (null == str2) {
            // 字符串2空，字符串1非空，直接false
            return false;
        }

        if (ignoreCase) {
            return str1.toString().equalsIgnoreCase(str2.toString());
        } else {
            return str1.toString().contentEquals(str2);
        }
    }

    /**
     * 判断字符串是否不为 null
     */
    public static boolean isNotNull(CharSequence str) {
        return !isNull(str);
    }

    /**
     * 判断字符串是否全为大写字母
     */
    public static boolean isUpperCase(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLowerCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否全为小写字母
     */
    public static boolean isLowerCase(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为纯数字
     */
    public static boolean isNumeric(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    // ==================== 转换相关 ====================

    /**
     * 转换为字符串，null 返回空字符串
     */
    public static String str(Object obj) {
        return null == obj ? EMPTY : obj.toString();
    }

    /**
     * 转换为字符串，null 返回指定的默认值
     */
    public static String str(Object obj, String defaultValue) {
        return null == obj ? defaultValue : obj.toString();
    }

    /**
     * 字符串转换为大写
     */
    public static String toUpperCase(CharSequence str) {
        return isEmpty(str) ? EMPTY : str.toString().toUpperCase();
    }

    /**
     * 字符串转换为小写
     */
    public static String toLowerCase(CharSequence str) {
        return isEmpty(str) ? EMPTY : str.toString().toLowerCase();
    }

    /**
     * 首字母转换为大写
     */
    public static String upperFirst(CharSequence str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        char c = str.charAt(0);
        return Character.isUpperCase(c) ? str.toString() : Character.toUpperCase(c) + str.subSequence(1, str.length()).toString();
    }

    /**
     * 首字母转换为小写
     */
    public static String lowerFirst(CharSequence str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        char c = str.charAt(0);
        return Character.isLowerCase(c) ? str.toString() : Character.toLowerCase(c) + str.subSequence(1, str.length()).toString();
    }

    // ==================== 截取相关 ====================

    /**
     * 截取字符串前面的部分
     */
    public static String sub(CharSequence str, int start) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        int len = str.length();
        if (start < 0) {
            start = len + start;
        }
        if (start < 0) {
            start = 0;
        }
        if (start >= len) {
            return EMPTY;
        }
        return str.subSequence(start, len).toString();
    }

    /**
     * 截取字符串的部分
     */
    public static String sub(CharSequence str, int start, int end) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        int len = str.length();
        if (start < 0) {
            start = len + start;
        }
        if (end < 0) {
            end = len + end;
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > len) {
            end = len;
        }
        if (start >= end) {
            return EMPTY;
        }
        return str.subSequence(start, end).toString();
    }

    /**
     * 取前 n 个字符
     */
    public static String subPre(CharSequence str, int len) {
        return sub(str, 0, len);
    }

    /**
     * 取最后 n 个字符
     */
    public static String subSuf(CharSequence str, int len) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return sub(str, -len);
    }

    // ==================== 查找相关 ====================

    /**
     * 查找字符在字符串中的位置
     */
    public static int indexOf(CharSequence str, char ch) {
        return indexOf(str, ch, 0);
    }

    /**
     * 查找字符在字符串中的位置
     */
    public static int indexOf(CharSequence str, char ch, int start) {
        if (isEmpty(str)) {
            return INDEX_NOT_FOUND;
        }
        return str.toString().indexOf(ch, start);
    }

    /**
     * 查找子字符串在字符串中的位置
     */
    public static int indexOf(CharSequence str, CharSequence searchStr) {
        return indexOf(str, searchStr, 0);
    }

    /**
     * 查找子字符串在字符串中的位置
     */
    public static int indexOf(CharSequence str, CharSequence searchStr, int start) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return INDEX_NOT_FOUND;
        }
        return str.toString().indexOf(searchStr.toString(), start);
    }

    /**
     * 最后一次出现的位置
     */
    public static int lastIndexOf(CharSequence str, char ch) {
        if (isEmpty(str)) {
            return INDEX_NOT_FOUND;
        }
        return str.toString().lastIndexOf(ch);
    }

    /**
     * 最后一次出现的位置
     */
    public static int lastIndexOf(CharSequence str, CharSequence searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return INDEX_NOT_FOUND;
        }
        return str.toString().lastIndexOf(searchStr.toString());
    }

    // ==================== 替换相关 ====================

    /**
     * 替换字符串
     */
    public static String replace(CharSequence str, CharSequence searchStr, CharSequence replacement) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return str.toString().replace(searchStr, replacement);
    }

    /**
     * 替换指定位置的字符
     */
    public static String replace(CharSequence str, int pos, char replacement) {
        if (isEmpty(str) || pos < 0 || pos >= str.length()) {
            return str == null ? EMPTY : str.toString();
        }
        char[] chars = str.toString().toCharArray();
        chars[pos] = replacement;
        return new String(chars);
    }

    /**
     * 使用正则表达式替换
     */
    public static String replaceAll(CharSequence str, String regex, String replacement) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return str.toString().replaceAll(regex, replacement);
    }

    /**
     * 使用正则表达式替换第一个
     */
    public static String replaceFirst(CharSequence str, String regex, String replacement) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return str.toString().replaceFirst(regex, replacement);
    }

    // ==================== 去空格相关 ====================

    /**
     * 去掉字符串两端的空格
     */
    public static String trim(CharSequence str) {
        return isEmpty(str) ? EMPTY : str.toString().trim();
    }

    /**
     * 去掉字符串左边的空格
     */
    public static String trimStart(CharSequence str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        String s = str.toString();
        int start = 0;
        while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        return s.substring(start);
    }

    /**
     * 去掉字符串右边的空格
     */
    public static String trimEnd(CharSequence str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        String s = str.toString();
        int end = s.length();
        while (end > 0 && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(0, end);
    }

    /**
     * 去掉所有空格
     */
    public static String removeAllBlank(CharSequence str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return str.toString().replaceAll("\\s+", "");
    }

    /**
     * 去掉所有指定的字符
     */
    public static String remove(CharSequence str, char ch) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return str.toString().replace(String.valueOf(ch), EMPTY);
    }

    // ==================== 分割相关 ====================

    /**
     * 分割字符串
     */
    public static String[] split(CharSequence str, char separator) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.toString().split(String.valueOf(separator));
    }

    /**
     * 分割字符串
     */
    public static String[] split(CharSequence str, String separator) {
        if (isEmpty(str)) {
            return new String[0];
        }
        return str.toString().split(separator);
    }

    /**
     * 分割字符串为列表
     */
    public static List<String> splitToList(CharSequence str, char separator) {
        if (isEmpty(str)) {
            return new ArrayList<>();
        }
        return Arrays.asList(split(str, separator));
    }

    /**
     * 分割字符串为列表
     */
    public static List<String> splitToList(CharSequence str, String separator) {
        if (isEmpty(str)) {
            return new ArrayList<>();
        }
        return Arrays.asList(split(str, separator));
    }

    // ==================== 连接相关 ====================

    /**
     * 用指定分隔符连接数组
     */
    public static String join(CharSequence separator, Object... objs) {
        if (null == objs || objs.length == 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < objs.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(objs[i]);
        }
        return sb.toString();
    }

    /**
     * 用指定分隔符连接集合
     */
    public static String join(CharSequence separator, Iterable<?> iterable) {
        if (null == iterable) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object obj : iterable) {
            if (!first) {
                sb.append(separator);
            }
            sb.append(obj);
            first = false;
        }
        return sb.toString();
    }

    // ==================== 其他相关 ====================

    /**
     * 字符串长度
     */
    public static int length(CharSequence str) {
        return null == str ? 0 : str.length();
    }

    /**
     * 字符串是否以指定字符串开头
     */
    public static boolean startWith(CharSequence str, CharSequence prefix) {
        if (isEmpty(str) || isEmpty(prefix)) {
            return false;
        }
        return str.toString().startsWith(prefix.toString());
    }

    /**
     * 字符串是否以指定字符串结尾
     */
    public static boolean endWith(CharSequence str, CharSequence suffix) {
        if (isEmpty(str) || isEmpty(suffix)) {
            return false;
        }
        return str.toString().endsWith(suffix.toString());
    }

    /**
     * 字符串是否包含指定字符串
     */
    public static boolean contains(CharSequence str, CharSequence searchStr) {
        if (isEmpty(str) || isEmpty(searchStr)) {
            return false;
        }
        return str.toString().contains(searchStr);
    }

    /**
     * 字符串是否包含空格
     */
    public static boolean containsBlank(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------ contains

    /**
     * 指定字符是否在字符串中出现过
     *
     * @param str        字符串
     * @param searchChar 被查找的字符
     * @return 是否包含
     * @since 3.1.2
     */
    public static boolean contains(CharSequence str, char searchChar) {
        return indexOf(str, searchChar) > -1;
    }


    /**
     * 重复字符串
     */
    public static String repeat(CharSequence str, int count) {
        if (isEmpty(str) || count <= 0) {
            return EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 反转字符串
     */
    public static String reverse(CharSequence str) {
        if (isEmpty(str)) {
            return EMPTY;
        }
        return new StringBuilder(str).reverse().toString();
    }

    /**
     * 获取字符串的字节数组
     */
    public static byte[] getBytes(CharSequence str, Charset charset) {
        if (isEmpty(str)) {
            return new byte[0];
        }
        return str.toString().getBytes(charset == null ? StandardCharsets.UTF_8 : charset);
    }

    /**
     * 获取字符串的字节数组（默认 UTF-8）
     */
    public static byte[] getBytes(CharSequence str) {
        return getBytes(str, StandardCharsets.UTF_8);
    }

    /**
     * 把字节数组转换为字符串
     */
    public static String str(byte[] bytes, Charset charset) {
        if (null == bytes || bytes.length == 0) {
            return EMPTY;
        }
        return new String(bytes, charset == null ? StandardCharsets.UTF_8 : charset);
    }

    /**
     * 把字节数组转换为字符串（默认 UTF-8）
     */
    public static String str(byte[] bytes) {
        return str(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 格式化字符串（使用 String.format）
     */
    public static String format(String template, Object... params) {
        if (isEmpty(template)) {
            return template;
        }
        return String.format(template, params);
    }

    public static String toUnderlineCase(String name){
        return NamingCase.toUnderlineCase(name);
    }
}