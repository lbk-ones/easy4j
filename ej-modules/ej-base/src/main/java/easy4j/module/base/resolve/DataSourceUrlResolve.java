package easy4j.module.base.resolve;
import easy4j.module.base.utils.SysConstant;

import java.util.Map;
import java.util.Properties;

/**
 * 处理连接加密码
 */
public class DataSourceUrlResolve extends PropertiesResolve{

    @Override
    public Properties handler(Properties properties, String p) {
        properties.setProperty(SysConstant.DB_URL_STR, getUrl(p));
        properties.setProperty(SysConstant.DB_USER_NAME, getUsername(p));
        properties.setProperty(SysConstant.DB_USER_PASSWORD, getPassword(p));
        return properties;
    }

    public void handlerMap(Map<String,Object> properties, String p) {
        properties.put(SysConstant.DB_URL_STR, getUrl(p));
        properties.put(SysConstant.DB_USER_NAME, getUsername(p));
        properties.put(SysConstant.DB_USER_PASSWORD, getPassword(p));
    }



}
