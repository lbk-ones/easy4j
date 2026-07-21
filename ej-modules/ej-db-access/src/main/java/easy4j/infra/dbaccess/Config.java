package easy4j.infra.dbaccess;

import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.infra.dbaccess.orm.AccessConfig;
import easy4j.infra.dbaccess.orm.DBAccessImpl;
import easy4j.infra.dbaccess.orm.IDBAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

import javax.sql.DataSource;

@Configuration
public class Config implements AutoRegisterContext {

    @Resource
    private DataSource dataSource;


    @Bean
    public IDBAccess idbAccess() {
        AccessConfig accessConfig = new AccessConfig().setDataSource(dataSource).setInTransaction(true).setPrintSqlIs(true);
        return new DBAccessImpl(accessConfig);
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        easy4jContext.register(idbAccess());
    }
}
