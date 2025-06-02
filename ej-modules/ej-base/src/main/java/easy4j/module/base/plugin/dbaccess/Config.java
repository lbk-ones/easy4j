package easy4j.module.base.plugin.dbaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public class Config {

    @Resource
    private DataSource dataSource;

    // 默认与当前事务绑定
    @Bean
    public DBAccess dbAccess() {
        return DBAccessFactory.getDBAccess(dataSource, true, true);
    }
}
