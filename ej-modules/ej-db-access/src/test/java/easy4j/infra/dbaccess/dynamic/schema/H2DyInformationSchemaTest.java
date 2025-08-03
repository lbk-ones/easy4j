package easy4j.infra.dbaccess.dynamic.schema;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.DBAccess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
    DBAccess dbAccess;

    @Test
    void getVersion() {

        H2DyInformationSchema mysqlDyInformationSchema = new H2DyInformationSchema();
        mysqlDyInformationSchema.setDbAccess(dbAccess);
        System.out.println(mysqlDyInformationSchema.getVersion());
    }
}