package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.utils.SqlType;
import easy4j.infra.common.utils.json.JacksonUtil;
import easy4j.infra.dbaccess.DBAccess;
import easy4j.infra.dbaccess.DBAccessFactory;
import easy4j.infra.dbaccess.TempDataSource;
import easy4j.infra.dbaccess.domain.SysLogRecord;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.IOpMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.TableMetadata;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySQLFieldTypeTest {

    @Test
    void getByClass() {
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(String.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Date.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Date.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(java.sql.Date.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(LocalDate.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(LocalDateTime.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Time.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(LocalTime.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(long.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Long.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(int.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Integer.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(byte.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Byte.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(byte[].class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(short.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Short.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(double.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Double.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(float.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Float.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(char.class)));
        System.out.println(JacksonUtil.toJson(MySQLFieldType.getByClass(Character.class)));
    }

    public DataSource getLocalOracle19C() {
        String s = "jdbc:oracle:thin:@//localhost:1521/orcl.mshome.net";
        String driverClassNameByUrl = SqlType.getDriverClassNameByUrl(s);
        return new TempDataSource(driverClassNameByUrl, s, "demo", "123456");
    }

    @Test
    public void test2() throws SQLException {
        DataSource localOracle19C = getLocalOracle19C();
        try (Connection connection = localOracle19C.getConnection()) {
            IOpMeta select = OpDbMeta.select(connection);
            List<TableMetadata> allTableInfo = select.getAllTableInfo();
            System.out.println(JacksonUtil.toJson(allTableInfo));
        }
    }
}