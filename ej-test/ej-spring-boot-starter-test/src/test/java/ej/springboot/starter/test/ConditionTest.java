package ej.springboot.starter.test;

import cn.hutool.core.lang.Tuple;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.condition.Condition;
import easy4j.module.base.plugin.dbaccess.condition.FCondition;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;
import easy4j.module.sentinel.EnableFlowDegrade;
import ej.spring.boot.starter.server.StartTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AppTest
 *
 * @author bokun.li
 * @date 2025-05
 */
@Easy4JStarter(
        serverPort = 10001,
        serverName = "test-server",
        serviceDesc = "测试条件",
        author = "bokun.li",
        enableH2 = false,
        ejDataSourceUrl = "jdbc:postgresql://localhost:5432/test@root:123456"
)
@EnableFlowDegrade
@SpringBootTest(classes = StartTest.class)
public class ConditionTest {

    private Condition condition;
    private Connection mockConnection;
    private Dialect mockDialect;
    private cn.hutool.db.sql.Wrapper mockWrapper;

    @Autowired
    DataSource dataSource;


    @BeforeEach
    public void setUp() throws SQLException {
        condition = Condition.get();
        mockConnection = dataSource.getConnection();
        System.out.println(mockConnection.isClosed());
        mockDialect = JdbcHelper.getDialect(mockConnection);
        assert mockDialect != null;
        mockWrapper = mockDialect.getWrapper();
    }

    @Test
    public void testBindConnection() {
        condition.bind(mockConnection);
        // 使用反射验证connection是否正确设置
        try {
            java.lang.reflect.Field connectionField = Condition.class.getDeclaredField("connection");
            connectionField.setAccessible(true);
            assertEquals(mockConnection, connectionField.get(condition));
        } catch (Exception e) {
            fail("反射获取connection字段失败: " + e.getMessage());
        }
    }

    @Test
    public void testSetCondition() {
        condition.set("id", "=", 1);
        assertEquals(1, condition.tuple.size());

        Tuple tuple = condition.tuple.get(0);
        assertEquals(true, tuple.get(0));  // 是否有符号
        assertEquals(Condition.AND, tuple.get(1)); // 连接符
        assertEquals("id", tuple.get(2));  // 字段名
        assertEquals("=", tuple.get(3));   // 符号
        assertEquals(1, (int) tuple.get(4));     // 值
    }

    @Test
    public void testEqual() {
        condition.equal("id", 1);
        Tuple tuple = condition.tuple.get(0);
        assertEquals("=", tuple.get(3));
    }

    @Test
    public void testGt() {
        condition.gt("age", 18);
        Tuple tuple = condition.tuple.get(0);
        assertEquals(">", tuple.get(3));
    }

    @Test
    public void testLike() {
        condition.like("name", "test");
        Tuple tuple = condition.tuple.get(0);
        assertEquals("like", tuple.get(3));
        assertEquals("%test%", tuple.get(4));
    }

    @Test
    public void testOrCondition() {
        Condition subCondition1 = Condition.get().equal("id", 1);
        Condition subCondition2 = Condition.get().equal("name", "test");
        condition.or(subCondition1, subCondition2);

        Tuple orTuple = condition.tuple.get(0);
        assertEquals("or", orTuple.get(1));
        Condition[] conditions = orTuple.get(2);
        assertEquals(2, conditions.length);
    }

    @Test
    public void testGroupBy() {
        condition.groupBy("id", "name");
        assertEquals(1, condition.groupTuple.size());

        Tuple groupTuple = condition.groupTuple.get(0);
        String[] fields = groupTuple.get(2);
        assertEquals(2, fields.length);
        assertArrayEquals(new String[]{"id", "name"}, fields);
    }

    @Test
    public void testOrderBy() {
        condition.asc("id").desc("create_time");
        assertEquals(2, condition.orderBy.size());

        Tuple ascTuple = condition.orderBy.get(0);
        assertEquals("id asc", ascTuple.get(2));

        Tuple descTuple = condition.orderBy.get(1);
        assertEquals("create_time desc", descTuple.get(2));
    }

    @Test
    public void testGetSqlSegment() {
        condition.equal("id", 1);
        List<Object> argsList = new ArrayList<>();

        String sqlSegment = condition.getSqlSegment(condition.tuple.get(0), argsList, mockWrapper);
        assertEquals(wrapper("id") + " = ?", sqlSegment);
        assertEquals(1, argsList.size());
        assertEquals(1, argsList.get(0));
    }

    @Test
    public void testGetSqlWithNoConnection() {
        assertThrows(EasyException.class, () -> condition.getSql(new ArrayList<>()));
    }

    public String wrapper(String field) {
        return mockWrapper.wrap(field);
    }

    @Test
    public void testGetSqlSimpleCondition() throws SQLException {
        condition.bind(mockConnection);
        condition.equal("id", 1).like("name", "test");

        List<Object> argsList = new ArrayList<>();
        String sql = condition.getSql(argsList);

        assertEquals(wrapper("id") + " = ? and " + wrapper("name") + " like ?", sql);
        assertEquals(2, argsList.size());
        assertEquals(1, argsList.get(0));
        assertEquals("%test%", argsList.get(1));
    }

    @Test
    public void testGetSqlWithOrCondition() throws SQLException {
        condition.bind(mockConnection);

        Condition subCondition1 = Condition.get().equal("id", 1).equal("age", 20);
        Condition subCondition2 = Condition.get().equal("name", "test").isNull("delete_time");
        condition.or(subCondition1, subCondition2);

        List<Object> argsList = new ArrayList<>();
        String sql = condition.getSql(argsList);

        assertEquals("( ( " + wrapper("id") + " = ? and " + wrapper("age") + " = ? ) or ( " + wrapper("name") + " = ? and " + wrapper("delete_time") + " is null ) )", sql);
        assertEquals(3, argsList.size());
        assertEquals(1, argsList.get(0));
        assertEquals(20, argsList.get(1));
        assertEquals("test", argsList.get(2));
    }

    @Test
    public void testGetSqlWithGroupByAndOrderBy() throws Exception {
        condition.bind(mockConnection);
        condition.equal("status", 1)
                .groupBy("type")
                .asc("create_time")
                .desc("update_time");

        List<Object> argsList = new ArrayList<>();
        String sql = condition.getSql(argsList);


        assertEquals(wrapper("status") + " = ? group by " + wrapper("type") + " order by " + wrapper("create_time") + " asc, " + wrapper("update_time") + " desc", sql);

        assertEquals(1, argsList.size());
        assertEquals(1, argsList.get(0));
    }

    @Test
    public void testFCondition() {
        SysLogRecord sysLogRecord = new SysLogRecord();
        FCondition fCondition = FCondition.get();
        FCondition equal = fCondition
                .equal(sysLogRecord::getId, "222")
                .equal(sysLogRecord::getTag, "tag1")
                .isNull(sysLogRecord::getErrorInfo);

        equal.bind(mockConnection);
        ArrayList<Object> objects = ListTs.newArrayList();
        String sql = equal.getSql(objects);
        System.out.println(sql);
        System.out.println(JacksonUtil.toJson(objects));
    }

    @AfterEach
    public void afterEach() throws SQLException {
        mockConnection.close();
    }

}
