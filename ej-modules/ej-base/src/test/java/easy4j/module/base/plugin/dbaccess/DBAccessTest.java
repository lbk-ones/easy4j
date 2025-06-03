package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import easy4j.module.base.plugin.dbaccess.condition.FWhereBuild;
import easy4j.module.base.plugin.dbaccess.condition.WhereBuild;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.plugin.dbaccess.proxy.JdkProxyHandler;
import easy4j.module.base.plugin.seed.Easy4jSeed;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = DBAccessTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=easy4j",
        "spring.datasource.password=easy4j",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.h2.console.enabled=true",
        "spring.h2.console.path=/h2-console",
        "spring.datasource.hikari.maximum-pool-size=50"
})
public class DBAccessTest {


    @Autowired
    DataSource dataSource;

    Connection connection;

    Dialect dialect;


    DBAccess dbAccess;

    Easy4jSeed easy4jSeed;

    @BeforeEach
    void init() {
        //MockitoAnnotations.openMocks(this);


        JdbcDbAccess jdbcDbAccess = new JdbcDbAccess();
        JdkProxyHandler jdkProxyHandler = new JdkProxyHandler(jdbcDbAccess, dataSource);
        dbAccess = (DBAccess) jdkProxyHandler.createProxy();

        dbAccess.init(dataSource);
        dbAccess.printPrintLog(true);
        try (Connection connection = dataSource.getConnection()) {
            dialect = JdbcHelper.getDialect(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        Snowflake snowflake = new Snowflake();
        easy4jSeed = new Easy4jSeed() {
            @Override
            public String nextIdStr() {

                return snowflake.nextIdStr();
            }

            @Override
            public long nextIdLong() {
                return snowflake.nextId();
            }
        };
        // 每一次都全部删除
        dbAccess.deleteAll(SysLogRecord.class);


    }


    @AfterEach
    void afterEach() {
        //MockitoAnnotations.openMocks(this);
//        try {
//            //connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
    }

    List<SysLogRecord> getList(int i) {
        List<SysLogRecord> list = ListTs.newArrayList();
        for (int j = 0; j < i; j++) {
            String s = easy4jSeed.nextIdStr();
            SysLogRecord sysLogRecord = new SysLogRecord();
            sysLogRecord.setId(s);
            sysLogRecord.setStatus("1");
            sysLogRecord.setRemark("remark test" + j);
            sysLogRecord.setTag("tag test" + j);
            sysLogRecord.setParams("params test" + j);
            sysLogRecord.setTagDesc("tagDesc test" + j);
            sysLogRecord.setCreateDate(new Date());
            sysLogRecord.setTraceId("traceId" + j);
            sysLogRecord.setProcessTime(String.valueOf(j * 2));
            sysLogRecord.setErrorInfo("error into test" + j);
            sysLogRecord.setOperateCode("operate code " + j);
            sysLogRecord.setOperateName("operate name" + j);
            sysLogRecord.setTargetId("target id" + j);
            sysLogRecord.setTargetId2("target id2" + j);
            list.add(sysLogRecord);
        }
        return list;
    }

    @Test
    void saveOne() {
        SysLogRecord sysLogRecord = getList(1).get(0);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i);

        SysLogRecord sysLogRecord1 = dbAccess.selectByPrimaryKey(sysLogRecord.getId(), SysLogRecord.class);

        String json = JacksonUtil.toJson(sysLogRecord);
        String json2 = JacksonUtil.toJson(sysLogRecord1);
        assertEquals(json, json2);
    }

    @Test
    void saveList() {
        List<SysLogRecord> list = getList(10);
        int i = dbAccess.saveList(list, SysLogRecord.class);
        assertEquals(10, i);
        List<Object> collect = list.stream().map(SysLogRecord::getId).collect(Collectors.toList());
        List<SysLogRecord> ts = dbAccess.selectByPrimaryKeys(collect, SysLogRecord.class);
        assertEquals(JacksonUtil.toJson(list), JacksonUtil.toJson(ts));
    }

    @Test
    void updateByPrimaryKey() {
        SysLogRecord sysLogRecord = getList(1).get(0);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i);
        sysLogRecord.setTag("change name tag");
        sysLogRecord.setCreateDate(new Date());
        SysLogRecord sysLogRecord1 = dbAccess.updateByPrimaryKey(sysLogRecord, SysLogRecord.class, true);

        assertEquals(JacksonUtil.toJson(sysLogRecord), JacksonUtil.toJson(sysLogRecord1));


    }

