package easy4j.infra.common.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * RegexEscapeUtils
 * 正则表达式特殊字符转义工具类 转义正则中具有特殊含义的元字符，使其作为普通字符处理
 *
 * @author bokun.li
 * @date 2025/9/4
 */
public class RegexEscapeUtils {

    // 正则表达式中需要转义的特殊字符集合
    private static final Set<Character> SPECIAL_CHARS;

    static {
        // 初始化需要转义的正则元字符
        SPECIAL_CHARS = new HashSet<>();
        String special = ".^$*+?()[]{}|\\<>";
        for (char c : special.toCharArray()) {
            SPECIAL_CHARS.add(c);
        }
    }

    /**
     * 转义正则表达式中的特殊字符
     *
     * @param input 原始字符串（可能包含正则特殊字符）
     * @return 转义后的字符串（特殊字符前添加反斜杠）
     */
    public static String escapeRegex(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            // 如果是特殊字符，则先添加反斜杠转义
            if (SPECIAL_CHARS.contains(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
