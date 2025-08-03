package easy4j.infra.dbaccess.dynamic.schema;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.DBAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
    DBAccess dbAccess;

    @Test
    void getVersion() {

        SqlServerDyInformationSchema mysqlDyInformationSchema = new SqlServerDyInformationSchema();
        mysqlDyInformationSchema.setDbAccess(dbAccess);
        System.out.println(mysqlDyInformationSchema.getVersion());
    }
}