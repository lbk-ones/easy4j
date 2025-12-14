package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.domain.TestDynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.OpSelector;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
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
@SpringBootTest(classes = DDLParseJavaClassTestOpenGuass.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=ssc_opengauss",
        "spring.datasource.password=SSC@hainan123",
        "spring.datasource.url=jdbc:postgresql://10.0.71.38:9000/postgres",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class DDLParseJavaClassTestOpenGuass {

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

            ResultSet sscProduct = metaData.getColumns(catalog, schema, "ssc_product", null);
            List<Map<String, Object>> handle1 = new MapListHandler().handle(sscProduct);

            System.out.println(JacksonUtil.toJson(handle1));

        }
    }

    @Test
    void OpMetaTest() throws SQLException {
        Connection connection = dataSource.getConnection();
        System.out.println(connection.getMetaData().getURL());
        String catalog = connection.getCatalog();
        String schema = connection.getSchema();
        OpDbMeta opDbMeta = new OpDbMeta(connection);
        System.out.println(JacksonUtil.toJson(opDbMeta.getAllTableInfo()));
        System.out.println(opDbMeta.getDbType());
        System.out.println(opDbMeta.getMajorVersion());
        System.out.println(opDbMeta.getMinorVersion());
        System.out.println(opDbMeta.getProductVersion());
        System.out.println("table-info:" + JacksonUtil.toJson(opDbMeta.getTableInfos("element_obs_exam_info")));
        System.out.println("columns:" + JacksonUtil.toJson(opDbMeta.getColumns(catalog, schema, "element_obs_exam_info")));
        System.out.println("primary-keys:" + JacksonUtil.toJson(opDbMeta.getPrimaryKes(catalog, schema, "element_obs_exam_info")));
        System.out.println("index-infos:" + JacksonUtil.toJson(opDbMeta.getIndexInfos(catalog, schema, "element_obs_exam_info")));

    }

    @Test
    void OpMetaTest2() {
//        Connection connection = dataSource.getConnection();
//        System.out.println(connection.getMetaData().getURL());
//        String catalog = connection.getCatalog();
//        String schema = connection.getSchema();
//        System.out.println(catalog);
//        System.out.println(schema);
        try (DynamicDDL sscElementTest = new DynamicDDL(dataSource, "element_obs_exam_info")) {
            System.out.println(sscElementTest.getCreateTableDDL());
            System.out.println(sscElementTest.getCreateTableComments().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
            System.out.println(sscElementTest.getIndexList().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
        }
        System.out.println("--------------------------------------------------------");
        try (DynamicDDL sscElementTest2 = new DynamicDDL(dataSource, "element_encounter_manage")) {
            System.out.println(sscElementTest2.getCreateTableDDL());
            System.out.println(sscElementTest2.getCreateTableComments().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
            System.out.println(sscElementTest2.getIndexList().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
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