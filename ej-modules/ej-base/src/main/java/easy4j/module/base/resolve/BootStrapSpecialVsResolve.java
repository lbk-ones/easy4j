package easy4j.module.base.resolve;

import cn.hutool.core.convert.Convert;
import easy4j.module.base.utils.SysConstant;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 引导阶段特殊参数对照
 */
public class BootStrapSpecialVsResolve extends MapEasy4jResolve {
    @Override
    public Map<String, Object> handler(Map<String, Object> mapProperties, String p) {
        Set<String> setCopy = new HashSet<>(mapProperties.keySet());
        // transform
        for (String key : setCopy) {
            Object o = mapProperties.get(key);
            switch (key) {
                case SysConstant.EASY4J_SERVER_PORT:
                    try {
                        Integer port = Integer.parseInt(o.toString());
                        mapProperties.put(SysConstant.SPRING_SERVER_PORT, port);
                    } catch (Exception e) {
                        throw new InvalidParameterException("invalid port:" + o);
                    }
                    break;
                case SysConstant.EASY4J_SERVER_NAME:
                    mapProperties.put(SysConstant.SPRING_SERVER_NAME, Convert.toStr(o));
                    break;
//                case SysConstant.EASY4J_SCA_NACOS_URL:
//                    NacosUrlResolve nacosUrlResolve = new NacosUrlResolve();
//                    nacosUrlResolve.handlerMap(mapProperties, Convert.toStr(o));
//                    break;
                case SysConstant.DB_URL_STR_NEW:
                    DataSourceUrlResolve dataSourceUrlResolve = new DataSourceUrlResolve();
                    dataSourceUrlResolve.handlerMap(mapProperties,Convert.toStr(o));
                 break;
                default:
                    break;
            }
        }
        return mapProperties;
    }
}
