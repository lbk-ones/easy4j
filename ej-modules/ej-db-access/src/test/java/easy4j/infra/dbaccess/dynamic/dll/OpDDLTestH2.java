package easy4j.infra.dbaccess.dynamic.dll;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.domain.TestDynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.impl.sc.CopyDbConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.IOpMeta;
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
@SpringBootTest(classes = OpDDLTestH2.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.datasource.url=jdbc:h2:tcp://10.100.128.93:9092/~/test",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class OpDDLTestH2 {

    @Autowired
    DataSource dataSource;

    @Test
    void getDDLFragment() {
        DDLParseJavaClass ddlParseJavaClass = new DDLParseJavaClass(TestDynamicDDL.class,dataSource,null);
        System.out.println(ddlParseJavaClass.getCreateTableTxt());
        System.out.println("执行成功----------------");
    }

    @Test
    void dbDatabaseMetaInfo() throws SQLException {
        try(Connection connection = dataSource.getConnection()){
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

            ResultSet sscProduct = metaData.getColumns(catalog, schema, "H2_DATA_TYPES_DEMO", null);
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
        System.out.println(opDbMeta.getMajorVersion());
        System.out.println(opDbMeta.getMinorVersion());
        System.out.println(opDbMeta.getProductVersion());
        System.out.println(JacksonUtil.toJson(opDbMeta.getTableInfos( "H2_DATA_TYPES_DEMO")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getColumns(catalog, schema, "H2_DATA_TYPES_DEMO")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getPrimaryKes(catalog, schema, "H2_DATA_TYPES_DEMO")));
        System.out.println(JacksonUtil.toJson(opDbMeta.getIndexInfos(catalog, schema, "H2_DATA_TYPES_DEMO")));

    }

    @Test
    void OpMetaTest3() {
        try (DynamicDDL sscElementTest = new DynamicDDL(dataSource, null,TestDynamicDDL.class)) {
            System.out.println(sscElementTest.getCreateTableDDL());
            System.out.println(sscElementTest.getCreateTableComments().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
            System.out.println(sscElementTest.getIndexList().stream().collect(Collectors.joining(SP.SEMICOLON + SP.NEWLINE)));
        }
    }
    public DataSource getMysql5DataSource(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/student");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }
    public DataSource getMysql5TestCopyDataSource(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://localhost:3306/test_copy");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("123456");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }
    public DataSource getMysql8Test2DataSource(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://10.0.71.38:36180/test2");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("root");
        hikariConfig.setPassword("SSC@hainan123");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }
    public DataSource getTestPg(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://10.0.32.19:30163/test");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("drhi_user");
        hikariConfig.setPassword("drhi_password");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }
    public DataSource getTest2Pg(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://10.0.32.19:30163/test2");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("drhi_user");
        hikariConfig.setPassword("drhi_password");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }

    public DataSource getOracle19cDataSource(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:oracle:thin:@//10.0.71.45:36181/ORCLPDB1");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("SSC");
        hikariConfig.setPassword("SSC@hainan123");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }
    public DataSource getOracle19cTestCopyDataSource(){
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:oracle:thin:@//10.0.71.45:36181/ORCLPDB1");
        String jdbcUrl = hikariConfig.getJdbcUrl();
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        hikariConfig.setDriverClassName(driverClassNameByUrl);
        hikariConfig.setUsername("TEST_COPY");
        hikariConfig.setPassword("SSC@hainan123");
        hikariConfig.setMaximumPoolSize(20); // 最大连接数
        hikariConfig.setMinimumIdle(20/2);             // 最小空闲连接数
        hikariConfig.setIdleTimeout(600000);         // 空闲超时 10 分钟
        hikariConfig.setMaxLifetime(1800000);        // 连接最大生命周期 30 分钟
        hikariConfig.setConnectionTimeout(30000);    // 获取连接超时 3 秒
        hikariConfig.setConnectionTestQuery(SqlType.getValidateSqlByUrl(jdbcUrl)); // 测试连接的 SQL
        return new HikariDataSource(hikariConfig);
    }

    // h2 到 oracle
    @Test
    void OpMetaTest4() {
        try (DynamicDDL sscElementTest = new DynamicDDL(dataSource)) {
            CopyDbConfig copyDbConfig = new CopyDbConfig();
            copyDbConfig.setDataSource(getOracle19cDataSource()).setExe(false);

            List<String> strings = sscElementTest.copyDataSourceDDL(null, new String[]{"TABLE"}, copyDbConfig);
            for (String string : strings) {
                System.out.println(string);
                System.out.println("-----------------------------");
            }
        }
    }
    // h2 到 pg
    @Test
    void h2ToPg() {
        try (DynamicDDL dynamicDDL = new DynamicDDL(dataSource)) {
            DataSource testPg = getTest2Pg();
            List<String> strings = dynamicDDL.copyDataSourceDDL(null, null, new CopyDbConfig().setDataSource(testPg).setExe(true));
            for (String string : strings) {
                System.out.println(string);
                System.out.println("-------------------------------------");
            }
        }
    }

    // h2 到 mysql
    @Test
    void h2ToMysql() {
        try (DynamicDDL dynamicDDL = new DynamicDDL(dataSource)) {
            DataSource testPg = getMysql8Test2DataSource();
            List<String> strings = dynamicDDL.copyDataSourceDDL(null, null, new CopyDbConfig().setDataSource(testPg).setExe(true));
            for (String string : strings) {
                System.out.println(string);
                System.out.println("-------------------------------------");
            }
        }
    }

    // oracle 到 h2
    @Test
    void OracleToH2() {
        try (DynamicDDL sscElementTest = new DynamicDDL(getOracle19cTestCopyDataSource())) {
            CopyDbConfig copyDbConfig = new CopyDbConfig();
            copyDbConfig.setDataSource(dataSource).setExe(true);

            List<String> strings = sscElementTest.copyDataSourceDDL(null, new String[]{"TABLE"}, copyDbConfig);
            for (String string : strings) {
                System.out.println(string);
                System.out.println("-----------------------------");
            }
        }
    }

    // mysql 到 h2
    @Test
    void MysqlToH2() {
        try (DynamicDDL sscElementTest = new DynamicDDL(getMysql8Test2DataSource())) {
            CopyDbConfig copyDbConfig = new CopyDbConfig();
            copyDbConfig.setDataSource(dataSource).setExe(true);

            List<String> strings = sscElementTest.copyDataSourceDDL(null, new String[]{"TABLE"}, copyDbConfig);
            for (String string : strings) {
                System.out.println(string);
                System.out.println("-----------------------------");
            }
        }
    }

    // pg 到 h2
    @Test
    void pgToH2() {
        try (DynamicDDL sscElementTest = new DynamicDDL(getTest2Pg())) {
            CopyDbConfig copyDbConfig = new CopyDbConfig();
            copyDbConfig.setDataSource(dataSource).setExe(true);

            List<String> strings = sscElementTest.copyDataSourceDDL(null, new String[]{"TABLE"}, copyDbConfig);
            for (String string : strings) {
                System.out.println(string);
                System.out.println("-----------------------------");
            }
        }
    }

}