package io.github.lbkones.nacos.pure;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标准nacos配置中心参数解析
 */
@Data
public class NacosStandProperties {
    private boolean configEnabled;
    private String applicationName;
    private String nacosConfigFileExtension;
    public String nacosConfigUsername;
    public String nacosConfigNamespace;
    public String nacosConfigGroup;
    public String nacosConfigPassword;
    public String nacosConfigServerAddr;
    public boolean cloudRefreshEnabled;
    private List<DataId> dataIds;



    @Data
    public static class DataId {
        private String dataId;
        private String group;
        private Map<String, String> queryMap = new HashMap<>();
    }

    public static String blankToDefault(String text, String dtxt) {
        if (StringUtils.hasText(text)) {
            return text;
        } else {
            return dtxt;
        }
    }

    private static Map<String, String> getQueryMap(String url) {
        int i = url.indexOf("?");
        Map<String, String> map = new HashMap<>();
        if (Math.max(0, url.length() - 1) < i + 1 || i < 0) {
            return map;
        }
        String s1 = StrUtil.subSuf(url, i + 1);
        List<String> split1 = StrUtil.split(s1, "&");
        for (String string : split1) {
            List<String> split2 = StrUtil.split(string, "=");
            String key = get(split2, 0);
            String value = get(split2, 1);
            map.putIfAbsent(key, value);
        }
        return map;
    }

    public static NacosStandProperties parse(Environment environment, SpringApplication application) {
        Class<?> mainApplicationClass = application.getMainApplicationClass();
        boolean annotationPresent = mainApplicationClass.isAnnotationPresent(EnableNacosConfigCenter.class);
        if (!annotationPresent) return null;
        EnableNacosConfigCenter annotation = mainApplicationClass.getAnnotation(EnableNacosConfigCenter.class);
        String prefix = environment.resolvePlaceholders(annotation.dataIdPrefix());
        String suffix = environment.resolvePlaceholders(annotation.dataIdSuffix());
        String applicationName = environment.getProperty(NacosConfigConstants.SPRING_APPLICATION_NAME);
        String addr = environment.getProperty(NacosConfigConstants.NACOS_SERVER_ADDR);
        String userName = environment.getProperty(NacosConfigConstants.NACOS_USERNAME);
        String nacosPassword = environment.getProperty(NacosConfigConstants.NACOS_PASSWORD);
        String configFileExtension = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_FILE_EXTENSION);
        String configNamespace = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_NAMESPACE);
        String nacosConfigUsername = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_USERNAME);
        String nacosConfigPassword = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_PASSWORD);
        String nacosConfigGroup = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_GROUP);
        boolean cloudRefreshEnabled = environment.getProperty(NacosConfigConstants.CLOUD_REFRESHED,Boolean.class,true);
        boolean configEnabled = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_ENABLED, Boolean.class, true);
        nacosConfigGroup = blankToDefault(nacosConfigGroup, "DEFAULT_GROUP");
        String springConfigImport = environment.getProperty(NacosConfigConstants.SPRING_CONFIG_IMPORT);
        String nacosConfigServerAddr = environment.getProperty(NacosConfigConstants.NACOS_CONFIG_SERVER_ADDR);
        List<DataId> dataIds_ = new ArrayList<>();
        if (StringUtils.hasText(springConfigImport)) {
            String[] split = springConfigImport.split(NacosConfigConstants.COMMA);
            ArrayList<String> list = ListUtil.toList(split);
            for (String s : list) {
                if (!StrUtil.contains(s, "nacos:")) {
                    continue;
                }
                if (StrUtil.startWith(s, "optional:")) {
                    s = s.replaceFirst("optional:", "");
                }
                if (StrUtil.startWith(s, "nacos:")) {
                    s = s.replaceFirst("nacos:", "");
                }
                Map<String, String> queryMap = getQueryMap(s);
                List<String> split1 = StrUtil.split(s, "?");
                String dataId = get(split1, 0);
                String group = queryMap.get("group");
                List<String> split2 = StrUtil.split(dataId, ".");
                String s1 = get(split2, 0);
                String s2 = get(split2, 1);
                List<String> objects = new ArrayList<>();
                objects.add(prefix);
                objects.add(s1);
                objects.add(suffix);
                if (s2 != null) {
                    objects.add(".");
                    objects.add(s2);
                }
                String dataIdRes = objects.stream().filter(StringUtils::hasText).collect(Collectors.joining(""));
                DataId dataId1 = new DataId();
                dataId1.setGroup(StrUtil.blankToDefault(group, nacosConfigGroup));
                dataId1.setDataId(dataIdRes);
                dataId1.setQueryMap(queryMap);
                dataIds_.add(dataId1);
            }
            if (dataIds_.isEmpty()) {
                defaultDataId(prefix + applicationName + suffix, configFileExtension, nacosConfigGroup, dataIds_);
            }
        } else {
            defaultDataId(prefix + applicationName + suffix, configFileExtension, nacosConfigGroup, dataIds_);
        }
        NacosStandProperties nacosStandProperties = new NacosStandProperties();
        nacosStandProperties.setCloudRefreshEnabled(cloudRefreshEnabled);
        nacosStandProperties.setConfigEnabled(configEnabled);
        nacosStandProperties.setNacosConfigFileExtension(configFileExtension);
        nacosStandProperties.setApplicationName(applicationName);
        nacosStandProperties.setDataIds(dataIds_);
        nacosStandProperties.setNacosConfigServerAddr(StrUtil.blankToDefault(nacosConfigServerAddr, addr));
        nacosStandProperties.setNacosConfigNamespace(StrUtil.blankToDefault(configNamespace, "public"));
        nacosStandProperties.setNacosConfigGroup(nacosConfigGroup);
        nacosStandProperties.setNacosConfigUsername(StrUtil.blankToDefault(nacosConfigUsername, userName));
        nacosStandProperties.setNacosConfigPassword(StrUtil.blankToDefault(nacosConfigPassword, nacosPassword));
        return nacosStandProperties;
    }

    private static void defaultDataId(String applicationName, String configFileExtension, String nacosConfigGroup, List<DataId> dataIds_) {
        String s = applicationName;
        if (StringUtils.hasText(configFileExtension)) {
            s += "." + configFileExtension;
        }
        DataId dataId = new DataId();
        dataId.setGroup(nacosConfigGroup);
        dataId.setDataId(s);
        dataIds_.add(dataId);
    }

    public static <T> T get(Iterable<T> collection, int index) {

        if (CollUtil.isNotEmpty(collection)) {
            Iterator<T> iterator = collection.iterator();
            int i = 0;
            if (collection instanceof Collection) {
                Collection<T> col = ((Collection<T>) collection);
                int size = col.size();
                if (size == 0 || size - 1 < index) {
                    return null;
                }
            }
            while (iterator.hasNext()) {
                T next = iterator.next();
                if (i == index) {
                    return next;
                }
                i++;
            }
        }
        return null;
    }
}
