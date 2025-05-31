package easy4j.module.base.plugin.dbaccess.condition;

import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.dialect.H2Dialect;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@Easy4JStarterTest(
//        serverPort = 9091,
//        serverName = "test-sqlbuilder"
//)
//@SpringBootTest(classes = SqlBuilderTest.class)
class SqlBuilderTest {

    @Mock
    Connection connection;

    FSqlBuild<SysLogRecord> fSqlBuilder;

    Dialect dialect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fSqlBuilder = FSqlBuild.get(SysLogRecord.class);
        fSqlBuilder.bind(connection);
        dialect = new H2Dialect();
        fSqlBuilder.bind(dialect);
    }

    @AfterEach
    void after() {
        //MockitoAnnotations.openMocks(this);
    }

    @Test
    void build() {
        ArrayList<Object> objects = new ArrayList<>();
        String build = fSqlBuilder.equal(SysLogRecord::getParams, "test")
                .build(objects);
        assertEquals("params = ?", build);


        fSqlBuilder.clear();
        List<Object> argList = ListTs.newArrayList();
        // 示例 1：简单条件
        String condition1 = fSqlBuilder
                .equal("age", 30)
                .and(SqlBuild.get(connection, dialect)
                        .equal("gender", "F")
                        .or(SqlBuild.get(connection, dialect)
                                .equal("department", "IT")
                                .ne("salary", 5000)
                        )
                ).or(
                        SqlBuild.get(connection, dialect)
                                .gt("create_date", new Date()).isNotNull("ord_class")
                ).in(
                        "order_no",
                        "1234151",
                        "2151251651"
                ).build(argList);
        System.out.println("条件 1: " + condition1);
        System.out.println("值 1: " + JacksonUtil.toJson(argList));
        argList.clear();
        assertEquals("age = ? AND order_no IN (?, ?) AND (gender = ? AND (department = ? OR salary != ?)) AND (create_date > ? OR ord_class IS NOT NULL)", condition1);
        // 输出: age = 30 AND (gender = 'F' OR (department = 'IT' AND salary != 5000))

        fSqlBuilder.clear();
        // 示例 2：复杂条件
        String condition2 = fSqlBuilder
                .withLogicOperator(LogicOperator.OR)
                .like("name", "A%")
                .in("department", ListTs.asList("IT", "HR"))
                .between("salary", 3000, 5000)
                .not(SqlBuild.get()
                        .isNull("email")
                        .or(SqlBuild.get()
                                .equal("status", "INACTIVE")
                        )
                ).build(argList);
        assertEquals("name LIKE ? OR department IN (?, ?) OR salary BETWEEN ? AND ? OR NOT (email IS NULL OR (status = ?))", condition2);
        System.out.println("条件 2: " + condition2);
        System.out.println("值 2: " + JacksonUtil.toJson(argList));
        // 输出: name LIKE 'A%' OR department IN ('IT', 'HR') OR salary BETWEEN 3000 AND 5000 OR NOT (email IS NULL OR status = 'INACTIVE')

        // 示例 3：用于 SQL 查询
        String sql = "SELECT * FROM employees WHERE " + condition1;
        System.out.println("完整 SQL: " + sql);
    }
}