    @Test
    void saveOrUpdateByPrimaryKey() {
        // 新增
        SysLogRecord sysLogRecord = getList(1).get(0);
        int i = dbAccess.saveOrUpdateByPrimaryKey(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i);

        sysLogRecord.setTag("tag name saveOrUpdateByPrimaryKey");
        int i2 = dbAccess.saveOrUpdateByPrimaryKey(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i2);

        SysLogRecord sysLogRecord1 = dbAccess.selectByPrimaryKey(sysLogRecord.getId(), SysLogRecord.class);

        String json = JacksonUtil.toJson(sysLogRecord);
        String json2 = JacksonUtil.toJson(sysLogRecord1);
        assertEquals(json, json2);
    }

    @Test
    void updateByPrimaryKeySelective() {

        SysLogRecord sysLogRecord = getList(1).get(0);
        int i = dbAccess.saveOrUpdateByPrimaryKey(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i);

        SysLogRecord sysLogRecord1 = new SysLogRecord();
        sysLogRecord1.setId(sysLogRecord.getId());
        sysLogRecord1.setTag("updateByPrimaryKeySelective");
        SysLogRecord sysLogRecord2 = dbAccess.updateByPrimaryKeySelective(sysLogRecord1, SysLogRecord.class, true);


        BeanUtil.copyProperties(sysLogRecord1, sysLogRecord, CopyOptions.create().ignoreNullValue());
        String json = JacksonUtil.toJson(sysLogRecord);
        String json2 = JacksonUtil.toJson(sysLogRecord2);
        assertEquals(json, json2);


    }

    @Test
    void updateListByPrimaryKey() {

        List<SysLogRecord> list = getList(10);
        int i = dbAccess.saveList(list, SysLogRecord.class);
        assertEquals(10, i);

        List<SysLogRecord> ts = dbAccess.selectByPrimaryKeysT(list, SysLogRecord.class);

        assertEquals(JacksonUtil.toJson(list), JacksonUtil.toJson(ts));

        for (SysLogRecord t : ts) {
            t.setTag("pki" + i);
            t.setCreateDate(new Date());
        }

        int i1 = dbAccess.updateListByPrimaryKey(ts, SysLogRecord.class);
        assertEquals(10, i1);

        List<SysLogRecord> list1 = dbAccess.selectByPrimaryKeysT(ts, SysLogRecord.class);

        assertEquals(JacksonUtil.toJson(ts), JacksonUtil.toJson(list1));

    }

