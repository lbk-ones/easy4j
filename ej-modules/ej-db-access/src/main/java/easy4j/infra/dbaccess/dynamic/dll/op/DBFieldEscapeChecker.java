package easy4j.infra.dbaccess.dynamic.dll.op;

import java.util.regex.Pattern;

/**
 * 数据库字段名转义检测工具类
 * 用于检测字段名是否需要转义（不考虑保留关键字的情况）
 *
 * @author bokun.li
 * @date 2025/9/11
 */
public class DBFieldEscapeChecker {

    // 匹配合法的首字符：字母(a-z, A-Z)或下划线(_)
    private static final Pattern VALID_FIRST_CHAR_PATTERN = Pattern.compile("^[a-zA-Z_].*");

    // 匹配合法的字段名：仅包含字母、数字、下划线
    private static final Pattern VALID_FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * 检查字段名是否需要转义
     * @param fieldName 数据库字段名
     * @return 是否需要转义
     */
    public static boolean needEscape(String fieldName) {
        // 空字段名直接返回需要转义
        if (fieldName == null || fieldName.trim().isEmpty()) {
            return true;
        }

        // 检查1：首字符是否合法（是否以数字或其他特殊字符开头）
        if (!VALID_FIRST_CHAR_PATTERN.matcher(fieldName).matches()) {
            return true;
        }

        // 检查2：是否包含特殊字符或空格（非字母、数字、下划线）
        if (!VALID_FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
            return true;
        }

        // 检查3：是否包含非ASCII字符（多语言字符）
        if (!isAllASCII(fieldName)) {
            return true;
        }

        // 所有检查都通过，不需要转义
        return false;
    }

    /**
     * 检查字符串是否只包含ASCII字符
     * @param str 待检查的字符串
     * @return 是否只包含ASCII字符
     */
    private static boolean isAllASCII(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试案例
        String[] testCases = {
                "username",          // 不需要转义
                "user_name",         // 不需要转义
                "user name",         // 包含空格，需要转义
                "2ndName",           // 数字开头，需要转义
                "user-age",          // 包含特殊字符，需要转义
                "price$",            // 包含特殊字符，需要转义
                "用户姓名",           // 非ASCII字符，需要转义
                "café",              // 非ASCII字符，需要转义
                "order123",          // 不需要转义
                "_temp",             // 不需要转义
                "my.field",          // 包含特殊字符，需要转义
                "status",            // 不需要转义（假设有表名也为status，此处不检测这种情况）
                "ssc_dict_rule_algorithm_params"
        };

        System.out.println("字段名转义检测结果：");
        System.out.println("--------------------------------");
        for (String field : testCases) {
            boolean need = needEscape(field);
            System.out.printf("%-10s -> %s%n",
                    field, need ? "需要转义" : "不需要转义");
        }
    }
}
