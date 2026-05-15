package easy4j.infra.base.properties;
import com.google.common.collect.Lists;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.base.resolve.StandAbstractEasy4jResolve;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * 兼容解析nacos的配置参数
 * @author bokun.li
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NacosPropetiesParse extends StandAbstractEasy4jResolve {

    private String nacosUrl;
    private String nacosUsername;
    private String nacosPassword;
    private String nacosNamespace;
    private String nacosGroup;

    private String nacosConfigUsername;
    private String nacosConfigPassword;
    private String nacosConfigUrl;
    private String nacosConfigNameSpace;
    private String nacosConfigGroup;

    private String nacosDiscoveryUsername;
    private String nacosDiscoveryPassword;
    private String nacosDiscoveryUrl;
    private String nacosDiscoveryNameSpace;
    private String nacosDiscoveryGroup;

    private List<NacosDataId> dataIds;


    @Data
    public static class NacosDataId{
        private String dataId;
        private String group;
    }

    public static NacosPropetiesParse build(Environment env){
        EjSysProperties ejSysProperties = Easy4j.getEjSysPropertiesFromEnv(env);
        String nacosUrl1 = getUrl(ejSysProperties.getNacosUrl());
        String nacosUsername1 = StrUtil.blankToDefault(getUsername(ejSysProperties.getNacosUrl()),ejSysProperties.getNacosUsername());
        String nacosPassword1 =  StrUtil.blankToDefault(getPassword(ejSysProperties.getNacosUrl()),ejSysProperties.getNacosPassword());
        String nacosGroup1 = ejSysProperties.getNacosGroup();
        String nacosNameSpace = ejSysProperties.getNacosNameSpace();
        String nacosConfigUrl1 = getUrl(ejSysProperties.getNacosConfigUrl());
        String nacosConfigNamespace = ejSysProperties.getNacosConfigNamespace();
        String nacosConfigUsername1 = StrUtil.blankToDefault(getUsername(ejSysProperties.getNacosConfigUrl()),ejSysProperties.getNacosConfigUsername());
        String nacosConfigPassword1 = StrUtil.blankToDefault(getPassword(ejSysProperties.getNacosConfigUrl()),ejSysProperties.getNacosConfigPassword());;
        String nacosConfigGroup1 = ejSysProperties.getNacosConfigGroup();
        String nacosDiscoveryUrl = getUrl(ejSysProperties.getNacosDiscoveryUrl());
        String nacosDiscoveryUsername1 = StrUtil.blankToDefault(getUsername(ejSysProperties.getNacosDiscoveryUrl()),ejSysProperties.getNacosDiscoveryUsername());;
        String nacosDiscoveryPassword1 = StrUtil.blankToDefault(getPassword(ejSysProperties.getNacosDiscoveryUrl()),ejSysProperties.getNacosDiscoveryPassword());
        String nacosDiscoveryNamespace = ejSysProperties.getNacosDiscoveryNamespace();
        String nacosDiscoveryGroup1 = ejSysProperties.getNacosDiscoveryGroup();
        String dataIds1 = ejSysProperties.getDataIds();

        NacosPropetiesParse nacosPropetiesParse = new NacosPropetiesParse();
        nacosPropetiesParse.setNacosUrl(nacosUrl1);
        nacosPropetiesParse.setNacosUsername(nacosUsername1);
        nacosPropetiesParse.setNacosPassword(nacosPassword1);
        nacosPropetiesParse.setNacosNamespace(nacosNameSpace);
        nacosPropetiesParse.setNacosGroup(nacosGroup1);
        nacosPropetiesParse.setNacosConfigUsername(StrUtil.blankToDefault(nacosConfigUsername1,nacosUsername1));
        nacosPropetiesParse.setNacosConfigPassword(StrUtil.blankToDefault(nacosConfigPassword1,nacosPassword1));
        nacosPropetiesParse.setNacosConfigUrl(StrUtil.blankToDefault(nacosConfigUrl1,nacosUrl1));
        nacosPropetiesParse.setNacosConfigNameSpace(StrUtil.blankToDefault(nacosConfigNamespace,nacosNameSpace));
        nacosPropetiesParse.setNacosConfigGroup(StrUtil.blankToDefault(nacosConfigGroup1,nacosGroup1));
        nacosPropetiesParse.setNacosDiscoveryUsername(StrUtil.blankToDefault(nacosDiscoveryUsername1,nacosUsername1));
        nacosPropetiesParse.setNacosDiscoveryPassword(StrUtil.blankToDefault(nacosDiscoveryPassword1,nacosPassword1));
        nacosPropetiesParse.setNacosDiscoveryUrl(StrUtil.blankToDefault(nacosDiscoveryUrl,nacosUrl1));
        nacosPropetiesParse.setNacosDiscoveryNameSpace(StrUtil.blankToDefault(nacosDiscoveryNamespace,nacosNameSpace));
        nacosPropetiesParse.setNacosDiscoveryGroup(StrUtil.blankToDefault(nacosDiscoveryGroup1,nacosGroup1));

        List<String> strings = ListTs.splitToList(dataIds1, SP.COMMA);
        List<NacosDataId> objects = Lists.newArrayList();
        for (String string : strings) {
            String dataId = nacosPropetiesParse.getDataId(string);
            String defaultGroup = StrUtil.firstNonBlank(nacosGroup1, nacosConfigGroup1, nacosDiscoveryGroup1);
            String group = nacosPropetiesParse.getGroup(string, defaultGroup);
            NacosDataId nacosDataId = new NacosDataId();
            nacosDataId.setDataId(dataId);
            nacosDataId.setGroup(group);
            objects.add(nacosDataId);
        }
        nacosPropetiesParse.setDataIds(objects);
        return nacosPropetiesParse;
    }

}
