package easy4j.infra.dbaccess.condition;

import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dialect.H2Dialect;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@Easy4JStarterTest(
//        serverPort = 9091,
//        serverName = "test-sqlbuilder"
//)
//@SpringBootTest(classes = SqlBuilderTest.class)
public class WhereBuilderTest {

    @Mock
    Connection connection;

    FWhereBuild<SysLogRecord> fSqlBuilder;

    Dialect dialect;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fSqlBuilder = FWhereBuild.get(SysLogRecord.class);
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
                .select("groupArg1", "groupArg2")
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
                .groupBy("groupArg1", "groupArg2")
                .build(argList);
        assertEquals("[\"group_arg1\",\"group_arg2\"]", JacksonUtil.toJson(fSqlBuilder.getSelectFields()));
        System.out.println("字段 1: " + fSqlBuilder.getSelectFields());
        System.out.println("条件 1: " + condition1);
        System.out.println("值 1: " + JacksonUtil.toJson(argList));
        argList.clear();
        assertEquals("age = ? AND order_no IN (?, ?) AND (gender = ? AND (department = ? OR salary != ?)) AND (create_date > ? OR ord_class IS NOT NULL) GROUP BY group_arg1, group_arg2 ORDER BY age_max ASC, xx ASC, xxx DESC", condition1);
        // 输出: age = 30 AND (gender = 'F' OR (department = 'IT' AND salary != 5000))

        fSqlBuilder.clear();
        // 示例 2：复杂条件
        String condition2 = fSqlBuilder
                .withLogicOperator(LogicOperator.OR)
                .like("name", "A%")
                .inArray("department", "IT", "HR")
                .between("salary", 3000, 5000)
                .not(WhereBuild.get()
                        .isNull("email")
                        .or(WhereBuild.get()
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