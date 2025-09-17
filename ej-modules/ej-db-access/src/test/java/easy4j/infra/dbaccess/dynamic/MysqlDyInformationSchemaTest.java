package easy4j.infra.dbaccess.dynamic;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.JdbcDbAccess;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.CatalogMetadata;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.MysqlDyInformationSchema;
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
@SpringBootTest(classes = MysqlDyInformationSchemaTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=root",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:mysql://localhost:3306/vcc_portal_v1",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class MysqlDyInformationSchemaTest {

    @Autowired
    DataSource dataSource;

    @Test
    void getColumns() {

        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        jdbcDbAccess.init(dataSource);
        MysqlDyInformationSchema pgDyInformationSchema = new MysqlDyInformationSchema();
        pgDyInformationSchema.setDbAccess(jdbcDbAccess);
        List<DynamicColumn> columns = pgDyInformationSchema.getColumns("seata", "branch_table");
        System.out.println(JacksonUtil.toJson(columns));

    }

    @Test
    void OpMetaTest() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println(connection.getMetaData().getURL());
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        OpDbMeta opDbMeta = new OpDbMeta(connection);
        System.out.println(JacksonUtil.toJson(opDbMeta.getAllTableInfo()));
        System.out.println(opDbMeta.getDbType(connection));
        System.out.println(opDbMeta.getMajorVersion());
        System.out.println(opDbMeta.getMinorVersion());
        System.out.println(opDbMeta.getProductVersion());
        System.out.println(JacksonUtil.toJson(opDbMeta.getTableInfos( "tb_sys_api_error")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getColumns(catalog, schema, "tb_sys_api_error")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getPrimaryKes(catalog, schema, "tb_sys_api_error")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getIndexInfos(catalog, schema, "tb_sys_api_error")));
        List<CatalogMetadata> cataLogs = opDbMeta.getCataLogs();
        System.out.println(JacksonUtil.toJson(cataLogs));
        for (CatalogMetadata cataLog : cataLogs) {
            System.out.println(JacksonUtil.toJson(opDbMeta.getSchemas(cataLog.getTableCat())));
        }

    }
}