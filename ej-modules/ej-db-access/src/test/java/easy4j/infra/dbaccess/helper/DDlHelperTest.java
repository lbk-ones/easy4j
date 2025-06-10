package easy4j.infra.dbaccess.helper;

import com.zaxxer.hikari.HikariDataSource;
import easy4j.infra.common.utils.ListTs;
import org.h2.Driver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;

@SpringBootTest
class DDlHelperTest {

    @Test
    void execDDL() throws Exception {

        try (HikariDataSource dataSource = new HikariDataSource()) {
            dataSource.setJdbcUrl("jdbc:h2:mem:test");
            dataSource.setUsername("easy4j");
            dataSource.setPassword("easy4j");
            dataSource.setDriverClassName(Driver.class.getName());
            Connection connection = dataSource.getConnection();
            DDlHelper.execDDL(connection, "create table TEMP(NAME VARCHAR(100) COMMENT 'NAME')", ListTs.asList(), false);
            DDlHelper.execDDL(connection, "select * from TEMP;", ListTs.asList(), false);
            DDlHelper.execDDL(connection, "drop table TEMP;", ListTs.asList(), true);
//            DDlHelper.execDDL(connection, "drop table XXX;", ListTs.asList(), false);
//            DDlHelper.execDDL(connection, "drop table XXX;", ListTs.asList(), false);
//            DDlHelper.execDDL(connection, "drop table XXX;", ListTs.asList(), true);
        }
    }
}