package easy4j.infra.dbaccess.dynamic.dll.op.impl.sc;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.domain.TestDynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = OpSqlCommandsTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=drhi_user",
        "spring.datasource.password=drhi_password",
        "spring.datasource.url=jdbc:postgresql://10.0.32.19:30163/ds",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class OpSqlCommandsTest {
    @Autowired
    DataSource dataSource;

    @Test
    void autoDDLByJavaClass() {

        try (DynamicDDL dynamicDDL = new DynamicDDL(dataSource, null, TestDynamicDDL.class)) {
            String sql = dynamicDDL.autoDDLByJavaClass(false);
            System.out.println(sql);
        }

    }
}