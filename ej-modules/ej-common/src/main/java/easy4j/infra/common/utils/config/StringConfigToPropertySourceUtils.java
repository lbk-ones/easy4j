package easy4j.infra.common.utils.config;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 将字符串格式的properties/yml解析为Spring PropertySource的工具类
 * @author bokun.li
 * @date 2025/10/22
 */
public class StringConfigToPropertySourceUtils {

    /**
     * 将properties格式的字符串解析为PropertySource
     *
     * @param sourceName PropertySource的名称（自定义，如"my-properties-source"）
     * @param propertiesStr properties格式的字符串（如"user.name=test\nuser.age=18"）
     * @return PropertySource实例
     * @throws IOException 解析失败时抛出
     */
    public static PropertySource<?> parsePropertiesString(String sourceName, String propertiesStr) {
        try{
            // 将字符串转为Spring的Resource（ByteArrayResource）
            Resource resource = new ByteArrayResource(
                    propertiesStr.getBytes(StandardCharsets.UTF_8),
                    "Properties string resource"
            );
            EncodedResource encodedResource = new EncodedResource(resource,StandardCharsets.UTF_8);
            // 加载为Properties对象
            Properties properties = PropertiesLoaderUtils.loadProperties(encodedResource);
            // 转为Map（Properties继承Hashtable，可直接转换）
            Map<String, Object> propsMap = properties.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().toString(),
                            Map.Entry::getValue
                    ));
            // 封装为MapPropertySource（也可用PropertiesPropertySource，两者类似）
            return new MapPropertySource(sourceName, propsMap);
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    /**
     * 将yml格式的字符串解析为PropertySource
     *
     * @param sourceName PropertySource的名称
     * @param ymlStr yml格式的字符串（如"user:\n  name: test\n  age: 18"）
     * @return PropertySource实例
     */
    public static PropertySource<?> parseYmlString(String sourceName, String ymlStr) {
        Resource resource = new ByteArrayResource(
                ymlStr.getBytes(StandardCharsets.UTF_8),
                "Properties string resource"
        );
        YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
        try {
            EncodedResource encodedResource = new EncodedResource(resource, StandardCharsets.UTF_8);
            return yamlPropertySourceLoader.load(sourceName, resource).get(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自动识别是否是yml和properties 然后自动解析 并返回 PropertySource
     * @param sourceName PropertySource的名称
     * @param str 要解析的字符串
     * @return
     */
    public static PropertySource<?> autoParse(String sourceName, String str){
        PropertySource<?> source = null;
        String detect = ConfigFormatDetector.detect(str);
        if(StrUtil.equals(detect,"properties")){
            source = parsePropertiesString(sourceName,str);
        }else if(StrUtil.equals(detect,"yml")){
            source = parseYmlString(sourceName,str);
        }
        return source;

    }

    /**
     * 将 propertySource 转为 Map<String,String>
     * @param propertySource 要转换的propertySource
     * @return
     */
    public static Map<String,Object> toMap(PropertySource<?> propertySource){

        Map<String,Object> res = Maps.newHashMap();
        if(null == propertySource) return res;
        Object source = propertySource.getSource();
        if(source instanceof Map){
            Map<?, ?> source1 = (Map<?, ?>) source;
            for (Map.Entry<?, ?> entry : source1.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                res.put(key.toString(),value.toString());
            }
        }
        return res;
    }

    /*public static void main(String[] args) {
        PropertySource<?> propertySource = autoParse("xx", "server:\n" +
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
                "\n");
        Object source = propertySource.getSource();
        if(source instanceof Map){
            Map<?, ?> source1 = (Map<?, ?>) source;
            for (Map.Entry<?, ?> entry : source1.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                System.out.println(key+"="+value);
            }
        }
        System.out.println(propertySource.getProperty("knife4j.nacos.routes[0].name"));
        System.out.println(propertySource.getProperty("knife4j.nacos.routes[0].service-name"));
    }*/
}