package easy4j.infra.dbaccess.dynamic.dll.op.impl.sc;

import cn.hutool.core.bean.BeanUtil;
import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dialect.v2.PsResult;
import easy4j.infra.dbaccess.domain.TestDynamicDDL;
import easy4j.infra.dbaccess.domains.MetaJobDefinition;
import easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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


    @Test
    void test1() throws SQLException {
        Connection connection = dataSource.getConnection();
        DialectV2 dialectV2 = DialectFactory.get(connection);
        List<Map<String, Object>> objects = ListTs.newList();
        for (int i = 0; i < 10; i++) {
            MetaJobDefinition metaJobDefinition = new MetaJobDefinition();
            metaJobDefinition.setJobName("测测试"+i);
            metaJobDefinition.setJobDescription("bk's test");
            metaJobDefinition.setExecutionPlan("");
            metaJobDefinition.setClassificationCode("");
            metaJobDefinition.setClassificationName("");
            metaJobDefinition.setResourceDirectoryId(0L);
            metaJobDefinition.setTemplateId(0);
            metaJobDefinition.setTableCommentAsCnName(false);
            metaJobDefinition.setColumnCommentAsCnName(false);
            metaJobDefinition.setIsEnabled(1);
            metaJobDefinition.setIsDeleted(0);
            metaJobDefinition.setDataSourceId(0);
            metaJobDefinition.setCatalogName("");

            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(metaJobDefinition, true, true);
            objects.add(stringObjectMap);
        }


        PsResult psResult = dialectV2.jdbcInsert(objects, "ssc_meta_job_definition", null, 10, false, true);

        int iwt = 0;
        for (Map<String, Object> object : objects) {
            object.put("jobName","改"+iwt);
            iwt++;
        }
        System.out.println(psResult.getSql());
        System.out.println(psResult.getCostTime());
        System.out.println(psResult.getEffectRows());
        System.out.println("----------------------------------------");
        PsResult psResult2 = dialectV2.jdbcUpdate(objects, "ssc_meta_job_definition", null, true,true,true,ListTs.asList("jobDefinitionId"));
        System.out.println(psResult2.toString());
        connection.close();
    }

    @Test
    void test2() throws SQLException {
        Connection connection = dataSource.getConnection();
        DialectV2 dialectV2 = DialectFactory.get(connection);
        List<DatabaseColumnMetadata> columns = dialectV2.getColumns(connection.getCatalog(), connection.getSchema(), dialectV2.escape("1-2"));
        System.out.println(JacksonUtil.toJson(columns));
    }
}