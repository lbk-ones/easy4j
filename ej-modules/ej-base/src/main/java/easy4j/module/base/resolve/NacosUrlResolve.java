package easy4j.module.base.resolve;


import cn.hutool.core.util.StrUtil;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.SysConstant;

import java.util.Map;
import java.util.Properties;

/**
 * 处理连接加密码
 */
public class NacosUrlResolve extends DataSourceUrlResolve {

    @Override
    public Properties handler(Properties properties, String p) {
        String url1 = getUrl(p);
        if (StrUtil.isBlank(url1)) {
            throw new RuntimeException("nacos url format is error !" + url1);
        }
        EjSysProperties ejSys = Easy4j.getEjSysProperties();
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_URL), url1);
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_USERNAME), getUsername(p));
        setPropertiesArr(properties, ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_PASSWORD), getPassword(p));
        return properties;
    }

    public void handlerMap(Map<String, Object> objectMap, String p) {
        String url1 = getUrl(p);
        if (StrUtil.isBlank(url1)) {
            throw new RuntimeException("nacos url format is error !" + url1);
        }
        EjSysProperties ejSys = Easy4j.getEjSysProperties();
        objectMap.put(ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_URL)[0], url1);
        objectMap.put(ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_USERNAME)[0], getUsername(p));
        objectMap.put(ejSys.getVs(SysConstant.EASY4J_SCA_NACOS_PASSWORD)[0], getPassword(p));
    }
}