    @Test
    void updateListByPrimaryKeySelective() {

        List<SysLogRecord> list = getList(10);
        int i = dbAccess.saveList(list, SysLogRecord.class);
        assertEquals(10, i);

        List<SysLogRecord> ts = dbAccess.selectByPrimaryKeysT(list, SysLogRecord.class);

        assertEquals(JacksonUtil.toJson(list), JacksonUtil.toJson(ts));

        List<SysLogRecord> objects = ListTs.newArrayList();
        Map<String, String> map = Maps.newHashMap();
        for (int i1 = 0; i1 < ts.size(); i1++) {
            SysLogRecord sysLogRecord = ts.get(i1);
            SysLogRecord t2 = new SysLogRecord();
            t2.setTag("pki" + i1);
            t2.setCreateDate(new Date());
            objects.add(t2);
            map.put(t2.getTag(), sysLogRecord.getId());
        }

        assertThrows(EasyException.class, () -> {
            dbAccess.updateListByPrimaryKeySelective(objects, SysLogRecord.class);
        });

        for (SysLogRecord object : objects) {
            String tag = object.getTag();
            String s = map.get(tag);
            object.setId(s);
        }
        int i1 = dbAccess.updateListByPrimaryKeySelective(objects, SysLogRecord.class);
        assertEquals(10, i1);

        List<SysLogRecord> list1 = dbAccess.selectByPrimaryKeysT(objects, SysLogRecord.class);

        Map<String, SysLogRecord> stringSysLogRecordMap = ListTs.mapOne(objects, SysLogRecord::getId);

        for (SysLogRecord t : ts) {
            String id = t.getId();
            SysLogRecord sysLogRecord = stringSysLogRecordMap.get(id);
            BeanUtil.copyProperties(sysLogRecord, t, CopyOptions.create().ignoreNullValue());
        }

        assertEquals(JacksonUtil.toJson(ts), JacksonUtil.toJson(list1));

    }

    @Test
    void selectOne() {
        SysLogRecord sysLogRecord = getList(1).get(0);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i);

        String sql = "select * from " + StrUtil.toUnderlineCase(SysLogRecord.class.getSimpleName()) + " where id = ?";
        SysLogRecord sysLogRecord1 = dbAccess.selectOne(sql, SysLogRecord.class, sysLogRecord.getId());

        assertTrue(Objects.nonNull(sysLogRecord1));

