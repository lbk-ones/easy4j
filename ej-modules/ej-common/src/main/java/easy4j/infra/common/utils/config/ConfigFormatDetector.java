package easy4j.infra.common.utils.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
/**
 * 配置文件识别
 * @author bokun.li
 * @date 2025/10/22
 */
public class ConfigFormatDetector {

    // 匹配yml的键值对分隔符（:后必须有空格，且前面是合法键名）
    private static final Pattern YML_KV_PATTERN = Pattern.compile("^\\s*[^:=\\s]+\\s*:\\s+.*$");
    // 匹配yml的列表项（-后必须有空格，可带缩进）
    private static final Pattern YML_LIST_PATTERN = Pattern.compile("^\\s*-\\s+.*$");
    // 匹配properties的键值对分隔符（=前后可带空格）
    private static final Pattern PROPERTIES_KV_PATTERN = Pattern.compile("^\\s*[^:=\\s]+\\s*=\\s*.*$");

    /**
     * 判断字符串是properties、yml还是未知格式
     *
     * @param content 待判断的配置字符串
     * @return 结果："properties"、"yml" 或 "unknown"
     */
    public static String detect(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "unknown";
        }

        // 按行分割（保留原始缩进）
        String[] lines = content.split("\n");
        List<String> validLines = new ArrayList<>();

        // 过滤注释行（#开头）和空行（仅空白字符）
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue; // 跳过注释和空行
            }
            validLines.add(line); // 保留原始行（含缩进）
        }

        if (validLines.isEmpty()) {
            return "unknown"; // 全是注释或空行，无法判断
        }

        // 检测是否符合yml特征
        boolean isYml = checkYmlFeatures(validLines);
        if (isYml) {
            return "yml";
        }

        // 检测是否符合properties特征
        boolean isProperties = checkPropertiesFeatures(validLines);
        if (isProperties) {
            return "properties";
        }

        return "unknown";
    }

    /**
     * 检查是否有yml特有的特征
     */
    private static boolean checkYmlFeatures(List<String> validLines) {
        boolean hasYmlKv = false; // 是否有: 分隔的键值对
        boolean hasYmlList = false; // 是否有- 列表项
        boolean hasIndentation = false; // 是否有缩进层级

        // 记录每行的前导空格数（用于检测层级）
        List<Integer> indentCounts = new ArrayList<>();

        for (String line : validLines) {
            // 计算前导空格数（缩进量）
            int indentCount = 0;
            for (char c : line.toCharArray()) {
                if (c == ' ') {
                    indentCount++;
                } else {
                    break;
                }
            }
            indentCounts.add(indentCount);

            // 检测是否有yml键值对（: 分隔）
            if (YML_KV_PATTERN.matcher(line).matches()) {
                hasYmlKv = true;
            }

            // 检测是否有yml列表项（- 开头）
            if (YML_LIST_PATTERN.matcher(line).matches()) {
                hasYmlList = true;
            }
        }

        // 检测是否有缩进层级（存在不同的缩进量，且非零）
        if (indentCounts.size() > 1) {
            int firstIndent = indentCounts.get(0);
            for (int indent : indentCounts) {
                if (indent != firstIndent && indent > 0) {
                    hasIndentation = true;
                    break;
                }
            }
        }

        // 满足以下任一条件即判定为yml：
        // 1. 存在列表项；2. 存在: 分隔的键值对且有缩进层级
        return hasYmlList || (hasYmlKv && hasIndentation);
    }

    /**
     * 检查是否有properties特有的特征
     */
    private static boolean checkPropertiesFeatures(List<String> validLines) {
        // 存在=分隔的键值对即判定为properties
        for (String line : validLines) {
            if (PROPERTIES_KV_PATTERN.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }


    // 测试示例
    public static void main(String[] args) {
        // 测试properties
        String propertiesStr = "user.name=tom\nuser.age=18\n# 这是注释\napp.enabled=true";
        System.out.println(detect(propertiesStr)); // 输出：properties

        // 测试yml
        String ymlStr = "server:\n" +
                "  port: 10909\n" +
                "knife4j:\n" +
                "  enableAggregation: true\n" +
                "  nacos:\n" +
                "    enable: true\n" +
                "    service-url: http://localhost:8848/nacos\n" +
                "    service-auth:\n" +
                "      enable: true\n" +
                "      username: nacos\n" +
                "      password: nacos\n" +
                "    routes:\n" +
                "      - name: 元数据\n" +
                "        service-name: dataspace-metadata\n" +
                "        location: /v3/api-docs\n" +
                "        service-path: /\n" +
                "        namespace-id: develop\n" +
                "        group-name: dataspace-service\n" +
                "        clusters:\n" +
                "      - name: 字典\n" +
                "        serviceName: dataspace-dict\n" +
                "        location: /v3/api-docs\n" +
                "        service-path: /\n" +
                "        namespace-id: develop\n" +
                "        group-name: dataspace-service\n" +
                "\n" +
                "\n";
        System.out.println(detect(ymlStr)); // 输出：yml

        // 测试未知格式
        String unknownStr = "hello world\nthis is a test";
        System.out.println(detect(unknownStr)); // 输出：unknown
    }
}