package easy4j.infra.dbaccess.orm.plugin;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.dbaccess.orm.Access;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PluginSelector {

    private final static List<IPlugin> iSqlDialectList = ServiceLoaderUtils.load(IPlugin.class);

    public static Connection getConnection(Access<?> access, AccessConfig accessConfig) {
        Map<String, AccessConfig.PluginState> plugins = accessConfig.getPlugins();
        for (IPlugin iPlugin : iSqlDialectList) {
            String name = iPlugin.getName();
            if (StrUtil.isBlank(name)) {
                continue;
            }
            if (plugins.getOrDefault(name, new AccessConfig.PluginState()).isEnabled()) {
                Connection connection = iPlugin.getConnection(access);
                if (connection != null) return connection;
            }
        }
        return null;
    }

    public static List<IPlugin> get(RuntimeContext<?> context) {
        List<IPlugin> pluginsRes = new ArrayList<>();
        AccessConfig accessConfig = context.getAccessUtils().getAccessConfig();
        Map<String, AccessConfig.PluginState> plugins = accessConfig.getPlugins();
        for (IPlugin iSqlDialect : iSqlDialectList) {
            String name = iSqlDialect.getName();
            if (StrUtil.isBlank(name)) {
                continue;
            }
            AccessConfig.PluginState pluginState = plugins.getOrDefault(name, new AccessConfig.PluginState());
            if (pluginState.isEnabled()) {
                pluginsRes.add(iSqlDialect);
            }

        }
        return pluginsRes;
    }


}
