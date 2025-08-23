package easy4j.infra.dbaccess.dynamic;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.JdbcDbAccess;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.H2DyInformationSchema;
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
@SpringBootTest(classes = H2DyInformationSchemaTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=easy4j",
        "spring.datasource.password=easy4j",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.h2.console.enabled=true",
        "spring.h2.console.path=/h2-console",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class H2DyInformationSchemaTest {

    @Autowired
    DataSource dataSource;

    @Test
    void getColumns() {

        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        jdbcDbAccess.init(dataSource);
        H2DyInformationSchema pgDyInformationSchema = new H2DyInformationSchema();
        pgDyInformationSchema.setDbAccess(jdbcDbAccess);
        List<DynamicColumn> columns = pgDyInformationSchema.getColumns(null, "SYS_LOG_RECORD");
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

            ResultSet sscProduct = metaData.getColumns(catalog, schema, "SYS_LOG_RECORD", null);
            List<Map<String, Object>> handle1 = new MapListHandler().handle(sscProduct);
            List<DatabaseColumnMetadata> map = ListTs.map(handle1, e -> BeanUtil.mapToBean(e, DatabaseColumnMetadata.class, true, CopyOptions.create().ignoreNullValue()));
            System.out.println(JacksonUtil.toJson(map));

        }
    }

    @Test
    void OpMetaTest() throws SQLException {
        Connection connection = dataSource.getConnection();
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        OpDbMeta opDbMeta = new OpDbMeta(connection);
        System.out.println(JacksonUtil.toJson(opDbMeta.getAllTableInfo()));
        System.out.println(opDbMeta.getMajorVersion());
        System.out.println(opDbMeta.getMinorVersion());
        System.out.println(opDbMeta.getProductVersion());
        System.out.println(JacksonUtil.toJson(opDbMeta.getColumns(catalog, schema, "SYS_LOG_RECORD")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getPrimaryKes(catalog, schema, "SYS_LOG_RECORD")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getIndexInfos(catalog, schema, "SYS_LOG_RECORD")));

    }

}