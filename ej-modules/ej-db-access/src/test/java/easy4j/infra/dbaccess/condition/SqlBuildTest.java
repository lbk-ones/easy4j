package easy4j.infra.dbaccess.condition;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = SqlBuildTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=easy4j",
        "spring.datasource.password=easy4j",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.h2.console.enabled=true",
        "spring.h2.console.path=/h2-console",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class SqlBuildTest {

    @Autowired
    DataSource dataSource;
    Connection connection;

    FWhereBuild<SysLogRecord> fSqlBuilder;

    Dialect dialect;

    @BeforeEach
    void setUp() {
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        fSqlBuilder = FWhereBuild.get(SysLogRecord.class);
        fSqlBuilder.bind(connection);
        dialect = JdbcHelper.getDialect(connection);
        fSqlBuilder.bind(dialect);
    }

    @AfterEach
    void afterEach() throws SQLException {
        connection.close();
    }

    @Test
    void build() {
        WhereBuild condition1 = fSqlBuilder
                .select("groupArg1", "groupArg2")
                .distinct()
                .equal("age", 30)
                .and(WhereBuild.get(connection, dialect)
                        .equal("gender", "F")
                        .or(WhereBuild.get(connection, dialect)
                                .equal("department", "IT")
                                .ne("salary", 5000)
                        )
                ).or(
                        WhereBuild.get(connection, dialect)
                                .gt("create_date", new Date()).isNotNull("ord_class")
                ).inArray("order_no", "1234151", "2151251651")
                .asc("ageMax", "xx")
                .desc("xxx")
                .groupBy("groupArg1", "groupArg2");

        SqlBuild sqlBuild = SqlBuild.get();
        String build = sqlBuild.build(
                SqlBuild.SELECT,
                condition1,
                SysLogRecord.class,
                null,
                true,
                null,
                connection,
                dialect
        );
        assertEquals("SELECT DISTINCT group_arg1, group_arg2 FROM sys_log_record  WHERE age = ? AND order_no IN (?, ?) AND (gender = ? AND (department = ? OR salary != ?)) AND (create_date > ? OR ord_class IS NOT NULL) GROUP BY group_arg1, group_arg2 ORDER BY age_max ASC, xx ASC, xxx DESC",
                build);
        System.out.println(build);
    }

    @Test
    void insert() {
        fSqlBuilder.equal(SysLogRecord::getTag, "test");
        SqlBuild sqlBuild = SqlBuild.get();
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId(UUID.randomUUID().toString().replace("-", ""));
        sysLogRecord.setStatus("1");
        sysLogRecord.setRemark("remark test");
        sysLogRecord.setTag("tag test");
        sysLogRecord.setParams("params test");
        sysLogRecord.setTagDesc("tagDesc test");
        sysLogRecord.setCreateDate(new Date());
        sysLogRecord.setTraceId("traceId");
        sysLogRecord.setProcessTime(String.valueOf(2));
        sysLogRecord.setErrorInfo("error into test");
        sysLogRecord.setOperateCode("operate code ");
        sysLogRecord.setOperateName("operate name");
        sysLogRecord.setTargetId("target id");
        sysLogRecord.setTargetId2("target id2");
        String build = sqlBuild.build(
                SqlBuild.INSERT,
                null,
                SysLogRecord.class,
                sysLogRecord,
                true,
                null,
                connection, dialect
        );
        assertEquals("INSERT INTO sys_log_record (trace_id, error_info, tag_desc, operate_code, remark, target_id, params, process_time, target_id2, operate_name, id, tag, create_date, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", build);


        String build2 = sqlBuild.build(
                SqlBuild.INSERT,
                null,
                SysLogRecord.class,
                sysLogRecord,
                false,
                null,
                connection, dialect
        );
        //assertEquals("INSERT INTO sys_log_record (trace_id, error_info, tag_desc, operate_code, remark, target_id, params, process_time, target_id2, operate_name, id, tag, create_date, status) VALUES ('traceId', 'error into test', 'tagDesc test', 'operate code ', 'remark test', 'target id', 'params test', '2', 'target id2', 'operate name', '0212563913b5407f8f0afb137a98051d', 'tag test', CAST('2025-06-03 17:29:45' as TIMESTAMP), '1')", build2);
        System.out.println(build2);
    }
}