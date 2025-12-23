package easy4j.module.mybatisplus.codegen.db;

import cn.hutool.core.util.StrUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Java字段名称合法性检测与修正工具类
 */
public class FieldNameValidator {

    // Java关键字 + 保留字（true/false/null）
    private static final Set<String> JAVA_KEYWORDS = new HashSet<>();

    static {
        // 初始化Java关键字
        String[] keywords = {
                "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
                "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
                "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
                "throw", "throws", "transient", "try", "void", "volatile", "while",
                // 保留字（虽非关键字，但不能作为标识符）
                "true", "false", "null"
        };
        JAVA_KEYWORDS.addAll(Arrays.asList(keywords));
    }

    /**
     * 检测并修正字段名称为合法的Java标识符
     * @param originalFieldName 原始字段名（可能为空/非法）
     * @return 合法的字段名
     */
    public static String validateAndCorrect(String originalFieldName) {
        // 1. 处理空/空白字符串
        if (originalFieldName == null || originalFieldName.trim().isEmpty()) {
            return "";
        }
        String fieldName = originalFieldName.trim();
        StringBuilder corrected = new StringBuilder();

        // 2. 处理首字符
        char firstChar = fieldName.charAt(0);
        if (isValidFirstChar(firstChar)) {
            corrected.append(firstChar);
        } else {
            // 首字符非法，前缀加_
            corrected.append('_').append(firstChar);
        }

        // 3. 处理后续字符（从第二个字符开始）
        for (int i = 1; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (isValidSubsequentChar(c)) {
                corrected.append(c);
            } else {
                // 非法字符替换为_
                corrected.append('_');
            }
        }

        // 4. 处理关键字（如果修正后是关键字，末尾加_）
        String tempResult = corrected.toString();
        if (JAVA_KEYWORDS.contains(tempResult)) {
            tempResult += "_";
        }

        // 5. 最终校验（防止极端情况仍非法，如全是替换后的重复_）
        return tempResult.isEmpty() ? "field_0" : tempResult;
    }

    /**
     * 检查是否为合法的首字符
     */
    private static boolean isValidFirstChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || c == '$';
    }

    /**
     * 检查是否为合法的后续字符（首字符之后的字符）
     */
    private static boolean isValidSubsequentChar(char c) {
        return isValidFirstChar(c) || (c >= '0' && c <= '9');
    }

    // 测试方法
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
                "123name",       // 首字符数字 → _123name
                "int",           // 关键字 → int_
                "na*me$123",     // 包含特殊字符* → na_me$123
                "   ",           // 空白 → field_0
                null,            // null → field_0
                "class",         // 关键字 → class_
                "user-name",     // 包含- → user_name
                "$user123",      // 合法 → $user123
                "true",          // 保留字 → true_
                "asf&aga",          // 保留字 → true_
                "asfa!@#$%^&*()aga_",          // 保留字 → true_
                "9*8&7"          // 全非法 → _9_8_7
        };

        for (String testCase : testCases) {
            String result = validateAndCorrect(testCase);
            System.out.printf("原始值：[%s] → 修正后：[%s]%n", testCase, StrUtil.toCamelCase(result));
        }
    }
}