package easy4j.infra.dbaccess.dynamic;

import cn.hutool.core.lang.Dict;
import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.condition.FWhereBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import java.util.List;

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
class DynamicTableQueryTest {

    @Autowired
    DataSource dataSource;

    @Test
    void query() {
        // Nice !
        WhereBuild equal = WhereBuild.get().equal("type", "SQL");
        List<Dict> query = new DynamicTableQuery(equal, dataSource, "public", "sys_flyway_schema_history")
                .query();

        System.out.println(JacksonUtil.toJson(query));
    }
}