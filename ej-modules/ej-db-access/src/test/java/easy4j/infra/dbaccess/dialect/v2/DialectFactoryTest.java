package easy4j.infra.dbaccess.dialect.v2;

import cn.hutool.core.bean.BeanUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


class DialectFactoryTest {

    @Test
    void get() throws SQLException {
        String s = "jdbc:mysql://localhost:3306/seata";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(s);
        TempDataSource tempDataSource = new TempDataSource(driverClassNameByUrl, s, "root", "123456");
        Connection connection = tempDataSource.getConnection();
        DialectV2 dialectV2 = DialectFactory.get(connection);
        SysLogRecord sysLock = new SysLogRecord();
        sysLock.setId(UUID.randomUUID().toString().replace("-", ""));
        sysLock.setRemark("备注");
        sysLock.setCreateDate(new Date());
        sysLock.setParams("{\"xx\":\"xgfagfA\"}");


        SysLogRecord sysLock2 = new SysLogRecord();
        sysLock2.setId(UUID.randomUUID().toString().replace("-", ""));
        sysLock2.setRemark("备注2");
        sysLock2.setCreateDate(new Date());
        sysLock2.setParams("{\"xx\":\"8080===\"}");

        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(sysLock);


        List<Map<String, Object>> objects = ListTs.newList();
        for (int i = 0; i < 5000; i++) {
            Map<String, Object> var2 = new HashMap<>();
            var2.put("test", "wwwttt" + i);
            var2.put("test_json", "{\"ga\": \"xcg" + i + "\"}");
            objects.add(var2);
        }

        PsResult testAuto = dialectV2.jdbcInsert(objects, "test_auto", null, 0, true, true);
        System.out.println(testAuto.getSql());
        System.out.println(testAuto.getEffectRows());
        for (Map<String, Object> object : objects) {
            System.out.println("result---->" + JacksonUtil.toJson(object));
        }

        PsResult sysLock1 = dialectV2.jdbcInsert(ListTs.asList(stringObjectMap, BeanUtil.beanToMap(sysLock2)), "sys_log_record", null, 500, true, true);
        WhereBuild whereBuild = WhereBuild.get(connection);
        whereBuild.equal("ID", "b590b5bc4e7342a684b80015e2d078e4");
        PsResult sysLogRecord = dialectV2.jdbcDelete("sys_log_record", null, true, true, whereBuild);
        System.out.println("deledte_" + sysLogRecord.getEffectRows());
        System.out.println(sysLock1.getSql());
        System.out.println(sysLock1.getCostTime());
        System.out.println(sysLock1.getEffectRows());
        System.out.println(JacksonUtil.toJson(stringObjectMap));
        connection.close();
    }
}