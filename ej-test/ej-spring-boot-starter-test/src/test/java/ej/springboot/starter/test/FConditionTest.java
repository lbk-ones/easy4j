package ej.springboot.starter.test;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.condition.FCondition;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.sentinel.EnableFlowDegrade;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Easy4JStarter(
        serverPort = 10001,
        serverName = "test-server",
        serviceDesc = "测试条件",
        author = "bokun.li",
        enableH2 = true
)
@EnableFlowDegrade
@SpringBootTest(classes = FConditionTest.class)
public class FConditionTest {

    User user;
    FCondition fCondition;

    private Connection mockConnection;
    private Dialect mockDialect;
    private cn.hutool.db.sql.Wrapper mockWrapper;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    public void setUp() throws SQLException {
        user = new User();
        fCondition = FCondition.get();
        mockConnection = dataSource.getConnection();
        mockDialect = JdbcHelper.getDialect(mockConnection);
        assert mockDialect != null;
        mockWrapper = mockDialect.getWrapper();
    }


    // 测试实体类（用于 Lambda 表达式获取字段名）
    @Data
    static class User {
        private Integer id;
        private String name;
        private Integer age;
        private Integer status;
        private Boolean isActive;
        private String createTime;
        private String updateTime;
        private String deleteTime;
        private String type;
    }

    // 获取字段名的辅助方法（验证 Lambda 解析是否正确）
    private String getName(Func0<?> func) {
        return StrUtil.toUnderlineCase(LambdaUtil.getFieldName(func));
    }

    private String getFieldName(Func0<?> func) {
        return LambdaUtil.getFieldName(func);
    }

    @Test
    public void testLambdaFieldNameExtraction() {
        // 验证 Lambda 表达式能否正确解析字段名
        assertEquals("name", getFieldName(user::getName));
        assertEquals("age", getFieldName(user::getAge));
        assertEquals("isActive", getFieldName(user::getIsActive));
    }

    @Test
    public void testSetConditionWithLambda() {
        FCondition condition = FCondition.get();
        condition.set(user::getName, "=", "test");
        assertEquals(1, condition.tuple.size());
        Tuple tuple = condition.tuple.get(0);

        assertEquals(true, tuple.get(0)); // 有符号
        assertEquals("and", tuple.get(1)); // 连接符
        assertEquals("name", tuple.get(2)); // 字段名（通过 Lambda 解析）
        assertEquals("=", tuple.get(3)); // 符号
        assertEquals("test", tuple.get(4)); // 值
    }

    @Test
    public void testEqualConditionWithLambda() {
        FCondition condition = FCondition.get();
        condition.equal(user::getAge, 18);
        Tuple tuple = condition.tuple.get(0);
        assertEquals("age", tuple.get(2));
        assertEquals("=", tuple.get(3));
        assertEquals(18, (int) tuple.get(4));
    }

    @Test
    public void testLikeConditionWithLambda() {
        FCondition condition = FCondition.get();
        condition.like(user::getName, "admin");
        Tuple tuple = condition.tuple.get(0);
        assertEquals("name", tuple.get(2));
        assertEquals("like", tuple.get(3));
        assertEquals("%admin%", tuple.get(4));
    }

    @Test
    public void testOrConditionWithFCondition() {
        FCondition subCondition1 = FCondition.get().equal(user::getName, "test");
        FCondition subCondition2 = FCondition.get().equal(user::getAge, 20);

        FCondition mainCondition = FCondition.get().or(subCondition1, subCondition2);
        assertEquals(1, mainCondition.tuple.size());
        Tuple orTuple = mainCondition.tuple.get(0);

        assertEquals(false, orTuple.get(0)); // 无符号（OR 条件组）
        assertEquals("or", orTuple.get(1));
        Object[] conditions = (Object[]) orTuple.get(2);
        assertEquals(2, conditions.length);
        assertTrue(conditions[0] instanceof FCondition);
        assertTrue(conditions[1] instanceof FCondition);
    }

    @Test
    public void testGroupByWithLambda() {
        FCondition condition = FCondition.get()
                .groupBy(user::getName, user::getAge);

        assertEquals(1, condition.groupTuple.size());
        Tuple groupTuple = condition.groupTuple.get(0);

        assertEquals(false, groupTuple.get(0)); // 无符号（GROUP BY）
        assertEquals("group by", groupTuple.get(1));
        String[] fields = (String[]) groupTuple.get(2);
        assertArrayEquals(new String[]{"name", "age"}, fields);
    }

