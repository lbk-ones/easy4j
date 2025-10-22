package easy4j.infra.knife.nacos.aggregation;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.properties.EjSysFieldInfo;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.properties.Knife4jRouter;
import easy4j.infra.base.properties.cc.ConfigCenterFactory;
import easy4j.infra.base.resolve.BootStrapSpecialVsResolve;
import easy4j.infra.base.starter.env.AbstractEasy4jEnvironment;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.common.utils.config.NacosConfigClient;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * api文档聚合相关参数配置
 *
 * @author bokun.li
 * @date 2025/10/22
 */
public class DocNacosAggEnvironment extends AbstractEasy4jEnvironment {

    @Override
    public String getName() {
        return "easy4j-knife4j-nacos-aggregation";
    }

    /**
     * knife4j.enable-aggregation=true
     * knife4j.nacos.enable=true
     * knife4j.nacos.service-url=http://localhost:8848/nacos
     * knife4j.nacos.service-auth.enable=true
     * knife4j.nacos.service-auth.username=nacos
     * knife4j.nacos.service-auth.password=nacos
     * knife4j.nacos.routes[0].name=元数据
     * knife4j.nacos.routes[0].service-name=dataspace-metadata
     * knife4j.nacos.routes[0].location=/v3/api-docs
     * knife4j.nacos.routes[0].service-path=/
     * knife4j.nacos.routes[0].namespace-id=develop
     * knife4j.nacos.routes[0].group-name=dataspace-service
     * knife4j.nacos.routes[1].name=字典
     * knife4j.nacos.routes[1].serviceName=dataspace-dict
     * knife4j.nacos.routes[1].location=/v3/api-docs
     * knife4j.nacos.routes[1].service-path=/
     * knife4j.nacos.routes[1].namespace-id=develop
     * knife4j.nacos.routes[1].group-name=dataspace-service
     */
    @Override
    public Properties getProperties() {

        return null;
    }

    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> properties = prepare();
        overrideEnv(environment, properties);
    }

    @NotNull
    private Map<String, Object> prepare() {
        String envProperty = getEnvProperty(SysConstant.EASY4J_SCA_NACOS_URL);
        String username = getEnvProperty(SysConstant.EASY4J_SCA_NACOS_USERNAME);
        String password = getEnvProperty(SysConstant.EASY4J_SCA_NACOS_PASSWORD);
        String namespace = getEnvProperty(SysConstant.EASY4J_NACOS_NAMESPACE);
        String group = getEnvProperty(SysConstant.EASY4J_NACOS_GROUP);
        String configGroup = getEnvProperty(SysConstant.EASY4J_SCA_NACOS_CONFIG_GOURP);
        String dataIds = getEnvProperty(SysConstant.EASY4J_NACOS_DATA_IDS);

        String username1 = StrUtil.blankToDefault(getUsername(envProperty), username);
        String password1 = StrUtil.blankToDefault(getPassword(envProperty), password);
        String url = getUrl(envProperty);
        String nacosUrl = "http://" + url + "/nacos";


        EjSysProperties envEjSysProperties = getEnvEjSysProperties();
        Map<String,Object> properties = Maps.newHashMap();
        properties.put("knife4j.enable-aggregation", "true");
        properties.put("knife4j.nacos.enable", "true");
        properties.put("knife4j.nacos.service-url", nacosUrl);
        if (StrUtil.isNotBlank(username1) && StrUtil.isNotBlank(password1)) {
            properties.put("knife4j.nacos.service-auth.enable", "true");
            properties.put("knife4j.nacos.service-auth.username", username1);
            properties.put("knife4j.nacos.service-auth.password", password1);
        }
        List<Knife4jRouter> knife4jRouters = envEjSysProperties.getKnife4jNacosRouters();
        List<Knife4jRouter> newKnife4jRouters = ListTs.newList();
        for (String normalDataId : ListTs.asList(dataIds.split(SP.COMMA))) {
            String dataId = getDataId(normalDataId);
            Map<String, Object> configMap = new NacosConfigClient(nacosUrl, username1, password1)
                    .getConfigMap(dataId, StrUtil.blankToDefault(configGroup, group), namespace, null);
            Map<String, String> strMap = toMapStringString(configMap);
            ConfigCenterFactory.get().change(strMap);
            BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
            bootStrapSpecialVsResolve.handler(configMap, null);
            for (String s : configMap.keySet()) {
                Object s2 = configMap.get(s);
                if (null != s2) {
                    properties.put(s, s2);
                    if ((StrUtil.startWith(s, "easy4j.knife4j-nacos-routers") || StrUtil.startWith(s, "easy4j.knife4jNacosRouters")) && s.contains("].path")) {
                        Knife4jRouter knife4jRouter = new Knife4jRouter();
                        knife4jRouter.setPath(s2.toString());
                        newKnife4jRouters.add(knife4jRouter);
                    }
                }
            }
        }

        if (ListTs.isNotEmpty(newKnife4jRouters)) {
            knife4jRouters = newKnife4jRouters;
        }
        Set<String> objects = new HashSet<>();
        if (ListTs.isNotEmpty(knife4jRouters)) {
            int i = 0;
            for (Knife4jRouter knife4jRouter : knife4jRouters) {
                String path = knife4jRouter.getPath();
                RoutePathInfo routePathInfo = parseRoutePath(path);
                if (null != routePathInfo) {
                    routePathInfo.setNameSpaceId(StrUtil.blankToDefault(namespace, routePathInfo.getNameSpaceId()));
                    routePathInfo.setGroupName(StrUtil.blankToDefault(group, routePathInfo.getGroupName()));
                    String displayName = routePathInfo.getDisplayName();
                    String serviceName = routePathInfo.getServiceName();
                    if (!StrUtil.isAllNotBlank(displayName, serviceName)) {
                        continue;
                    }
                    if (objects.contains(serviceName)) {
                        continue;
                    }
                    objects.add(serviceName);
                    properties.put("knife4j.nacos.routes[" + i + "].name", displayName);
                    properties.put("knife4j.nacos.routes[" + i + "].service-name", serviceName);
                    properties.put("knife4j.nacos.routes[" + i + "].location", routePathInfo.getLocationUrl());
                    if (StrUtil.isNotBlank(routePathInfo.getContextPath())) {
                        properties.put("knife4j.nacos.routes[" + i + "].service-path", routePathInfo.getContextPath());
                    }
                    if (StrUtil.isNotBlank(routePathInfo.getNameSpaceId())) {
                        properties.put("knife4j.nacos.routes[" + i + "].namespace-id", routePathInfo.getNameSpaceId());
                    }
                    if (StrUtil.isNotBlank(routePathInfo.getGroupName())) {
                        properties.put("knife4j.nacos.routes[" + i + "].group-name", routePathInfo.getGroupName());
                    }
                    String cluster = routePathInfo.getCluster();
                    if (StrUtil.isNotBlank(cluster)) {
                        properties.put("knife4j.nacos.routes[" + i + "].clusters", cluster);
                    }
                    i++;
                }
            }
        }
        return properties;
    }


    private void overrideEnv(ConfigurableEnvironment environment, Map<String, Object> properties) {
        List<EjSysFieldInfo> allEjSysFieldInfoList = EjSysFieldInfo.getAllEjSysInfoList();
        Map<String, EjSysFieldInfo> stringEjSysFieldInfoMap = ListTs.mapOne(allEjSysFieldInfoList, EjSysFieldInfo::getSysConstantName);
        Set<String> strings = properties.keySet();
        Map<String,Object> sysMap = Maps.newHashMap();
        Map<String,Object> noSysMap = Maps.newHashMap();
        for (String key : strings) {
            Object o = properties.get(key);
            EjSysFieldInfo ejSysFieldInfo = stringEjSysFieldInfoMap.get(key);
            if(null == ejSysFieldInfo){
                noSysMap.put(key,o);
            }else{
                sysMap.put(key,o);
            }
        }
        MutablePropertySources propertySources = environment.getPropertySources();
        if(!sysMap.isEmpty()){
            System.out.println(SysLog.compact("success override nacos sys config keys:" + sysMap.keySet().size()));
            OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(getName(), sysMap, true);
            propertySources.addAfter(FIRST_ENV_NAME, originTrackedMapPropertySource);
        }
        if(!noSysMap.isEmpty()){
            System.out.println(SysLog.compact("success override nacos nonsys config keys:" + sysMap.keySet().size()));
            OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(getName()+"_2", noSysMap, true);
            propertySources.addBefore(FIRST_ENV_NAME, originTrackedMapPropertySource);
        }
    }


    // 服务名?displayName::xx&locationUrl::/v3/api-docs&contextPath::xxx&nameSpaceId::xxx&groupName::&xxx&cluster::xxx
    private RoutePathInfo parseRoutePath(String path) {
        if (StrUtil.isBlank(path)) return null;
        RoutePathInfo routePathInfo = new RoutePathInfo();
        String[] split = path.split("\\?");
        String serviceName = ListTs.get(split, 0);
        routePathInfo.setServiceName(serviceName);
        String serviceNameSuffix = ListTs.get(split, 1);
        if (StrUtil.isNotBlank(serviceNameSuffix)) {
            String[] split1 = serviceNameSuffix.split("&");
            for (String s : split1) {
                String[] split2 = s.split("::");
                String name = ListTs.get(split2, 0);
                String value = ListTs.get(split2, 1);
                if (StrUtil.isAllNotBlank(name, value)) {
                    Field field = ReflectUtil.getField(RoutePathInfo.class, name);
                    if (null != field) {
                        ReflectUtil.setFieldValue(routePathInfo, field, value);
                    }
                }
            }
        }
        return routePathInfo;
    }


    public Map<String, String> toMapStringString(Map<String, Object> configMap) {
        Map<@Nullable String, @Nullable String> res = Maps.newHashMap();
        if (configMap == null) return res;
        for (String s : configMap.keySet()) {
            Object o = configMap.get(s);
            if (o != null) {
                res.put(s, o.toString());
            }
        }
        return res;
    }



    @Data
    public static class RoutePathInfo {
        private String serviceName;
        private String displayName;
        private String locationUrl = "/v3/api-docs";
        private String contextPath = "/";
        private String nameSpaceId = "public";
        private String groupName = "DEFAULT_GROUP";
        private String cluster;
    }
}
