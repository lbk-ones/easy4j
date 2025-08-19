package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

class PgDDLFieldStrategyTest {
    public static final DDLConfig ddlConfig = new DDLConfig();

    @Test
    void getResColumn() {


        PgDDLFieldStrategy pgDDLFieldStrategy = new PgDDLFieldStrategy();
        System.out.println(pgDDLFieldStrategy.getResColumn(new DDLFieldInfo().setDllConfig(ddlConfig).setFieldClass(String.class).setNotNull(true).setName("name").setDef("2").setDataLength(50)));
        System.out.println(pgDDLFieldStrategy.getResColumn(new DDLFieldInfo().setDllConfig(ddlConfig).setFieldClass(int.class).setNotNull(true).setName("num").setDefNum(2)));
        System.out.println(pgDDLFieldStrategy.getResColumn(new DDLFieldInfo().setDllConfig(ddlConfig).setFieldClass(Date.class).setNotNull(true).setName("create_date").setDefTime(true)));
        System.out.println(pgDDLFieldStrategy.getResColumn(new DDLFieldInfo().setDllConfig(ddlConfig).setFieldClass(LocalDate.class).setNotNull(true).setName("create_date2").setDefTime(true)));
        System.out.println(pgDDLFieldStrategy.getResColumn(new DDLFieldInfo().setDllConfig(ddlConfig).setFieldClass(LocalDateTime.class).setNotNull(true).setName("reqDateTime").setDefTime(true)));
        System.out.println(pgDDLFieldStrategy.getResColumn(new DDLFieldInfo().setDllConfig(ddlConfig).setFieldClass(java.sql.Date.class).setNotNull(true).setName("reqDateTime2").setDefTime(true)));

    }
}