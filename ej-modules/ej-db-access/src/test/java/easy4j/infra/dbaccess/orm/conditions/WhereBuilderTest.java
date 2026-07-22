package easy4j.infra.dbaccess.orm.conditions;

import com.fasterxml.jackson.core.type.TypeReference;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.json.JacksonUtil;

import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dialect.H2Dialect;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import easy4j.infra.dbaccess.orm.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
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


    public DataSource getH2DataSource() {
        String jdbcUrl = "jdbc:h2:mem:testdb";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(jdbcUrl);
        return new TempDataSource(driverClassNameByUrl, jdbcUrl, "sa", "");
    }
    @Test
    void build() {
        FWhereBuild<SysLogRecord> fSqlBuilder = FWhereBuild.get(SysLogRecord.class);
        ArrayList<Object> objects = new ArrayList<>();
        Access<SysLogRecord> tAccess = new Access<SysLogRecord>()
                .setClazz(SysLogRecord.class)
                .setOperateType(OperateType.SELECT);
        AccessConfig accessConfig = new AccessConfig();
        accessConfig.setDataSource(getH2DataSource());
        AccessUtils accessUtils = new AccessUtils(accessConfig);
        RuntimeContext<SysLogRecord> context = accessUtils.toContext(tAccess);
        DialectV2 dialectV2 = context.getDialectV2();
        String build = fSqlBuilder.eq(SysLogRecord::getParams, "test")
                .build(objects,context,false);
        assertEquals(dialectV2.escape("params")+" = ?",build);


        fSqlBuilder.clear();
        List<Object> argList = ListTs.newArrayList();
        // 示例 1：简单条件
        WhereBuild whereBuild = fSqlBuilder
                .select("groupArg1", "groupArg2")
                .eq("age", 30)
                .and(e2 -> e2
                        .eq("gender", "F")
                        .or((e) -> e.eq("department", "IT")
                                .ne("salary", 5000)
                        )
                ).or(
                        e -> e
                                .gt("create_date", new Date()).isNotNull("ord_class")
                ).inArray("order_no", "1234151", "2151251651")
                .asc("ageMax", "xx")
                .desc("xxx")
                .groupBy("groupArg1", "groupArg2");
        String build1 = whereBuild.build(argList, context, false);
        System.out.println("build1-->"+build1);
        String json = JacksonUtil.toJson(whereBuild);
        System.out.println("JSON-->"+ json);

        FWhereBuild<SysLogRecord> object = JacksonUtil.toObject(json, new TypeReference<FWhereBuild<SysLogRecord>>() {
        });


        String condition1 = object.build(argList,context,false);

        System.out.println("条件 1: " + condition1);
        System.out.println("值 1: " + JacksonUtil.toJson(argList));
        argList.clear();
        //assertEquals("age = ? AND order_no IN (?, ?) AND (gender = ? AND (department = ? OR salary != ?)) AND (create_date > ? OR ord_class IS NOT NULL) GROUP BY group_arg1, group_arg2 ORDER BY age_max ASC, xx ASC, xxx DESC", condition1);
        // 输出: age = 30 AND (gender = 'F' OR (department = 'IT' AND salary != 5000))

        fSqlBuilder.clear();
        // 示例 2：复杂条件
        String condition2 = fSqlBuilder
                .withLogicOperator(LogicOperator.OR)
                .like("name", "A%")
                .inArray("department", "IT", "HR")
                .between("salary", 3000, 5000)
                .not(e1 -> e1
                        .isNull("email")
                        .or(e2 -> e2
                                .eq("status", "INACTIVE")
                        )
                ).build(argList,context,false);
        //assertEquals("name LIKE ? OR department IN (?, ?) OR salary BETWEEN ? AND ? OR NOT (email IS NULL OR (status = ?))", condition2);
        System.out.println("条件 2: " + condition2);
        System.out.println("值 2: " + JacksonUtil.toJson(argList));
        // 输出: name LIKE 'A%' OR department IN ('IT', 'HR') OR salary BETWEEN 3000 AND 5000 OR NOT (email IS NULL OR status = 'INACTIVE')

        // 示例 3：用于 SQL 查询
        String sql = "SELECT * FROM employees WHERE " + condition1;
        System.out.println("完整 SQL: " + sql);
    }
}