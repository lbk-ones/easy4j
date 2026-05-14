package easy4j.infra.base.starter.env;

import cn.hutool.core.util.RandomUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysLog;
import lombok.Getter;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Spring Boot 配置文件加载处理器
 * <p>
 * 功能：
 * 1. 处理 spring.config.location 和 spring.config.additional-location
 * 2. 处理 spring.config.import
 * 3. 支持多种前缀：file:, classpath:, optional:
 * 4. 支持多个值（逗号分隔）
 * 5. 后加载的配置覆盖先加载的配置
 */
public class SpringBootConfigLoader {

    private static final String FILE_PREFIX = "file:";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String OPTIONAL_PREFIX = "optional:";
    private static final String CONFIG_LOCATION_PARAM = "spring.config.location";
    private static final String CONFIG_ADDITIONAL_LOCATION_PARAM = "spring.config.additional-location";
    private static final String CONFIG_IMPORT_PARAM = "spring.config.import";

    private final ResourceLoader resourceLoader;
    @Getter
    private final LinkedList<PropertySource<?>> propertyResolvers = new LinkedList<>();
    private final Map<String, Properties> loadedConfigs; // 记录加载顺序，后加载的覆盖
    private final Set<String> failedResources; // 记录失败的资源

    public SpringBootConfigLoader() {
        this(new PathMatchingResourcePatternResolver());
    }