    @Test
    public void testOrderByAscWithLambda() {
        FCondition condition = FCondition.get()
                .asc(user::getAge);

        assertEquals(1, condition.orderBy.size());
        Tuple orderTuple = condition.orderBy.get(0);

        assertEquals(false, orderTuple.get(0)); // 无符号（ORDER BY）
        assertEquals("order by", orderTuple.get(1));
        assertEquals("age asc", orderTuple.get(2));
    }

    @Test
    public void testOrderByDescWithLambda() {
        FCondition condition = FCondition.get()
                .desc(user::getName);
        Tuple orderTuple = condition.orderBy.get(0);
        assertEquals("name desc", orderTuple.get(2));
    }

    @Test
    public void testChainingMethods() {
        FCondition condition = FCondition.get()
                .equal(user::getName, "test")
                .gt(user::getAge, 18)
                .like(user::getIsActive, "true")
                .groupBy(user::getAge)
                .asc(user::getName)
                .desc(user::getIsActive);

        assertEquals(3, condition.tuple.size()); // 3 个单条件
        assertEquals(1, condition.groupTuple.size());
        assertEquals(2, condition.orderBy.size());
    }


    @Test
    public void testGetSqlSegment() {
        fCondition.equal(user::getId, 1);
        fCondition.bind(mockConnection);
        List<Object> argsList = new ArrayList<>();

        String sqlSegment = fCondition.getSqlSegment(fCondition.tuple.get(0), argsList, mockWrapper);
        assertEquals(wrapper(getName(user::getId)) + " = ?", sqlSegment);
        assertEquals(1, argsList.size());
        assertEquals(1, argsList.get(0));
    }

    @Test
    public void testGetSqlWithNoConnection() {
        assertThrows(EasyException.class, () -> fCondition.getSql(new ArrayList<>()));
    }

    public String wrapper(String field) {
        return mockWrapper.wrap(field);
    }

    @Test
    public void testGetSqlSimpleCondition() throws SQLException {
        fCondition.bind(mockConnection);
        fCondition.equal(user::getId, 1).like(user::getName, "test");

        List<Object> argsList = new ArrayList<>();
        String sql = fCondition.getSql(argsList);

        assertEquals(wrapper(getName(user::getId)) + " = ? and " + wrapper(getName(user::getName)) + " like ?", sql);
        assertEquals(2, argsList.size());
        assertEquals(1, argsList.get(0));
        assertEquals("%test%", argsList.get(1));
    }

    @Test
    public void testGetSqlWithOrCondition() throws SQLException {
        fCondition.bind(mockConnection);

        FCondition subCondition1 = FCondition.get().equal(user::getId, 1).equal(user::getAge, 20);
        FCondition subCondition2 = FCondition.get().equal(user::getName, "test").isNull(user::getDeleteTime);
        fCondition.or(subCondition1, subCondition2);

        List<Object> argsList = new ArrayList<>();
        String sql = fCondition.getSql(argsList);

        assertEquals("( ( " + wrapper(getName(user::getId)) + " = ? and " + wrapper(getName(user::getAge)) + " = ? ) or ( " + wrapper(getName(user::getName)) + " = ? and " + wrapper(getName(user::getDeleteTime)) + " is null ) )", sql);
        assertEquals(3, argsList.size());
        assertEquals(1, argsList.get(0));
        assertEquals(20, argsList.get(1));
        assertEquals("test", argsList.get(2));
    }

    @Test
    public void testGetSqlWithGroupByAndOrderBy() throws Exception {
        fCondition.bind(mockConnection);
        fCondition.clear();
        fCondition.equal(user::getStatus, 1)
                .groupBy(user::getType)
                .asc(user::getCreateTime)
                .desc(user::getUpdateTime);

        List<Object> argsList = new ArrayList<>();
        String sql = fCondition.getSql(argsList);


        assertEquals(wrapper(getName(user::getStatus)) + " = ? group by " + wrapper(getName(user::getType)) + " order by " + wrapper(getName(user::getCreateTime)) + " asc, " + wrapper(getName(user::getUpdateTime)) + " desc", sql);

        assertEquals(1, argsList.size());
        assertEquals(1, argsList.get(0));
    }


}