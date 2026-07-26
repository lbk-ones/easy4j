package easy4j.infra.dbaccess.orm.plugin;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.dbaccess.orm.Access;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Slf4j
public class PluginSelector {

    private final static List<IPlugin> iSqlDialectList = ServiceLoaderUtils.load(IPlugin.class);

    public static DataSource getDataSource(Access<?> access, AccessConfig accessConfig) {
        Map<String, AccessConfig.PluginState> plugins = accessConfig.getPlugins();
        for (IPlugin iPlugin : iSqlDialectList) {
            String name = iPlugin.getName();
            if (StrUtil.isBlank(name)) {
                continue;
            }
            if (plugins.getOrDefault(name, new AccessConfig.PluginState()).isEnabled()) {
                DataSource dataSource = iPlugin.getDataSource(access);
                if (dataSource != null) return dataSource;
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


    public static <T> void finish(RuntimeContext<T> context) {
        List<IPlugin> plugins = get(context);
        for (IPlugin plugin : plugins) {
            try{
                plugin.finish(context);
            }catch (Exception e){
                log.error("finish error",e);
            }
        }

    }
}
