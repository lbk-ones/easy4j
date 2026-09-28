package easy4j.infra.dbaccess.orm;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.dbaccess.orm.plugin.IPlugin;
import easy4j.infra.dbaccess.orm.plugin.Plugins;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PluginLoader {

    static List<IPlugin> pluginsList;


    @Resource
    public void setPluginsList(List<IPlugin> pluginsList) {
        PluginLoader.pluginsList = pluginsList;
    }


    public static void loader(List<String> plugins, AccessConfig accessConfig) {
        if (plugins != null) {
            List<IPlugin> staticAll = Plugins.staticAll;
            List<IPlugin> finalAll = new ArrayList<>();
            ListTs.addAll(finalAll, staticAll);
            if (CollUtil.isNotEmpty(pluginsList)) {
                ListTs.addAll(finalAll, pluginsList);
            }
            for (IPlugin iPlugin : finalAll) {
                String name = iPlugin.getName();
                if (StrUtil.isBlank(name)) continue;
                if (plugins.stream().anyMatch(e -> StrUtil.equals(e, name))) {
                    System.out.println(SysLog.compact("load db access orm plugin " + name));
                    accessConfig.addPlugin(iPlugin);
                }
            }
        }
    }

}
