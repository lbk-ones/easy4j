package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.domain.SysDdlHistory;
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
@SpringBootTest(classes = DDLParseJavaClassTestPG.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=drhi_user",
        "spring.datasource.password=drhi_password",
        "spring.datasource.url=jdbc:postgresql://10.0.32.19:30163/ds",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class DDLParseJavaClassTestPG {

    @Autowired
    DataSource dataSource;

    @Test
    void getDDLFragment() {
        DDLParseJavaClass ddlParseJavaClass = new DDLParseJavaClass(TestDynamicDDL.class,dataSource,null);
        System.out.println(ddlParseJavaClass.getCreateTableTxt());
        System.out.println("执行成功----------------");
    }
}