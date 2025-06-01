package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.plugin.seed.Easy4jSeed;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = DBAccessTest.class, properties = {
        "spring.datasource.username=easy4j",
        "spring.datasource.password=easy4j",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.h2.console.enabled=true",
        "spring.h2.console.path=/h2-console"
})
class DBAccessTest {


    @Autowired
    DataSource dataSource;

    Connection connection;

    Dialect dialect;


    DBAccess dbAccess;

    Easy4jSeed easy4jSeed;

    @BeforeEach
    void init() {
        //MockitoAnnotations.openMocks(this);


        dbAccess = new JdbcDbAccess();
        dbAccess.init(dataSource);
        try {
            connection = dataSource.getConnection();
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
    }

    @AfterEach
    void afterEach() {
        //MockitoAnnotations.openMocks(this);
        try {
            connection.close();
        } catch (SQLException ignored) {

        }
    }

    List<SysLogRecord> getList(int i) {
        List<SysLogRecord> list = ListTs.newArrayList();
        for (int j = 0; j < i; j++) {
            String s = easy4jSeed.nextIdStr();
            SysLogRecord sysLogRecord = new SysLogRecord();
            sysLogRecord.setId(s);
            sysLogRecord.setStatus("1");
            sysLogRecord.setRemark("remark test" + i);
            sysLogRecord.setTag("tag test" + i);
            sysLogRecord.setParams("params test" + i);
            sysLogRecord.setTagDesc("tagDesc test" + i);
            sysLogRecord.setCreateDate(new Date());
            sysLogRecord.setTraceId("traceId" + i);
            sysLogRecord.setProcessTime(String.valueOf(i * 2));
            sysLogRecord.setErrorInfo("error into test" + i);
            sysLogRecord.setOperateCode("operate code " + i);
            sysLogRecord.setOperateName("operate name" + i);
            sysLogRecord.setTargetId("target id" + i);
            sysLogRecord.setTargetId2("target id2" + i);
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
        SysLogRecord sysLogRecord1 = dbAccess.updateByPrimaryKey(sysLogRecord, SysLogRecord.class);

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
        SysLogRecord sysLogRecord2 = dbAccess.updateByPrimaryKeySelective(sysLogRecord1, SysLogRecord.class);


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
    }

    @Test
    void selectByPrimaryKey() {
    }

    @Test
    void selectByPrimaryKeys() {
    }

    @Test
    void countBy() {
    }

    @Test
    void countByMap() {
    }

    @Test
    void runScript() {
    }

    @Test
    void getConnection() {
    }

    @Test
    void deleteAll() {
    }

    @Test
    void deleteByPrimaryKey() {
    }

    @Test
    void deleteByMap() {
    }

    @Test
    void selectByObject() {
    }

    @Test
    void selectByMap() {
    }

    @Test
    void selectOneByMap() {
    }

    @Test
    void existByPrimaryKey() {
    }

    @Test
    void countByCondition() {
    }

    @Test
    void deleteByCondition() {
    }

    @Test
    void selectByCondition() {
    }

    @Test
    void updateByCondition() {
    }

    @Test
    void existByCondition() {
    }
}