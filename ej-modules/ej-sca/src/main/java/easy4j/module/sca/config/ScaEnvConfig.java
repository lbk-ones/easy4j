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
            EjSysProperties ejSysProperties = Easy4j.getEjSysProperties();
            String serverName = ejSysProperties.getServerName();
            if (StrUtil.isBlank(serverName)) {
                log.info(SysConstant.EASY4J_SERVER_NAME + "is not set,so skip sca config setting....");
                return null;
            }
            String env = ejSysProperties.getEnv();
            String nacosConfigFileExtension = StrUtil.blankToDefault(ejSysProperties.getNacosConfigFileExtension(), "properties");
            String dataids = "optional:nacos:" + serverName;
            if (StrUtil.isNotBlank(env)) {
                dataids += "-" + env + "." + nacosConfigFileExtension;
            } else {
                dataids += "." + nacosConfigFileExtension;
            }
            List<String> list = ListTs.asList(dataids.split(StringPool.COLON));
            properties.setProperty(SysConstant.EASY4J_NACOS_DATA_IDS, ListTs.get(list, list.size() - 1, ""));
            properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT, dataids);

            String nacosUrl = ejSysProperties.getNacosUrl();
            if (StrUtil.isNotBlank(nacosUrl)) {
                List<String> split = ListTs.asList(nacosUrl.split(StringPool.AT));
                String url = ListTs.get(split, 0);
                if (StrUtil.isBlank(url)) {
                    throw new RuntimeException("nacos url format is error !" + nacosUrl);
                }
                List<String> up = ListTs.asList(split.get(1).split(":"));
                String username = ListTs.get(up, 0, "");
                String password = ListTs.get(up, 1, "");
                properties.setProperty(SysConstant.EASY4J_SCA_NACOS_URL, url);
                properties.setProperty(SysConstant.EASY4J_SCA_NACOS_USERNAME, username);
                properties.setProperty(SysConstant.EASY4J_SCA_NACOS_PASSWORD, password);
            }
            String nacosGroup = StrUtil.blankToDefault(ejSysProperties.getNacosGroup(), "DEFAULT_GROUP");
            String nacosNameSpace = StrUtil.blankToDefault(ejSysProperties.getNacosNameSpace(), "public");
            String nacosConfigUrl = ejSysProperties.getNacosConfigUrl();
            setProperties(properties, SysConstant.EASY4J_SCA_NACOS_CONFIG_URL, nacosConfigUrl);
            String nacosConfigUsername = ejSysProperties.getNacosConfigUsername();
            setProperties(properties, SysConstant.EASY4J_SCA_NACOS_CONFIG_USERNAME, nacosConfigUsername);
            String configPassword = ejSysProperties.getNacosConfigPassword();
            setProperties(properties, SysConstant.EASY4J_SCA_NACOS_CONFIG_PASSWORD, configPassword);
            String configGroup = ejSysProperties.getNacosConfigGroup();
            properties.setProperty(SysConstant.EASY4J_SCA_NACOS_CONFIG_GOURP, StrUtil.blankToDefault(configGroup, nacosGroup));
            String configNamespace = ejSysProperties.getNacosConfigNamespace();
            properties.setProperty(SysConstant.EASY4J_SCA_NACOS_CONFIG_NAMESPACE, StrUtil.blankToDefault(configNamespace, nacosNameSpace));
            String nacosDiscoveryUrl = ejSysProperties.getNacosDiscoveryUrl();
            setProperties(properties, SysConstant.EASY4J_SCA_NACOS_DISCOVERY_URL, nacosDiscoveryUrl);
            String discoveryUsername = ejSysProperties.getNacosDiscoveryUsername();
            setProperties(properties, SysConstant.EASY4J_SCA_NACOS_DISCOVERY_USERNAME, discoveryUsername);
            String discoveryPassword = ejSysProperties.getNacosDiscoveryPassword();
            setProperties(properties, SysConstant.EASY4J_SCA_NACOS_DISCOVERY_PASSWORD, discoveryPassword);
            String discoveryGroup = ejSysProperties.getNacosDiscoveryGroup();
            properties.setProperty(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_GROUP, StrUtil.blankToDefault(discoveryGroup, nacosGroup));
            String discoveryNamespace = ejSysProperties.getNacosDiscoveryNamespace();
            properties.setProperty(SysConstant.EASY4J_SCA_NACOS_DISCOVERY_NAMESPACE, StrUtil.blankToDefault(discoveryNamespace, nacosGroup));
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