        assertEquals(JacksonUtil.toJson(sysLogRecord), JacksonUtil.toJson(sysLogRecord1));

    }

    @Test
    void selectList() {

        List<SysLogRecord> sysLogRecord = getList(2);
        int i = dbAccess.saveList(sysLogRecord, SysLogRecord.class);
        assertEquals(2, i);

        String sql = "select * from " + StrUtil.toUnderlineCase(SysLogRecord.class.getSimpleName());
        List<SysLogRecord> sysLogRecord1 = dbAccess.selectList(sql, SysLogRecord.class);

        assertTrue(Objects.nonNull(sysLogRecord1));

        assertEquals(JacksonUtil.toJson(sysLogRecord), JacksonUtil.toJson(sysLogRecord1));
    }

    @Test
    void selectAll() {
        List<SysLogRecord> sysLogRecord = getList(20);
        int i = dbAccess.saveList(sysLogRecord, SysLogRecord.class);
        assertEquals(20, i);

        List<SysLogRecord> list = dbAccess.selectAll(SysLogRecord.class);

        assertEquals(20, list.size());

        List<SysLogRecord> list2 = dbAccess.selectAll(SysLogRecord.class, "id");

        List<Map<String, Object>> collect = list2.stream().map(e -> BeanUtil.beanToMap(e, false, true)).collect(Collectors.toList());

        for (Map<String, Object> stringObjectMap : collect) {
            Set<String> strings = stringObjectMap.keySet();
            assertEquals(1, strings.size());
        }
    }

    @Test
    void selectListByPage() {
        List<SysLogRecord> sysLogRecord = getList(27);
        int i = dbAccess.saveList(sysLogRecord, SysLogRecord.class);
        assertEquals(27, i);

        Page<SysLogRecord> objectPage = new Page<>();
        objectPage.setPageNo(1);
        objectPage.setPageSize(6);
        QueryFilter queryFilter = new QueryFilter();
        queryFilter.setOrderBy("createDate");
        queryFilter.setOrder("desc");

//        dbAccess.selectListByPage(objectPage,queryFilter,SysLogRecord.class,)
    }

    @Test
    void selectByPrimaryKey() {

        SysLogRecord sysLogRecord = getList(1).get(0);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        assertEquals(1, i);

        SysLogRecord sysLogRecord1 = dbAccess.selectByPrimaryKey(sysLogRecord.getId(), SysLogRecord.class);
        assertNotNull(sysLogRecord1);
        SysLogRecord sysLogRecord2 = dbAccess.selectByPrimaryKey(sysLogRecord, SysLogRecord.class);

        assertNotNull(sysLogRecord2);

        assertEquals(JacksonUtil.toJson(sysLogRecord1), JacksonUtil.toJson(sysLogRecord2));


    }

    @Test
    void selectByPrimaryKeys() {

        List<SysLogRecord> sysLogRecord = getList(10);
        int i = dbAccess.saveList(sysLogRecord, SysLogRecord.class);
        assertEquals(10, i);

        List<SysLogRecord> sysLogRecord1 = dbAccess.selectByPrimaryKeys(ListTs.objectToListObject(sysLogRecord, Function.identity()), SysLogRecord.class);
        assertNotNull(sysLogRecord1);

        List<SysLogRecord> sysLogRecord2 = dbAccess.selectByPrimaryKeys(ListTs.objListToListObjectByT(sysLogRecord, SysLogRecord::getId), SysLogRecord.class);

        assertNotNull(sysLogRecord2);

        assertEquals(JacksonUtil.toJson(sysLogRecord1), JacksonUtil.toJson(sysLogRecord2));
    }

    @Test
    void countBy() {

        List<SysLogRecord> sysLogRecord = getList(10);
        int i = dbAccess.saveList(sysLogRecord, SysLogRecord.class);
        assertEquals(10, i);

        SysLogRecord sysLogRecord1 = new SysLogRecord();
        // 这里不是很好测其他条件 就测个总数吧
        long l = dbAccess.countBy(sysLogRecord1);
        assertEquals(l, i);

        // assertEquals(JacksonUtil.toJson(sysLogRecord1), JacksonUtil.toJson(sysLogRecord2));

    }

    @Test
    void countByMap() {

        List<SysLogRecord> sysLogRecord = getList(10);
        int i = dbAccess.saveList(sysLogRecord, SysLogRecord.class);
        assertEquals(10, i);

        SysLogRecord sysLogRecord1 = sysLogRecord.get(2);

        Dict dict = Dict.create();
        // 这里不是很好测其他条件 就测个总数吧
        long l = dbAccess.countByMap(dict, SysLogRecord.class);
        assertEquals(l, i);


        Dict dict1 = Dict.create();
        dict1.set("id", sysLogRecord1.getId());
        long l1 = dbAccess.countByMap(dict1, SysLogRecord.class);
        assertEquals(1, l1);

        Dict dict2 = Dict.create();
        dict2.set(LambdaUtil.getFieldName(SysLogRecord::getTraceId), sysLogRecord1.getTraceId());
        long l2 = dbAccess.countByMap(dict2, SysLogRecord.class);
        assertEquals(1, l2);
    }

    @Test
    void runScript() {
        String sql = "CREATE TABLE SYS_LOCK_TEMP\n" +
                "(\n" +
                "    ID          VARCHAR(36) PRIMARY KEY COMMENT '主键',\n" +
                "    CREATE_DATE TIMESTAMP COMMENT '操作时间',\n" +
                "    EXPIRE_DATE TIMESTAMP COMMENT '过期时间',\n" +
                "    REMARK      VARCHAR(36) COMMENT '备注'\n" +
                ");\n" +
                "CREATE INDEX IDX_SYS_LOCK_TEMP_CREATE_DATE ON SYS_LOCK_TEMP (CREATE_DATE);";
        InputStream inputStream = new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);
        dbAccess.runScript(inputStreamResource);

        SysLockT sysLockT = new SysLockT();
        sysLockT.setId("testId");
        sysLockT.setCreateDate(new Date());
        int i = dbAccess.saveOne(sysLockT, SysLockT.class);
        assertEquals(1, i);

        int i2 = (int) dbAccess.countBy(sysLockT);
        assertEquals(1, i2);
    }

    @Data
    @JdbcTable(name = "sys_lock_temp")
    public static class SysLockT implements Serializable {

        @JdbcColumn(isPrimaryKey = true)
        private String id;

        /**
         * 创建时间
         */
        private Date createDate;

        /**
         * 过期时间
         */
        private Date expireDate;

        /**
         * 备注 这一次锁的详细信息
         * 比如说是谁成功抢走的
         * 或者说加锁具体内容
         */
        private String remark;
    }

    @Test
    void deleteAll() {
        // 插入测试数据
        List<SysLogRecord> list = getList(5);
        dbAccess.saveList(list, SysLogRecord.class);

        // 删除所有数据
        dbAccess.deleteAll(SysLogRecord.class);

        // 验证数据为空
        long count = dbAccess.countBy(new SysLogRecord());
        assertEquals(0, count);
    }

    @Test
    void deleteByPrimaryKey() {

        // 插入数据
        SysLogRecord record = getList(1).get(0);
        dbAccess.saveOne(record, SysLogRecord.class);

        // 删除数据
        dbAccess.deleteByPrimaryKey(record, SysLogRecord.class);

        // 验证数据删除
        SysLogRecord deleted = dbAccess.selectByPrimaryKey(record, SysLogRecord.class);
        assertNull(deleted);

    }

    @Test
    void deleteByMap() {

        // 插入测试数据
        List<SysLogRecord> list = getList(3);
        dbAccess.saveList(list, SysLogRecord.class);

        // 构造删除条件：删除 tag 为 "tag test0" 的数据
        Dict condition = Dict.create()
                .set("tag", "tag test0");

        // 删除符合条件的数据
        int deletedCount = dbAccess.deleteByMap(condition, SysLogRecord.class);
        assertEquals(1, deletedCount); // 预期删除 1 条

        // 验证剩余数据量
        long remainingCount = dbAccess.countBy(new SysLogRecord());
        assertEquals(2, remainingCount);

    }

    @Test
    void selectByObject() {
        // 插入测试数据
        List<SysLogRecord> list = getList(2);
        dbAccess.saveList(list, SysLogRecord.class);

        // 构造查询条件：查询 tag 为 "tag test0" 的数据
        SysLogRecord condition = new SysLogRecord();
        condition.setTag("tag test0");

        // 查询符合条件的数据
        List<SysLogRecord> result = dbAccess.selectByObject(condition, SysLogRecord.class);

        // 验证结果数量和内容
        assertEquals(1, result.size());
        assertEquals("tag test0", result.get(0).getTag());
    }

    @Test
    void selectByMap() {

        // 插入测试数据
        SysLogRecord record = getList(1).get(0);
        dbAccess.saveOne(record, SysLogRecord.class);

        // 构造查询条件：根据 traceId 查询
        Dict condition = Dict.create()
                .set("traceId", record.getTraceId());

        // 查询数据
        List<SysLogRecord> result = dbAccess.selectByMap(condition, SysLogRecord.class);

        // 验证结果
        assertEquals(1, result.size());
        assertEquals(JacksonUtil.toJson(record), JacksonUtil.toJson(result.get(0)));
    }

    @Test
    void selectOneByMap() {
        // 插入数据
        SysLogRecord record = getList(1).get(0);
        dbAccess.saveOne(record, SysLogRecord.class);

        // 构造唯一条件（主键）
        Dict condition = Dict.create()
                .set("id", record.getId());

        // 查询单条数据
        SysLogRecord result = dbAccess.selectOneByMap(condition, SysLogRecord.class);

        // 验证结果
        assertNotNull(result);
        assertEquals(JacksonUtil.toJson(record), JacksonUtil.toJson(result));

    }

    @Test
    void existByPrimaryKey() {
        // 插入数据
        SysLogRecord record = getList(1).get(0);
        dbAccess.saveOne(record, SysLogRecord.class);

        // 验证存在
        boolean exists = dbAccess.existByPrimaryKey(record.getId(), SysLogRecord.class);
        assertTrue(exists);

        // 删除数据后验证不存在
        dbAccess.deleteByPrimaryKey(record, SysLogRecord.class);
        exists = dbAccess.existByPrimaryKey(record.getId(), SysLogRecord.class);
        assertFalse(exists);
    }

    @Test
    void countByCondition() {
        // 插入测试数据（status 均为 "1"）
        List<SysLogRecord> list = getList(5);
        dbAccess.saveList(list, SysLogRecord.class);

        WhereBuild equal = FWhereBuild.get(SysLogRecord.class)
                .equal(SysLogRecord::getStatus, "1");

        // 统计数量
        long count = dbAccess.countByCondition(equal, SysLogRecord.class);
        assertEquals(5, count);
    }

    @Test
    void deleteByCondition() {

        // 插入测试数据（status 均为 "1"）
        List<SysLogRecord> list = getList(5);
        dbAccess.saveList(list, SysLogRecord.class);

        WhereBuild equal = FWhereBuild.get(SysLogRecord.class)
                .equal(SysLogRecord::getStatus, "1");

        // 统计数量
        long count = dbAccess.deleteByCondition(equal, SysLogRecord.class);
        assertEquals(5, count);
    }

    @Test
    void selectByCondition() {

        List<SysLogRecord> list = getList(5);
        dbAccess.saveList(list, SysLogRecord.class);

        SysLogRecord sysLogRecord = list.get(0);
        SysLogRecord sysLogRecord1 = list.get(1);
        List<String> list1 = ListTs.asList(sysLogRecord.getId(), sysLogRecord1.getId());


        WhereBuild equal = FWhereBuild.get(SysLogRecord.class)
                .in(SysLogRecord::getId, list1);

        // 统计数量
        long count = dbAccess.deleteByCondition(equal, SysLogRecord.class);
        assertEquals(2, count);
    }

    @Test
    void updateByCondition() {

        List<SysLogRecord> list = getList(5);
        dbAccess.saveList(list, SysLogRecord.class);

        SysLogRecord sysLogRecord = list.get(0);
        SysLogRecord sysLogRecord1 = list.get(1);
        List<String> list1 = ListTs.asList(sysLogRecord.getId(), sysLogRecord1.getId());


        WhereBuild equal = FWhereBuild.get(SysLogRecord.class)
                .in(SysLogRecord::getId, list1);

        SysLogRecord sysLogRecord2 = new SysLogRecord();
        sysLogRecord2.setTag("change tag");
        // 统计数量
        long count = dbAccess.updateByCondition(equal, sysLogRecord2, SysLogRecord.class);
        assertEquals(2, count);

        WhereBuild equal2 = FWhereBuild.get(SysLogRecord.class)
                .equal(SysLogRecord::getTag, "change tag");

        // 统计数量
        long count2 = dbAccess.countByCondition(equal2, SysLogRecord.class);
        assertEquals(2, count2);

    }

    @Test
    void existByCondition() {

        List<SysLogRecord> list = getList(5);
        dbAccess.saveList(list, SysLogRecord.class);

        SysLogRecord sysLogRecord = list.get(0);
        SysLogRecord sysLogRecord1 = list.get(1);
        List<String> list1 = ListTs.asList(sysLogRecord.getId(), sysLogRecord1.getId());


        WhereBuild equal = FWhereBuild.get(SysLogRecord.class)
                .in(SysLogRecord::getId, list1);

        SysLogRecord sysLogRecord2 = new SysLogRecord();
        sysLogRecord2.setTag("change tag2");
        // 统计数量
        long count = dbAccess.updateByCondition(equal, sysLogRecord2, SysLogRecord.class);
        assertEquals(2, count);

        WhereBuild equal2 = FWhereBuild.get(SysLogRecord.class)
                .equal(SysLogRecord::getTag, "change tag2");

        // 是否会存在
        boolean count2 = dbAccess.existByCondition(equal2, SysLogRecord.class);
        assertTrue(count2);
    }
}