package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.domain.TestDynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
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
import java.util.stream.Collectors;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = DDLParseJavaClassTestOracle.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=demo",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/orcl.mshome.net",
        "spring.datasource.driver-class-name=oracle.jdbc.OracleDriver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class DDLParseJavaClassTestOracle {

    @Autowired
    DataSource dataSource;

    @Test
    void getDDLFragment() {
        DDLParseJavaClass ddlParseJavaClass = new DDLParseJavaClass(TestDynamicDDL.class, dataSource, null);
        System.out.println(ddlParseJavaClass.getCreateTableTxt());
        System.out.println("执行成功----------------");
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

            ResultSet sscProduct = metaData.getColumns(catalog, schema, "TEST_DYNAMIC_DDL", null);
            List<Map<String, Object>> handle1 = new MapListHandler().handle(sscProduct);

            System.out.println(JacksonUtil.toJson(handle1));

        }
    }

    @Test
    void OpMetaTest3() {
        try (DynamicDDL sscElementTest = new DynamicDDL(dataSource, null, TestDynamicDDL.class)) {
            System.out.println(sscElementTest.getCreateTableDDL());
            System.out.println(sscElementTest.getCreateTableComments().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
            System.out.println(sscElementTest.getIndexList().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
        }
    }

}