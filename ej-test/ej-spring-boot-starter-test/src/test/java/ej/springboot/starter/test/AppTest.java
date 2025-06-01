/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ej.springboot.starter.test;

import cn.hutool.core.date.DateUtil;
import easy4j.module.base.context.Easy4jContext;
import easy4j.module.base.log.DbLog;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.base.starter.Easy4j;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;
import easy4j.module.seed.CommonKey;
import easy4j.module.sentinel.EnableFlowDegrade;
import ej.spring.boot.starter.server.StartTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Easy4JStarter(
        serverPort = 10001,
        serverName = "build-server",
        serviceDesc = "测试服务",
        author = "bokun.li",
        enableH2 = true
        //ejDataSourceUrl = "jdbc:postgresql://localhost:5432/test@root:123456"
        //ejDataSourceUrl = "jdbc:mysql://localhost:3306/vcc_portal_v1@root:123456",
        // 使用h2当数据库
)
/**
 * AppTest
 *
 * @author bokun.li
 * @date 2025-05
 */
@EnableFlowDegrade
@SpringBootTest(classes = StartTest.class)
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
        SysLogRecord sysLogRecordById = dbAccess.selectByPrimaryKey(sysLogRecord.getId(), SysLogRecord.class);
        System.out.println(JacksonUtil.toJson(sysLogRecordById));
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

        SysLogRecord sysLogRecordById = dbAccess.selectByPrimaryKey(sysLogRecord.getId(), SysLogRecord.class);
        System.out.println("更新前--->" + JacksonUtil.toJson(sysLogRecordById));
        sysLogRecordById.setTraceId("123");
        sysLogRecordById.setProcessTime("8988");
        SysLogRecord sysLogRecord1 = dbAccess.updateByPrimaryKeySelective(sysLogRecordById, SysLogRecord.class);
        System.out.println("更新后--->" + JacksonUtil.toJson(sysLogRecord1));

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
        List<SysLogRecord> objectByPrimaryKeys = dbAccess.selectByPrimaryKeys(ListTs.mapList(needUpdateList, SysLogRecord::getId), SysLogRecord.class);
        for (SysLogRecord sysLogRecord : objectByPrimaryKeys) {
            System.out.println(JacksonUtil.toJson(sysLogRecord));
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

    @Test
    public void testDuplicate() throws SQLException {
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
        sysLogRecord.setTraceId(s);
        int i1 = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);

        assertThatThrownBy(() -> {
            dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        }).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void testDbLog() throws SQLException {

        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        List<SysLogRecord> needUpdateList = ListTs.newArrayList();
        for (int i = 0; i < 10; i++) {
            SysLogRecord sysLogRecord = new SysLogRecord();
            sysLogRecord.setId(CommonKey.gennerString());
            sysLogRecord.setRemark("this is the remark" + i);
            sysLogRecord.setParams("this is the params" + i);
            sysLogRecord.setTag("测试新增和查询");
            sysLogRecord.setTagDesc("这是" + DateUtil.formatDate(new Date()) + "的测试用例" + i);
            sysLogRecord.setStatus("1");
            sysLogRecord.setErrorInfo("cause by this is a error info list....." + i);
            sysLogRecord.setCreateDate(DateUtil.offsetDay(new Date(), -10));
            String s = UUID.randomUUID().toString().replaceAll("-", "");
            sysLogRecord.setTraceId(s);
            int i1 = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
            if (i1 > 0) {
                needUpdateList.add(sysLogRecord);
            }
        }


        Easy4jContext context = Easy4j.getContext();
        DbLog dbLog = context.get(DbLog.class);
        Date startTime = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -7)).toJdkDate();
        dbLog.clearLog(startTime);
    }

    @Test
    public void testDbLog2() throws SQLException {

        Easy4jContext context = Easy4j.getContext();
        DbLog dbLog = context.get(DbLog.class);
        Date startTime = DateUtil.endOfDay(DateUtil.offsetDay(new Date(), -7));
        dbLog.clearLog(startTime);

        DbLog.beginLog("test", "test222", "test222");
        System.out.println(DbLog.getDeque().size());
        SysLogRecord sysLogRecord = new SysLogRecord();
        String jsonContainNull = JacksonUtil.toJsonContainNull(sysLogRecord);
        DbLog.putRemark(jsonContainNull);
        DbLog.putRemark("step2");
        DbLog.putRemark("step1");
        String id = DbLog.getParams(SysLogRecord::getId, "");
        System.out.println("-- id " + id);
        DbLog.beginLog("test2", "test23444", "tesggaahgat222");
        System.out.println(DbLog.getDeque().size());
//        DbLog.putRemark("loop2");
        System.out.println(DbLog.getDeque().size());
        String params = DbLog.getParams(SysLogRecord::getId, "");
        System.out.println("first id " + params);
        DbLog.endLog();
        System.out.println(DbLog.getDeque().size());

        String id2 = DbLog.getParams(SysLogRecord::getId, "");
        System.out.println("two id " + id2);
        DbLog.endLog();
        System.out.println(DbLog.getDeque() == null);


        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);

        SysLogRecord objectByPrimaryKey = dbAccess.selectByPrimaryKey(id, SysLogRecord.class);
        System.out.println(JacksonUtil.toJson(objectByPrimaryKey));

    }


    @AfterAll
    public static void wtt() {
        System.out.println("close----ConditionTest---");
    }
}