    public SpringBootConfigLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.loadedConfigs = new LinkedHashMap<>(); // 保持加载顺序
        this.failedResources = new HashSet<>();
    }

    /**
     * 加载配置文件
     *
     * @param location           spring.config.location 参数值
     * @param additionalLocation spring.config.additional-location 参数值
     * @param importLocations    spring.config.import 参数值
     * @return 合并后的 Properties
     */
    public Properties loadConfigs(String location, String additionalLocation, String importLocations) {
        // 清空之前的加载记录
        loadedConfigs.clear();
        propertyResolvers.clear();
        failedResources.clear();

        List<String> configLocations = new LinkedList<>();

        // 1. 先加载 spring.config.location（会覆盖默认位置）
        if (StringUtils.hasText(location)) {
            configLocations.addAll(parseLocations(location));
        }

        // 2. 再加载 spring.config.additional-location（追加）
        if (StringUtils.hasText(additionalLocation)) {
            configLocations.addAll(parseLocations(additionalLocation));
        }

        // 3. 加载主配置文件
        for (String configLocation : configLocations) {
            loadConfigFromLocation(configLocation);
        }

        // 4. 最后处理 spring.config.import（在所有主配置之后）
        if (StringUtils.hasText(importLocations)) {
            Set<String> importedSet = new HashSet<>(); // 防止重复导入
            List<String> toImport = parseLocations(importLocations);
            processImports(toImport, importedSet);
        }

        // 5. 合并所有配置（后加载覆盖先加载）
        return mergeConfigs();
    }

    /**
     * 解析逗号分隔的位置字符串
     */
    private List<String> parseLocations(String locations) {
        if (!StringUtils.hasText(locations)) {
            return Collections.emptyList();
        }

        return Arrays.stream(locations.split(SP.COMMA))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 从单个位置加载配置
     */
    private void loadConfigFromLocation(String location) {
        location = location.trim();

        // 解析前缀
        boolean isOptional = location.startsWith(OPTIONAL_PREFIX);
        if (isOptional) {
            location = location.substring(OPTIONAL_PREFIX.length()).trim();
        }

        // 处理多种前缀
        Resource resource = resolveResource(location);

        if (resource == null || !resource.exists()) {
            String errorMsg = "Config resource not found: " + location;
            failedResources.add(errorMsg);

            if (!isOptional) {
                System.err.println(SysLog.compact("[ERROR] " + errorMsg));
            } else {
                System.out.println(SysLog.compact("[WARN] Optional config resource not found: " + location));
            }
            return;
        }

        loadPropertiesFromResource(resource, location);
    }

    /**
     * 解析 Resource
     */
    private Resource resolveResource(String location) {
        try {
            // file: 前缀
            if (location.startsWith(FILE_PREFIX)) {
                String filePath = location.substring(FILE_PREFIX.length());
                return resourceLoader.getResource(FILE_PREFIX + filePath);
            }

            // classpath: 前缀
            if (location.startsWith(CLASSPATH_PREFIX)) {
                String classPath = location.substring(CLASSPATH_PREFIX.length());
                return resourceLoader.getResource(CLASSPATH_PREFIX + classPath);
            }

            // 无前缀 - 默认作为文件路径处理
            return resourceLoader.getResource(FILE_PREFIX + location);

        } catch (Exception e) {
            System.err.println(SysLog.compact("[ERROR] Failed to resolve resource: " + location + ", cause: " + e.getMessage()));
            return null;
        }
    }

    /**
     * 从 Resource 加载配置文件
     */
    private void loadPropertiesFromResource(Resource resource, String location) {
        try (InputStream input = resource.getInputStream()) {
            Properties props = new Properties();

            // 判断文件类型
            if (location.endsWith(".yml") || location.endsWith(".yaml")) {
                // YAML 格式
                props = loadYamlProperties(resource, location);
            } else {
                // Properties 格式
                props.load(input);
                PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource(location, props);
                propertyResolvers.add(propertiesPropertySource);
            }
            loadedConfigs.put(location, props);
            System.out.println(SysLog.compact("[INFO] Loaded config from: " + location));

        } catch (IOException e) {
            String errorMsg = "Failed to load config from: " + location + ", cause: " + e.getMessage();
            failedResources.add(errorMsg);
            System.err.println(SysLog.compact("[ERROR] " + errorMsg));
        }
    }

    /**
     * 加载 YAML 文件为 Properties（简化版）
     * 注：实际项目中应使用 yaml 解析库如 SnakeYAML
     */
    private Properties loadYamlProperties(Resource resource, String location) throws IOException {
        YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
        List<PropertySource<?>> load = yamlPropertySourceLoader.load(location, resource);
        PropertySource<?> propertySource = ListTs.get(load, 0);
        if (propertySource != null) propertyResolvers.add(propertySource);
        return PropertySourceConverter.toProperties(propertySource);
    }

    /**
     * 递归处理 spring.config.import
     */
    private void processImports(List<String> importLocations, Set<String> importedSet) {
        for (String importLocation : importLocations) {
            importLocation = importLocation.trim();

            // 检查是否已导入（防止循环导入）
            if (importedSet.contains(importLocation)) {
                System.out.println(SysLog.compact("[WARN] Circular import detected: " + importLocation));
                continue;
            }
            importedSet.add(importLocation);

            // 解析前缀
            boolean isOptional = importLocation.startsWith(OPTIONAL_PREFIX);
            if (isOptional) {
                importLocation = importLocation.substring(OPTIONAL_PREFIX.length()).trim();
            }

            // spring.config.import 只支持 file: 和 classpath:
            if (!importLocation.startsWith(FILE_PREFIX) && !importLocation.startsWith(CLASSPATH_PREFIX)) {
                String errorMsg = "spring.config.import only supports 'file:' and 'classpath:' prefixes, got: " + importLocation;
                System.err.println(SysLog.compact("[ERROR] " + errorMsg));
                continue;
            }

            Resource resource = resolveResource(importLocation);

            if (resource == null || !resource.exists()) {
                String errorMsg = "Import resource not found: " + importLocation;
                failedResources.add(errorMsg);

                if (!isOptional) {
                    System.err.println(SysLog.compact("[ERROR] " + errorMsg));
                } else {
                    System.out.println(SysLog.compact("[WARN] Optional import resource not found: " + importLocation));
                }
                continue;
            }

            loadPropertiesFromResource(resource, importLocation);

            // 如果导入的文件中还有 spring.config.import，递归处理
            Properties importedProps = loadedConfigs.get(importLocation);
            if (importedProps != null && importedProps.containsKey(CONFIG_IMPORT_PARAM)) {
                String nestedImports = importedProps.getProperty(CONFIG_IMPORT_PARAM);
                processImports(parseLocations(nestedImports), importedSet);
            }
        }
    }

    /**
     * 合并所有配置
     * 后加载的配置覆盖先加载的配置
     */
    private Properties mergeConfigs() {
        Properties merged = new Properties();

        for (Properties props : loadedConfigs.values()) {
            merged.putAll(props);
        }

        return merged;
    }

    /**
     * 获取加载的配置源（调试用）
     */
    public Map<String, Properties> getLoadedConfigs() {
        return new LinkedHashMap<>(loadedConfigs);
    }

    /**
     * 获取失败的资源列表
     */
    public Set<String> getFailedResources() {
        return new HashSet<>(failedResources);
    }

    /**
     * 打印加载摘要
     */
    public void printLoadSummary() {
        if (!loadedConfigs.isEmpty()) {
            System.out.println(SysLog.compact("loaded sources (" + loadedConfigs.size() + "):"));
            loadedConfigs.forEach((location, props) -> {
                System.out.println("  ✓ " + location + " (" + props.size() + " properties)");
            });
        }
        if (!failedResources.isEmpty()) {
            System.out.println(SysLog.compact("failed resources (" + failedResources.size() + "):"));
            failedResources.forEach(resource -> System.out.println("  ✗ " + resource));
        }
    }
}
