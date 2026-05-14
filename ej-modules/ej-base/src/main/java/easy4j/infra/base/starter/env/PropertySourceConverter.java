package easy4j.infra.base.starter.env;

import org.springframework.core.env.*;
import java.util.*;

/**
 * PropertySource 转换工具类
 * 
 * 功能：
 * 1. 将单个 PropertySource 转换为 Properties
 * 2. 将 PropertySources 集合转换为 Properties（支持优先级）
 * 3. 支持各种 PropertySource 实现类
 * 4. 处理 EnumerablePropertySource 和非 Enumerable 的情况
 */
public class PropertySourceConverter {

    public static Map<String,String> toMap(PropertySource<?> propertySource){
        if (propertySource == null) {
            return new HashMap<>();
        }

        Map<String,String> properties = new HashMap<>();

        // 判断是否是 EnumerablePropertySource
        if (propertySource instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) propertySource;
            String[] propertyNames = enumerable.getPropertyNames();

            if (propertyNames != null) {
                for (String propertyName : propertyNames) {
                    Object value = enumerable.getProperty(propertyName);
                    if (value != null) {
                        properties.put(propertyName, value.toString());
                    }
                }
            }
        }
        // MapPropertySource 的特殊处理
        else if (propertySource instanceof MapPropertySource mapSource) {
            Map<String, Object> source = (Map<String, Object>) mapSource.getSource();
            if (source != null) {
                for (Map.Entry<String, Object> entry : source.entrySet()) {
                    if (entry.getValue() != null) {
                        properties.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        }
        // Properties 源的特殊处理
        else if (propertySource.getSource() instanceof Properties sourceProps) {
            for (Object keyO : sourceProps.keySet()) {
                String key = keyO.toString();
                String v = sourceProps.getProperty(key);
                properties.put(key,v);
            }
        }
        // Map 源的处理
        else if (propertySource.getSource() instanceof Map<?, ?> sourceMap) {
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                if (entry.getValue() != null) {
                    properties.put(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        // 通用处理：尝试获取所有属性
        else {
            Properties properties1 = extractPropertiesGeneric(propertySource);
            for (Object keyO : properties1.keySet()) {
                String key = keyO.toString();
                String v = properties1.getProperty(key);
                properties.put(key,v);
            }
        }
        return properties;
    }
    /**
     * 将单个 PropertySource 转换为 Properties
     * 
     * @param propertySource PropertySource 对象
     * @return Properties 对象
     */
    public static Properties toProperties(PropertySource<?> propertySource) {
        if (propertySource == null) {
            return new Properties();
        }
        
        Properties properties = new Properties();
        
        // 判断是否是 EnumerablePropertySource
        if (propertySource instanceof EnumerablePropertySource) {
            EnumerablePropertySource<?> enumerable = (EnumerablePropertySource<?>) propertySource;
            String[] propertyNames = enumerable.getPropertyNames();
            
            if (propertyNames != null) {
                for (String propertyName : propertyNames) {
                    Object value = enumerable.getProperty(propertyName);
                    if (value != null) {
                        properties.setProperty(propertyName, value.toString());
                    }
                }
            }
        } 
        // MapPropertySource 的特殊处理
        else if (propertySource instanceof MapPropertySource mapSource) {
            Map<String, Object> source = (Map<String, Object>) mapSource.getSource();
            if (source != null) {
                for (Map.Entry<String, Object> entry : source.entrySet()) {
                    if (entry.getValue() != null) {
                        properties.setProperty(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        }
        // Properties 源的特殊处理
        else if (propertySource.getSource() instanceof Properties sourceProps) {
            properties.putAll(sourceProps);
        }
        // Map 源的处理
        else if (propertySource.getSource() instanceof Map<?, ?> sourceMap) {
            for (Map.Entry<?, ?> entry : sourceMap.entrySet()) {
                if (entry.getValue() != null) {
                    properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        // 通用处理：尝试获取所有属性
        else {
            properties = extractPropertiesGeneric(propertySource);
        }
        
        return properties;
    }
    
    /**
     * 将 PropertySources 集合转换为 Properties（支持优先级）
     * 后面的 PropertySource 会覆盖前面的
     * 
     * @param propertySources PropertySources 集合
     * @return 合并后的 Properties 对象
     */
    public static Properties toProperties(PropertySources propertySources) {
        if (propertySources == null) {
            return new Properties();
        }
        
        Properties mergedProperties = new Properties();
        
        // 按顺序遍历 PropertySources，后面的会覆盖前面的
        for (PropertySource<?> source : propertySources) {
            Properties sourceProps = toProperties(source);
            mergedProperties.putAll(sourceProps);
        }
        
        return mergedProperties;
    }
    
    /**
     * 将 PropertySources 集合转换为 Properties（带详细信息）
     * 返回 Map，包含每个 PropertySource 的详细信息
     * 
     * @param propertySources PropertySources 集合
     * @return Map<PropertySource名称, Properties>
     */
    public static Map<String, Properties> toPropertiesMap(PropertySources propertySources) {
        if (propertySources == null) {
            return Collections.emptyMap();
        }
        
        Map<String, Properties> result = new LinkedHashMap<>();
        
        for (PropertySource<?> source : propertySources) {
            Properties props = toProperties(source);
            result.put(source.getName(), props);
        }
        
        return result;
    }
    
    /**
     * 将 PropertySources 转换为 Properties，并输出加载顺序
     * 
     * @param propertySources PropertySources 集合
     * @return 包含属性的 Properties 对象
     */
    public static PropertiesWithMetadata toPropertiesWithMetadata(PropertySources propertySources) {
        PropertiesWithMetadata metadata = new PropertiesWithMetadata();
        
        if (propertySources == null) {
            return metadata;
        }
        
        for (PropertySource<?> source : propertySources) {
            Properties props = toProperties(source);
            metadata.addPropertySource(source.getName(), props);
        }
        
        return metadata;
    }
    
    /**
     * 通用的属性提取方法
     * 用于处理非 EnumerablePropertySource 的情况
     */
    private static Properties extractPropertiesGeneric(PropertySource<?> propertySource) {
        Properties properties = new Properties();
        Object source = propertySource.getSource();

        if (source == null) {
            return properties;
        }

        // 尝试通过反射或其他方式提取属性
        if (source instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) source;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getValue() != null) {
                    properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        
        return properties;
    }
    
    /**
     * 从 Environment 中获取所有 PropertySources 并转换
     * 
     * @param environment Spring Environment 对象
     * @return 合并后的 Properties 对象
     */
    public static Properties fromEnvironment(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return new Properties();
        }
        
        ConfigurableEnvironment configurableEnv = (ConfigurableEnvironment) environment;
        return toProperties(configurableEnv.getPropertySources());
    }
    
    /**
     * 从 Environment 中获取所有 PropertySources 的详细信息
     * 
     * @param environment Spring Environment 对象
     * @return PropertySources 详细信息 Map
     */
    public static Map<String, Properties> fromEnvironmentDetailed(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return Collections.emptyMap();
        }
        
        ConfigurableEnvironment configurableEnv = (ConfigurableEnvironment) environment;
        return toPropertiesMap(configurableEnv.getPropertySources());
    }
    
    /**
     * 内部类：包含元数据的 Properties 容器
     */
    public static class PropertiesWithMetadata {
        private Properties mergedProperties;
        private LinkedHashMap<String, Properties> sourceMap;
        private List<String> loadOrder;
        
        public PropertiesWithMetadata() {
            this.mergedProperties = new Properties();
            this.sourceMap = new LinkedHashMap<>();
            this.loadOrder = new ArrayList<>();
        }
        
        public void addPropertySource(String name, Properties properties) {
            sourceMap.put(name, properties);
            loadOrder.add(name);
            mergedProperties.putAll(properties);
        }
        
        public Properties getMergedProperties() {
            return mergedProperties;
        }
        
        public LinkedHashMap<String, Properties> getSourceMap() {
            return sourceMap;
        }
        
        public List<String> getLoadOrder() {
            return loadOrder;
        }
        
        public void printSummary() {
            System.out.println("\n========== PropertySource Summary ==========");
            System.out.println("Total sources: " + sourceMap.size());
            System.out.println("Total properties: " + mergedProperties.size());
            System.out.println("\nLoad Order:");
            
            for (int i = 0; i < loadOrder.size(); i++) {
                String sourceName = loadOrder.get(i);
                Properties props = sourceMap.get(sourceName);
                System.out.println("  " + (i + 1) + ". " + sourceName + " (" + props.size() + " properties)");
            }
            
            System.out.println("\nMerged Properties:");
            mergedProperties.forEach((key, value) -> {
                System.out.println("  " + key + " = " + value);
            });
            
            System.out.println("==========================================\n");
        }
    }
    
    // ===================== 使用示例 =====================
    
    public static void main(String[] args) {
        // 示例1：创建测试的 PropertySource
        Map<String, Object> map1 = new HashMap<>();
        map1.put("app.name", "MyApp");
        map1.put("server.port", "8080");
        MapPropertySource source1 = new MapPropertySource("source1", map1);
        
        Map<String, Object> map2 = new HashMap<>();
        map2.put("database.url", "jdbc:mysql://localhost:3306/mydb");
        map2.put("database.user", "root");
        MapPropertySource source2 = new MapPropertySource("source2", map2);
        
        // 示例2：单个 PropertySource 转换
        System.out.println("========== 示例1：单个转换 ==========");
        Properties props1 = toProperties(source1);
        props1.forEach((key, value) -> System.out.println(key + " = " + value));
        
        // 示例3：PropertySources 集合转换
        System.out.println("\n========== 示例2：集合转换 ==========");
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(source1);
        sources.addLast(source2);
        
        Properties mergedProps = toProperties(sources);
        mergedProps.forEach((key, value) -> System.out.println(key + " = " + value));
        
        // 示例4：带元数据的转换
        System.out.println("\n========== 示例3：带元数据转换 ==========");
        PropertiesWithMetadata metadata = toPropertiesWithMetadata(sources);
        metadata.printSummary();
        
        // 示例5：详细 Map 转换
        System.out.println("\n========== 示例4：详细 Map 转换 ==========");
        Map<String, Properties> detailMap = toPropertiesMap(sources);
        detailMap.forEach((name, props) -> {
            System.out.println("Source: " + name);
            props.forEach((key, value) -> System.out.println("  " + key + " = " + value));
        });
    }
}
