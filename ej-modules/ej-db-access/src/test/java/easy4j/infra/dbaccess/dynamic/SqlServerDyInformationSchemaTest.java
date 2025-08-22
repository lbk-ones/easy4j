package easy4j.infra.dbaccess.dynamic;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.JdbcDbAccess;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.SqlServerDyInformationSchema;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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


    @Test
    void dbDatabaseMetaInfo() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            int databaseMajorVersion = metaData.getDatabaseMajorVersion();
            System.out.println(databaseMajorVersion);
            int databaseMinorVersion = metaData.getDatabaseMinorVersion();
            System.out.println(databaseMinorVersion);
            String databaseProductVersion = metaData.getDatabaseProductVersion();
            System.out.println(databaseProductVersion);
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            System.out.println(catalog);
            System.out.println(schema);
            ResultSet tables = metaData.getTables(catalog, schema, null, new String[]{"TABLE", "VIEW"});
            MapListHandler mapListHandler = new MapListHandler();
            List<Map<String, Object>> handle = mapListHandler.handle(tables);
            System.out.println(JacksonUtil.toJson(handle));
            JdbcHelper.close(tables);

            ResultSet sscProduct = metaData.getColumns(catalog, schema, "sys_log_record", null);
            List<Map<String, Object>> handle1 = new MapListHandler().handle(sscProduct);

            System.out.println(JacksonUtil.toJson(handle1));

        }
    }
}