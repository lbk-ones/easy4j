package easy4j.module.sca.config;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.AbstractEnvironmentForEj;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.SysConstant;
import easy4j.module.base.utils.SysLog;
import jodd.util.StringPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Properties;

/**
 * 晚于
 *
 * @see easy4j.module.base.starter.Easy4jEnvironmentFirst
 */
@Slf4j
@Order(value = ConfigDataEnvironmentPostProcessor.ORDER - 1)
public class ScaEnvConfig extends AbstractEnvironmentForEj {

    public static final String SCA_ENV = "sca-env-config";

    @Override
    public String getName() {
        return SCA_ENV;
    }

    @Override
    public Properties getProperties() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try {
            classLoader.loadClass("easy4j.module.boot.sca.Enable");

            Properties properties = new Properties();
            EjSysProperties ejSys = Easy4j.getEjSysProperties();
            String serverName = ejSys.getServerName();
            if (StrUtil.isBlank(serverName)) {
                log.info(SysLog.compact(SysConstant.EASY4J_SERVER_NAME + "is not set,so skip sca config setting...."));
                return null;
            }
            String env = ejSys.getEnv();
            String nacosConfigFileExtension = StrUtil.blankToDefault(ejSys.getNacosConfigFileExtension(), "properties");
            String dataids = "nacos:" + serverName;
            if(!ejSys.isNacosConfigStrict()){
                dataids = "optional:"+dataids;
            }
            if (StrUtil.isNotBlank(env)) {
                dataids += StringPool.DASH + env + StringPool.DOT + nacosConfigFileExtension;
            } else {
                dataids += StringPool.DOT + nacosConfigFileExtension;
            }
            List<String> list = ListTs.asList(dataids.split(StringPool.COLON));
            properties.setProperty(SysConstant.EASY4J_NACOS_DATA_IDS, ListTs.get(list, list.size() - 1, ""));
            properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT, dataids);

            String nacosUrl = ejSys.getNacosUrl();
            String username = "";
            String password = "";
            if (StrUtil.isNotBlank(nacosUrl)) {
                List<String> split = ListTs.asList(nacosUrl.split(StringPool.AT));
                String url = ListTs.get(split, 0);
                if (StrUtil.isBlank(url)) {
                    throw new RuntimeException("nacos url format is error !" + nacosUrl);
                }
                List<String> up = ListTs.asList(split.get(1).split(":"));
                username = ListTs.get(up, 0, "");
                password = ListTs.get(up, 1, "");
                setPropertiesArr(properties,ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_URL),url);
                setPropertiesArr(properties,ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_USERNAME), username);
                setPropertiesArr(properties,ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_PASSWORD), password);
            }
            String nacosGroup = StrUtil.blankToDefault(ejSys.getNacosGroup(), "DEFAULT_GROUP");
            String nacosNameSpace = StrUtil.blankToDefault(ejSys.getNacosNameSpace(), "public");
            String nacosConfigUrl = ejSys.getNacosConfigUrl();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_URL), nacosConfigUrl);
            String nacosConfigUsername = ejSys.getNacosConfigUsername();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_USERNAME), nacosConfigUsername);
            String configPassword = ejSys.getNacosConfigPassword();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_PASSWORD), configPassword);
            String configGroup = ejSys.getNacosConfigGroup();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_GOURP), StrUtil.blankToDefault(configGroup, nacosGroup));
            String configNamespace = ejSys.getNacosConfigNamespace();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_CONFIG_NAMESPACE), StrUtil.blankToDefault(configNamespace, nacosNameSpace));
            String nacosDiscoveryUrl = ejSys.getNacosDiscoveryUrl();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_URL), nacosDiscoveryUrl);
            String discoveryUsername = ejSys.getNacosDiscoveryUsername();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_USERNAME), StrUtil.blankToDefault(discoveryUsername,username));
            String discoveryPassword = ejSys.getNacosDiscoveryPassword();
            setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_PASSWORD), StrUtil.blankToDefault(discoveryPassword,password));
            String discoveryGroup = ejSys.getNacosDiscoveryGroup();
            setPropertiesArr(properties,ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_GROUP), StrUtil.blankToDefault(discoveryGroup, nacosGroup));
            String discoveryNamespace = ejSys.getNacosDiscoveryNamespace();
            setPropertiesArr(properties,ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_NAMESPACE), StrUtil.blankToDefault(discoveryNamespace, nacosGroup));
            return properties;
        } catch (ClassNotFoundException e) {
            System.out.println(SysLog.compact("未引用sca-starter模块sca配置不生效!"));
        }

        return null;
    }


    @Override
    public void handlerEnvironMent(ConfigurableEnvironment environment, SpringApplication application) {
        log.info(SysLog.compact(SCA_ENV + "已初始化。。"));

    }
}
