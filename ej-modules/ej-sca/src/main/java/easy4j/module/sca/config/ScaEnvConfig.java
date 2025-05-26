package easy4j.module.sca.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import easy4j.module.base.properties.EjSysProperties;
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

    /**
     * 通过 Easy4jEnvironmentFirst 将数据加载进 EjSysProperties
     * 然后这里再将 EjSysProperties的数据转为 nacos 的配置
     * @return
     */
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
            String dataIds = ejSys.getDataIds();
            String env = ejSys.getEnv();
            String nacosGroup = StrUtil.blankToDefault(ejSys.getNacosGroup(), "DEFAULT_GROUP");
            String nacosGroup2 = ejSys.getNacosConfigGroup();
            boolean nacosConfigStrict = ejSys.isNacosConfigStrict();
            String nacosConfigFileExtension = StrUtil.blankToDefault(ejSys.getNacosConfigFileExtension(), "properties");
            // 手动指定dataId
            if(StrUtil.isNotBlank(dataIds)){
                List<String> list1 = ListTs.asList(dataIds.split(StringPool.COMMA));
                List<String> dataids = ListTs.newArrayList();
                for (int i = 0; i < list1.size(); i++) {
                    String e = list1.get(i);
                    String dataId = getDataId(e);
                    String group = getGroup(e,null);
                    String configImport = "nacos:";
                    if(!nacosConfigStrict){
                        configImport = "optional:";
                    }
                    if (StrUtil.isNotBlank(env)) {
                        // 无组要加后缀
                        String s = dataId+StringPool.DASH + env + StringPool.DOT + nacosConfigFileExtension;
                        // 有组则原样返回
                        if(StrUtil.isNotBlank(group)){
                            s = e;
                        }
                        dataids.add(s);
                        configImport += s;
                    } else {
                        if(StrUtil.isBlank(group)){
                            configImport += StringPool.DOT + nacosConfigFileExtension;
                        }else{
                            configImport+= e;
                        }
                    }
                    properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT+"["+i+"]", configImport);
                }
                properties.setProperty(SysConstant.EASY4J_NACOS_DATA_IDS, String.join(StringPool.COMMA,dataids));
            }else{
                // 如果没指定那么就以 serverName 为准进行推算
                String dataids = "nacos:";
                if(!nacosConfigStrict){
                    dataids = "optional:";
                }
                if (StrUtil.isNotBlank(env)) {
                    dataids += serverName + StringPool.DASH + env + StringPool.DOT + nacosConfigFileExtension;
                } else {
                    dataids += serverName + StringPool.DOT + nacosConfigFileExtension;
                }
                properties.setProperty(SysConstant.SPRING_CONFIG_IMPORT, dataids);
                List<String> list = ListTs.asList(dataids.split(StringPool.COLON));
                properties.setProperty(SysConstant.EASY4J_NACOS_DATA_IDS, ListTs.get(list, list.size() - 1, ""));
            }


            String nacosUrl = ejSys.getNacosUrl();
            NacosUrlResolve nacosUrlResolve = new NacosUrlResolve();
            String username = nacosUrlResolve.getUsername(nacosUrl);
            String password = nacosUrlResolve.getPassword(nacosUrl);
            nacosUrlResolve.handler(properties,nacosUrl);


            String nacosNameSpace = StrUtil.blankToDefault(ejSys.getNacosNameSpace(), "public");
            String nacosConfigUrl = ejSys.getNacosConfigUrl();
            setProperties(properties, SysConstant.EASY4J_NACOS_GROUP, nacosGroup);
            setProperties(properties, SysConstant.EASY4J_NACOS_NAMESPACE, nacosNameSpace);
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
