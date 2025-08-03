package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = MysqlDDLTableStrategyTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=root",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:mysql://localhost:3306/seata",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class MysqlDDLTableStrategyTest {

    @Test
    void getTableTemplate() {
        MysqlDDLTableStrategy mysqlDDLTableStrategy = new MysqlDDLTableStrategy();
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName("test_create_table");
        ddlTableInfo.setComment("这是测试");
        DDLFieldInfo ddlFieldInfo = new DDLFieldInfo();
        ddlFieldInfo.setName("ordTxt");
//        ddlFieldInfo.setDataType("String");
//        ddlFieldInfo.setDataLength();
        ddlTableInfo.setFieldInfoList(ListTs.asList(ddlFieldInfo));
        String tableTemplate = mysqlDDLTableStrategy.getTableTemplate(ddlTableInfo);

        System.out.println(tableTemplate);
    }

}