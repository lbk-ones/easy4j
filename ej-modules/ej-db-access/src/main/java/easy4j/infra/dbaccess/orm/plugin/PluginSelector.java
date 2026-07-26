package easy4j.infra.dbaccess.orm.plugin;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.ServiceLoaderUtils;
import easy4j.infra.dbaccess.orm.Access;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.*;

@Slf4j
public class PluginSelector {

    private final static List<IPlugin> iSqlDialectList = ServiceLoaderUtils.load(IPlugin.class);

    public static DataSource getDataSource(Access<?> access, AccessConfig accessConfig) {
        ArrayList<IPlugin> objects = new ArrayList<>();
        ListTs.addAll(objects,iSqlDialectList);
        List<IPlugin> pluginList = accessConfig.getPluginList();
        ListTs.addAll(objects,pluginList);
        for (IPlugin iPlugin : objects) {
            String name = iPlugin.getName();
            if (StrUtil.isBlank(name)) {
                continue;
            }
            DataSource dataSource = iPlugin.getDataSource(access);
            if (dataSource != null) return dataSource;
        }
        return null;
    }

    public static List<IPlugin> list(RuntimeContext<?> context) {
        List<IPlugin> pluginsRes = new ArrayList<>();
        AccessConfig accessConfig = context.getAccessUtils().getAccessConfig();
        List<IPlugin> pluginList = accessConfig.getPluginList();
        ListTs.addAll(pluginsRes,pluginList);
        for (IPlugin iSqlDialect : iSqlDialectList) {
            String name = iSqlDialect.getName();
            if (StrUtil.isBlank(name)) {
                continue;
            }
            pluginsRes.add(iSqlDialect);
        }
        return pluginsRes;
    }


    public static <T> void finish(RuntimeContext<T> context) {
        List<IPlugin> plugins = list(context);
        for (IPlugin plugin : plugins) {
            try {
                plugin.finish(context);
            } catch (Exception e) {
                log.error("finish error", e);
            }
        }

    }
}
