package easy4j.infra.dbaccess.dynamic;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.JdbcDbAccess;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.SqlServerDyInformationSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.List;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = SqlServerDyInformationSchemaTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=sa",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:sqlserver://localhost:1433;database=mydb;encrypt=false",
        "spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class SqlServerDyInformationSchemaTest {

    @Autowired
    DataSource dataSource;

    @Test
    void getColumns() {

        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        jdbcDbAccess.init(dataSource);
        SqlServerDyInformationSchema pgDyInformationSchema = new SqlServerDyInformationSchema();
        pgDyInformationSchema.setDbAccess(jdbcDbAccess);
        List<DynamicColumn> columns = pgDyInformationSchema.getColumns("public", "sys_security_session");
        System.out.println(JacksonUtil.toJson(columns));

    }
}