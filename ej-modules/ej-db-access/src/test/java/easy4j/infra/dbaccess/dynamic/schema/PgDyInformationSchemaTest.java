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
@SpringBootTest(classes = PgDyInformationSchemaTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=root",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/postgres",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class PgDyInformationSchemaTest {
    @Autowired
    DBAccess dbAccess;

    @Test
    void getVersion() {

        PgDyInformationSchema mysqlDyInformationSchema = new PgDyInformationSchema();
        mysqlDyInformationSchema.setDbAccess(dbAccess);
        System.out.println(mysqlDyInformationSchema.getVersion());
    }
}