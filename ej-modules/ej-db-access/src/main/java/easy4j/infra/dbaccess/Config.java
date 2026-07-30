package easy4j.infra.dbaccess;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.dbaccess.orm.*;
import easy4j.infra.dbaccess.orm.plugin.IPlugin;
import easy4j.infra.dbaccess.orm.plugin.Plugins;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableConfigurationProperties({SpringOrmProperties.class})
public class Config implements AutoRegisterContext {

    @Resource
    private DataSource dataSource;


    @Autowired
    SpringOrmProperties ormProperties;


    @Autowired(required = false)
    List<IPlugin> pluginsList;

    @Bean
    @ConditionalOnMissingBean
    public IDBAccess idbAccess() {
        // 默认加入当前事务，且打印全部sql
        AccessConfig accessConfig = new AccessConfig().setDataSource(dataSource).setInTransaction(true).setPrintSqlIs(true).setOnlyPrintSlowSql(false);
        List<String> plugins = ormProperties.getPlugins();
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
        return OrmFactory.get(accessConfig,ormProperties, true);
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        easy4jContext.register(idbAccess());
    }
}
