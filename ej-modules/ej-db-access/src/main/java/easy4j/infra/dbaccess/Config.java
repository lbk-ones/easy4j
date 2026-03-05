package easy4j.infra.dbaccess;

import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

import javax.sql.DataSource;

@Configuration
public class Config implements AutoRegisterContext {

    @Resource
    private DataSource dataSource;

    // 默认与当前事务绑定
    @Bean
    public DBAccess dbAccess() {
        return DBAccessFactory.getDBAccess(dataSource, true, true);
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        easy4jContext.register(dbAccess());
    }
}
