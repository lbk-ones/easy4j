package easy4j.infra.dbaccess.dynamic.schema;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.json.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = OracleTDyInformationSchemaTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=demo",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/orcl.mshome.net",
        "spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class OracleTDyInformationSchemaTest {

    @Autowired
    DataSource dataSource;

    @Test
    void getDDLFragment() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("catalog-" + connection.getCatalog());
            System.out.println("schema-" + connection.getSchema());
            List<DynamicColumn> columns = InformationSchema.getColumns(dataSource, "demo", "test", connection);
            System.out.println(JacksonUtil.toJson(columns));
            String dbVersion = InformationSchema.getDbVersion(dataSource, connection);
            System.out.println(dbVersion);
            String dbType = InformationSchema.getDbType(dataSource, connection);
            System.out.println(dbType);
        }
    }
}