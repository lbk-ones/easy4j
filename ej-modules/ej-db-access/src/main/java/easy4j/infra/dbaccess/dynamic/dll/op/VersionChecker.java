package easy4j.infra.dbaccess.dynamic.dll.op;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 版本号比较
 * x.y.z 比较 如果不合法先提取数字加点的版本号 如果格式不满足 x.y.z 以.0填充
 *
 * @author bokun.li
 * @date 2025/8/27
 */
public class VersionChecker {

    /**
     * 判断版本号是否大于等于目标版本号
     *
     * @param currentVersion 当前版本号 (x.y.z格式)
     * @param targetVersion  目标版本号 (x.y.z格式)
     * @return 如果currentVersion >= targetVersion返回true，否则返回false
     * @throws IllegalArgumentException 如果版本号格式不正确
     */
    public static boolean isGreaterOrEqual(String currentVersion, String targetVersion) {
        // 验证版本号格式
        if (isNotValidVersion(currentVersion) || isNotValidVersion(targetVersion)) {
            throw new IllegalArgumentException("版本号格式不正确，必须为x.y.z格式（x、y、z为非负整数）");
        }

        currentVersion = compatibleVersion(currentVersion);
        targetVersion = compatibleVersion(targetVersion);

        // 分割版本号为整数数组
        int[] currentParts = parseVersion(currentVersion);
        int[] targetParts = parseVersion(targetVersion);

        // 依次比较主版本、次版本、修订号
        for (int i = 0; i < 3; i++) {
            if (currentParts[i] > targetParts[i]) {
                return true;
            } else if (currentParts[i] < targetParts[i]) {
                return false;
            }
        }

        // 所有部分都相等
        return true;
    }

    /**
     * 从版本号中提取数字加点的部分（如从5.7.22-log提取5.7.22）
     */
    private static String extractVersionNumbers(String version) {
        if (version == null || version.isEmpty()) {
            return "";
        }
        // 正则匹配开头的数字加点部分（至少一位数字，后跟点和数字，重复2次）
        // + 一次或者多次
        // ? 0次或者1次
        // * 0次或者多次
        // \\d 数字匹配
        Pattern pattern = Pattern.compile("^(\\d+\\.?\\d*\\.?\\d*)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 兼容版本号
     *
     * @param currentVersion
     * @return
     */
    private static String compatibleVersion(String currentVersion) {
        currentVersion = extractVersionNumbers(currentVersion);
        int dotLength = 0;
        for (char c : currentVersion.toCharArray()) {
            if (c == '.') {
                dotLength++;
            }
        }
        if (dotLength < 2) {
            int i = 2 - dotLength;
            StringBuilder currentVersionBuilder = new StringBuilder(currentVersion);
            for (int j = 0; j < i; j++) {
                currentVersionBuilder.append(".0");
            }
            currentVersion = currentVersionBuilder.toString();
        }
        return currentVersion;
    }

    /**
     * 解析版本号为整数数组
     */
    private static int[] parseVersion(String version) {
        return Arrays.stream(version.split("\\."))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /**
     * 验证版本号格式是否为x.y.z
     */
    private static boolean isNotValidVersion(String version) {
        return version == null || version.isEmpty();
//        // 正则匹配三个由点分隔的非负整数
//        return version.matches("^\\d+\\.\\d+\\.\\d+$");
    }

    // 测试方法
    public static void main(String[] args) {
        String s = extractVersionNumbers("19c");
        String s3 = extractVersionNumbers("19c+");
        String s2 = extractVersionNumbers("5.7.22-log");
        String s4 = extractVersionNumbers("5.7.22+");
        String s5 = extractVersionNumbers("15.4 (Debian 15.4-2.pgdg120+1) on x86_64-pc-linux-gnu, compiled by gcc (Debian 12.2.0-14) 12.2.0, 64-bit");
        String s6 = extractVersionNumbers("2.1.214 (2022-06-13)");
        System.out.println(s);
        System.out.println(s3);
        System.out.println(s2);
        System.out.println(s4);
        System.out.println(s5);
        System.out.println(s6);
        // 测试用例
        System.out.println(isGreaterOrEqual("1.2.3", "1.2.3"));  // true (相等)
        System.out.println(isGreaterOrEqual("1.2", "1.2.3"));  // true (修订号更大)
        System.out.println(isGreaterOrEqual("1.3.0", "1.2.9"));  // true (次版本号更大)
        System.out.println(isGreaterOrEqual("2.0.0", "1.9.9"));  // true (主版本号更大)
        System.out.println(isGreaterOrEqual("1.2.2", "1.2.3"));  // false (修订号更小)
        System.out.println(isGreaterOrEqual("1.1.9", "1.2.0"));  // false (次版本号更小)
        System.out.println(isGreaterOrEqual("0.9.9", "1.0.0"));  // false (主版本号更小)
    }
}
    