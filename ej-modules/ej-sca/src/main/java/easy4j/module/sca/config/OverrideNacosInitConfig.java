package easy4j.module.sca.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.google.common.collect.Maps;
import easy4j.module.base.properties.EjSysFieldInfo;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.resolve.BootStrapSpecialVsResolve;
import easy4j.module.base.resolve.DataSourceUrlResolve;
import easy4j.module.base.resolve.NacosUrlResolve;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.*;

/**
 * 晚于
 * 启动的时候重载远程加载的数据
 * @see ConfigDataEnvironmentPostProcessor
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER + 1)
public class OverrideNacosInitConfig extends AbstractEnvironmentForEj {

    public static final String SCA_OVERRIDE_ENV = "sca-env-overide-nacos-config";

    @Override
    public String getName() {
        return SCA_OVERRIDE_ENV;
    }

    @Override
    public Properties getProperties() {
        return null;
    }



    /**
     * 走到这里来说明 nacos配置基本没问题 参数正常获取
     *
     * 系统参数和spring参数有一层对照
     * 参数的解析是分散再各个服务解析的 比如数据库，nacos等
     * 但是有些特殊的参数是只有从配置中提前读取然后再转换才生效了的具体转换逻辑在，如果把所有的参数都丢到nacos配置中心的话 那么那些参数是没有经过转化的
     *
     *
     * @param environment
     * @param application
     */
    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        List<EjSysFieldInfo> allEjSysFieldInfoList = EjSysFieldInfo.getAllEjSysInfoList();
        EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
        String dataIds = ejSysProperties.getDataIds();
        for (String dataId : ListTs.asList(dataIds.split(StringPool.COMMA))) {
            String nacosGroup = getGroup(dataId,ejSysProperties.getNacosGroup());
            String dataIds2 = getDataId(dataId);
            String nacosPropertiesResourceName = nacosGroup+"@"+dataIds2;
            log.info(SysLog.compact("begin override nacos init remote config:"+ nacosPropertiesResourceName));
            MutablePropertySources propertySources = environment.getPropertySources();
            PropertySource<?> propertySource = propertySources.get(nacosPropertiesResourceName);
            if(Objects.isNull(propertySource)){
                return;
            }
            Map<String,Object> mapPropertiesResource = Maps.newHashMap();
            allEjSysFieldInfoList.forEach(ejSysFieldInfo -> {
                String sysConstantName = ejSysFieldInfo.getSysConstantName();
                Object property = propertySource.getProperty(sysConstantName);
                if(ObjectUtil.isNotEmpty(property)){
                    mapPropertiesResource.put(sysConstantName,property);
                }
            });

            if(CollUtil.isNotEmpty(mapPropertiesResource)){

                BootStrapSpecialVsResolve bootStrapSpecialVsResolve = new BootStrapSpecialVsResolve();
                bootStrapSpecialVsResolve.handler(mapPropertiesResource,null);

                log.info(SysLog.compact("success override nacos config keys:"+ mapPropertiesResource.keySet().size()));
                OriginTrackedMapPropertySource originTrackedMapPropertySource = new OriginTrackedMapPropertySource(getName(),mapPropertiesResource,true);

                propertySources.addBefore(nacosPropertiesResourceName,originTrackedMapPropertySource);
            }
        }

    }
}
