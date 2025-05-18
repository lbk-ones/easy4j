package ej.spring.boot.starter.test;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.utils.ListTs;
import easy4j.module.seed.CommonKey;
import easy4j.module.sentinel.EnableFlowDegrade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Easy4JStarter(
        serverPort = 10001,
        serverName = "build-server",
        serviceDesc = "测试服务",
        author = "bokun.li",
        enableH2 = false,
        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false"
        // 使用h2当数据库
)
@EnableFlowDegrade
@SpringBootTest(classes = AppTest.class)
public class AppTest {
    @Autowired
    DataSource dataSource;

    // saveRecord
    @Test
    public void test1() throws SQLException {
        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId(CommonKey.gennerString());
        sysLogRecord.setRemark("this is the remark");
        sysLogRecord.setParams("this is the params");
        sysLogRecord.setTag("测试新增");
        sysLogRecord.setTagDesc("这是" + DateUtil.formatDate(new Date()) + "的测试用例");
        sysLogRecord.setStatus("1");
        sysLogRecord.setErrorInfo("cause by this is a error info list.....");
        sysLogRecord.setCreateDate(new Date());
        String s = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("uuid---" + s);
        sysLogRecord.setTraceId(s);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        System.out.println("更新条数---" + i);
    }

    // saveRecord and queryRecord
    @Test
    public void test2() throws SQLException {
        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId(CommonKey.gennerString());
        sysLogRecord.setRemark("this is the remark");
        sysLogRecord.setParams("this is the params");
        sysLogRecord.setTag("测试新增和查询");
        sysLogRecord.setTagDesc("这是" + DateUtil.formatDate(new Date()) + "的测试用例");
        sysLogRecord.setStatus("1");
        sysLogRecord.setErrorInfo("cause by this is a error info list.....");
        sysLogRecord.setCreateDate(new Date());
        String s = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("uuid---" + s);
        sysLogRecord.setTraceId(s);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        System.out.println("更新条数---" + i);
        SysLogRecord sysLogRecordById = dbAccess.getObjectByPrimaryKey(SysLogRecord.class, sysLogRecord.getId());
        System.out.println(JSON.toJSONString(sysLogRecordById));
        String traceId = sysLogRecordById.getTraceId();
        System.out.println(traceId.equals(s));

    }

    // test update one
    @Test
    public void test3() throws SQLException {
        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId(CommonKey.gennerString());
        sysLogRecord.setRemark("this is the remark");
        sysLogRecord.setParams("this is the params");
        sysLogRecord.setTag("测试新增和查询");
        sysLogRecord.setTagDesc("这是" + DateUtil.formatDate(new Date()) + "的测试用例");
        sysLogRecord.setStatus("1");
        sysLogRecord.setErrorInfo("cause by this is a error info list.....");
        sysLogRecord.setCreateDate(new Date());
        String s = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("uuid---" + s);
        sysLogRecord.setTraceId(s);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);

        SysLogRecord sysLogRecordById = dbAccess.getObjectByPrimaryKey(SysLogRecord.class, sysLogRecord.getId());
        System.out.println("更新前--->" + JSON.toJSONString(sysLogRecordById));
        sysLogRecordById.setTraceId("123");
        sysLogRecordById.setProcessTime("8988");
        SysLogRecord sysLogRecord1 = dbAccess.updateByPrimaryKeySelective(sysLogRecordById, SysLogRecord.class);
        System.out.println("更新后--->" + JSON.toJSONString(sysLogRecord1));

    }

    // batch update
    @Test
    public void test4() throws SQLException {
        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        List<SysLogRecord> needUpdateList = ListTs.newArrayList();
        for (int i = 0; i < 3; i++) {
            SysLogRecord sysLogRecord = new SysLogRecord();
            sysLogRecord.setId(CommonKey.gennerString());
            sysLogRecord.setRemark("this is the remark" + i);
            sysLogRecord.setParams("this is the params" + i);
            sysLogRecord.setTag("测试新增和查询");
            sysLogRecord.setTagDesc("这是" + DateUtil.formatDate(new Date()) + "的测试用例" + i);
            sysLogRecord.setStatus("1");
            sysLogRecord.setErrorInfo("cause by this is a error info list....." + i);
            sysLogRecord.setCreateDate(new Date());
            String s = UUID.randomUUID().toString().replaceAll("-", "");
            sysLogRecord.setTraceId(s);
            int i1 = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
            if (i1 > 0) {
                needUpdateList.add(sysLogRecord);
            }
        }
        for (int i = 0; i < needUpdateList.size(); i++) {
            SysLogRecord sysLogRecord = needUpdateList.get(i);
            sysLogRecord.setTraceId("9999-" + i);
            sysLogRecord.setErrorInfo(null);
        }
        int i = dbAccess.updateListByPrimaryKey(needUpdateList, SysLogRecord.class);
        List<SysLogRecord> objectByPrimaryKeys = dbAccess.getObjectByPrimaryKeys(SysLogRecord.class, ListTs.mapList(needUpdateList, SysLogRecord::getId));
        for (SysLogRecord sysLogRecord : objectByPrimaryKeys) {
            System.out.println(JSON.toJSONString(sysLogRecord, JSONWriter.Feature.WriteMapNullValue));
        }
        System.out.println("更新" + i + "条数据");

    }

    @Test
    public void countBy() throws SQLException {
        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        long l = dbAccess.countBy(new SysLogRecord());
        System.out.println("总共多少条--->" + l);
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setErrorInfo("is not null");
        long l2 = dbAccess.countBy(sysLogRecord);
        System.out.println("错误信息总共多少条--->" + l2);

        SysLogRecord sysLogRecord2 = new SysLogRecord();
        sysLogRecord2.setTag("测试新增");
        long l3 = dbAccess.countBy(sysLogRecord2);
        System.out.println("测试新增总共多少条--->" + l3);

    }
}
