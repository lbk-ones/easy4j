package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.domain.TestDynamicDDL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

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
}