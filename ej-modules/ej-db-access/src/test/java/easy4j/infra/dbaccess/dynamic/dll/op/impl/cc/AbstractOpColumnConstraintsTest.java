package easy4j.infra.dbaccess.dynamic.dll.op.impl.cc;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.OpSelector;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpColumnConstraints;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

class AbstractOpColumnConstraintsTest {

    @Test
    void getColumnConstraintsTestOracle() {
        OpContext opContext = new OpContext();
        opContext.setDbType("oracle");
        opContext.setOpConfig(new OpConfig());
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext);

        DDLFieldInfo ddlFieldInfo = new DDLFieldInfo()
                .setName("testId")
                .setFieldClass(int.class)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("23")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo));

        // date
        DDLFieldInfo ddlFieldInfo2 = new DDLFieldInfo();
        ddlFieldInfo2.setName("testDate");
        ddlFieldInfo2.setFieldClass(Date.class);
        ddlFieldInfo2.setAutoIncrement(true);
        ddlFieldInfo2.setStartWith(1000);
        ddlFieldInfo2.setIncrement(0);
        ddlFieldInfo2.setDef("23");
        ddlFieldInfo2.setDefTime(true);
        ddlFieldInfo2.setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo2));

        // localdateTime
        DDLFieldInfo ddlFieldInfo21 = new DDLFieldInfo()
                .setName("testDate2")
                .setFieldClass(LocalDateTime.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo21));

        // varchar2
        DDLFieldInfo ddlFieldInfo3 = new DDLFieldInfo()
                .setName("testVarchar2")
                .setFieldClass(String.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo3));


        // long
        DDLFieldInfo ddlFieldInfo4 = new DDLFieldInfo()
                .setName("testLong1")
                .setFieldClass(long.class)
                .setDataLength(30)
//                .setAutoIncrement(true)
//                .setStartWith(1000)
//                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo4));

    }


    @Test
    void getColumnConstraintsTestMysql() {
        OpContext opContext = new OpContext();
        opContext.setDbType(DbType.MYSQL.getDb());
        opContext.setOpConfig(new OpConfig());
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext);

        DDLFieldInfo ddlFieldInfo = new DDLFieldInfo()
                .setName("testId")
                .setComment("测试ID")
                .setFieldClass(int.class)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("23")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo));

        // date
        DDLFieldInfo ddlFieldInfo2 = new DDLFieldInfo();
        ddlFieldInfo2.setName("testDate");
        ddlFieldInfo2.setComment("测试时间");
        ddlFieldInfo2.setFieldClass(Date.class);
        ddlFieldInfo2.setAutoIncrement(true);
        ddlFieldInfo2.setStartWith(1000);
        ddlFieldInfo2.setIncrement(0);
        ddlFieldInfo2.setDef("23");
        ddlFieldInfo2.setDefTime(true);
        ddlFieldInfo2.setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo2));

        // localdateTime
        DDLFieldInfo ddlFieldInfo21 = new DDLFieldInfo()
                .setName("testDate2")
                .setFieldClass(LocalDateTime.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo21));

        // varchar2
        DDLFieldInfo ddlFieldInfo3 = new DDLFieldInfo()
                .setName("testVarchar2")
                .setComment("测试varchar2")
                .setFieldClass(String.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo3));


        // long
        DDLFieldInfo ddlFieldInfo4 = new DDLFieldInfo()
                .setName("testLong1")
                .setFieldClass(long.class)
                .setDataLength(30)
//                .setAutoIncrement(true)
//                .setStartWith(1000)
//                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo4));

        // lob
        DDLFieldInfo ddlFieldInfo5 = new DDLFieldInfo()
                .setName("testLob1")
                .setFieldClass(String.class)
                .setLob(true)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo5));


        // json
        DDLFieldInfo ddlFieldInfo6 = new DDLFieldInfo()
                .setName("testLob1")
                .setFieldClass(String.class)
                .setJson(true)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("{\"na\":\"1\"}")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo6));

    }


    @Test
    void getColumnConstraintsTestPg() {
        OpContext opContext = new OpContext();
        opContext.setDbType(DbType.POSTGRE_SQL.getDb());
        opContext.setOpConfig(new OpConfig());
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext);

        DDLFieldInfo ddlFieldInfo = new DDLFieldInfo()
                .setName("testId")
                .setComment("测试ID")
                .setFieldClass(int.class)
                .setAutoIncrement(true)
                .setStartWith(1000)
//                .setIncrement(0)
                .setDef("23")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo));

        // date
        DDLFieldInfo ddlFieldInfo2 = new DDLFieldInfo();
        ddlFieldInfo2.setName("testDate");
        ddlFieldInfo2.setComment("测试时间");
        ddlFieldInfo2.setFieldClass(Date.class);
        ddlFieldInfo2.setAutoIncrement(true);
        ddlFieldInfo2.setStartWith(1000);
        ddlFieldInfo2.setIncrement(0);
        ddlFieldInfo2.setDef("23");
        ddlFieldInfo2.setDefTime(true);
        ddlFieldInfo2.setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo2));

        // localdateTime
        DDLFieldInfo ddlFieldInfo21 = new DDLFieldInfo()
                .setName("testDate2")
                .setFieldClass(LocalDateTime.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo21));

        // varchar2
        DDLFieldInfo ddlFieldInfo3 = new DDLFieldInfo()
                .setName("testVarchar2")
                .setComment("测试varchar2")
                .setFieldClass(String.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo3));


        // long
        DDLFieldInfo ddlFieldInfo4 = new DDLFieldInfo()
                .setName("testLong1")
                .setFieldClass(long.class)
                .setDataLength(30)
//                .setAutoIncrement(true)
//                .setStartWith(1000)
//                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo4));

        // lob
        DDLFieldInfo ddlFieldInfo5 = new DDLFieldInfo()
                .setName("testLob1")
                .setFieldClass(String.class)
                .setLob(true)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("test-varchar2")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo5));


        // json
        DDLFieldInfo ddlFieldInfo6 = new DDLFieldInfo()
                .setName("testLob1")
                .setFieldClass(String.class)
                .setJson(true)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setDef("{\"na\":\"1\"}")
                .setNotNull(true);
        System.out.println(opColumnConstraints.getCreateColumnSql(ddlFieldInfo6));

    }
}