package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.common.utils.json.JacksonUtil;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

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
}