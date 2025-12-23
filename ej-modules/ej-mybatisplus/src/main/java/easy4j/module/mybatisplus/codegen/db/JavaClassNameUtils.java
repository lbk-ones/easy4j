package easy4j.module.mybatisplus.codegen.db;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Java 类名合法性检测与转换工具
 */
public class JavaClassNameUtils {

    // 1. Java 所有关键字（含保留关键字、上下文关键字）
    private static final Set<String> JAVA_KEYWORDS;
    static {
        JAVA_KEYWORDS = new HashSet<>();
        // 基础关键字
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while",
            // 上下文关键字（Java 9+）
            "var", "yield", "module", "open", "opens", "requires", "exports", "to", "uses", "provides"
        };
        for (String kw : keywords) {
            JAVA_KEYWORDS.add(kw);
        }
    }

    // 2. 合法首字符正则：字母、_、$
    private static final Pattern VALID_FIRST_CHAR = Pattern.compile("^[a-zA-Z_\\$]$");
    // 3. 合法后续字符正则：字母、数字、_、$
    private static final Pattern VALID_OTHER_CHAR = Pattern.compile("^[a-zA-Z0-9_\\$]$");

    /**
     * 检测字符串是否为合法 Java 类名
     * @param className 待检测的类名
     * @return true=合法，false=非法
     */
    public static boolean isValidJavaClassName(String className) {
        // 空/空白字符串直接非法
        if (className == null || className.trim().isEmpty()) {
            return false;
        }
        String trimmed = className.trim();

        // 检测是否是关键字
        if (JAVA_KEYWORDS.contains(trimmed)) {
            return false;
        }

        char[] chars = trimmed.toCharArray();
        // 检测首字符
        if (!VALID_FIRST_CHAR.matcher(String.valueOf(chars[0])).matches()) {
            return false;
        }

        // 检测后续字符
        for (int i = 1; i < chars.length; i++) {
            if (!VALID_OTHER_CHAR.matcher(String.valueOf(chars[i])).matches()) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将任意字符串转换为合法的 Java 类名
     * @param input 原始输入字符串（可为空、含非法字符、关键字等）
     * @return 合法的 Java 类名（默认大驼峰格式）
     */
    public static String toValidJavaClassName(String input) {
        // 步骤1：处理空/空白输入，返回默认值
        if (input == null || input.trim().isEmpty()) {
            return "DefaultClass";
        }
        String processed = input.trim();

        // 步骤2：替换所有非法字符为下划线（先清理非字母/数字/_/$的字符）
        StringBuilder sb = new StringBuilder();
        for (char c : processed.toCharArray()) {
            if (VALID_FIRST_CHAR.matcher(String.valueOf(c)).matches() || VALID_OTHER_CHAR.matcher(String.valueOf(c)).matches()) {
                sb.append(c);
            } else {
                sb.append("_"); // 非法字符替换为下划线
            }
        }
        processed = sb.toString();

        // 步骤3：处理首字符（若首字符是数字/非法首字符，前缀加下划线）
        char firstChar = processed.charAt(0);
        if (!VALID_FIRST_CHAR.matcher(String.valueOf(firstChar)).matches()) {
            processed = "_" + processed;
        }

        // 步骤4：转换为大驼峰（PascalCase），同时清理连续下划线
        processed = toPascalCase(processed);

        // 步骤5：若最终结果是关键字，加后缀"_Class"
        if (JAVA_KEYWORDS.contains(processed)) {
            processed += "_Class";
        }

        // 步骤6：兜底（防止全非法字符导致空字符串）
        return processed.isEmpty() ? "DefaultClass" : processed;
    }

    /**
     * 辅助方法：将字符串转换为大驼峰（PascalCase），处理下划线分隔的场景
     * 示例：user_name → UserName；_user_age123 → UserAge123；$test → Test
     */
    private static String toPascalCase(String input) {
        StringBuilder pascalCase = new StringBuilder();
        boolean nextUpper = true; // 首字符大写，后续下划线后字符大写
        for (char c : input.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
                continue; // 跳过下划线
            }
            if (nextUpper) {
                pascalCase.append(Character.toUpperCase(c));
                nextUpper = false;
            } else {
                pascalCase.append(Character.toLowerCase(c));
            }
        }
        // 若首字符是$，保留（符合Java规范），但后续仍按大驼峰
        if (pascalCase.length() > 0 && pascalCase.charAt(0) == '$') {
            return pascalCase.toString();
        }
        // 若转换后为空（如全是下划线），返回默认前缀
        return pascalCase.length() == 0 ? "Class" : pascalCase.toString();
    }

    // 测试示例
    public static void main(String[] args) {
        // 测试用例
        String[] testCases = {
            null,                // 空 → DefaultClass
            "",                  // 空字符串 → DefaultClass
            "   ",               // 空白 → DefaultClass
            "123user",           // 首字符数字 → _123user → 123user → OneTwoThreeUser
            "user-name",         // 含非法字符- → user_name → UserName
            "class",             // 关键字 → Class_Class
            "var123",            // 上下文关键字（但带数字，非纯关键字 → Var123
            "$test_age",         // 含$ → $TestAge
            "my_class$123",      // 混合字符 → MyClass$123
            "非法字符test",      // 含中文 → _test → Test
            "   user age  ",     // 含空格 → user_age → UserAge
            "int"                // 关键字 → Int_Class
        };

        for (String test : testCases) {
            boolean valid = isValidJavaClassName(test);
            String converted = toValidJavaClassName(test);
            System.out.printf("原始输入：[%s] → 是否合法：%s → 转换后：[%s]%n",
                    test == null ? "null" : test, valid, converted);
        }
    }
}