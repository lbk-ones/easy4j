package easy4j.infra.dbaccess.dynamic.dll.op.impl.ct;

import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.condition.Where;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.OpSelector;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpDdlCreateTable;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 建表语句测试
 */
class OpDdlCreateTableTest {

    public List<DDLFieldInfo> getDdlFieldInfoList(){
        List<DDLFieldInfo> objects = ListTs.newList();
        objects.add(new DDLFieldInfo()
                .setName("testId")
                .setFieldClass(int.class)
                .setPrimary(true)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试主键")
                .setDef("23")
                .setNotNull(true)
        );
        objects.add(new DDLFieldInfo()
                .setName("testDate2")
                .setFieldClass(Date.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试Date")
                .setDef("test-varchar2")
                .setNotNull(true));
        objects.add(new DDLFieldInfo()
                .setName("testDate3")
                .setFieldClass(LocalDateTime.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试LocalDateTime")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField01")
                .setFieldClass(byte.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试byte")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField02")
                .setFieldClass(short.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试short")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField03")
                .setFieldClass(int.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试int")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField04")
                .setFieldClass(long.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试long")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField05")
                .setFieldClass(float.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试float")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField06")
                .setFieldClass(double.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试double")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField07")
                .setFieldClass(String.class)
                .setDataLength(30)
                .setJson(true)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试json")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField08")
                .setFieldClass(String.class)
                .setDataLength(30)
                .setLob(true)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试lob")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField09")
                .setFieldClass(String.class)
                .setUnique(true)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试unique")
                .setDef("test-varchar2")
                .setNotNull(false));

        objects.add(new DDLFieldInfo()
                .setName("backField10")
                .setFieldClass(String.class)
                .setCheck(Where.get("backField10", "in" ,"('0','1')"))
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试check")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField11")
                .setFieldClass(int.class)
                .setDataLength(30)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试generate always as1")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField12")
                .setFieldClass(int.class)
                .setDataLength(30)
                .setGeneratedAlwaysAs(Where.get("backField11", "*", "2"))
                .setGeneratedAlwaysAsNotNull(true)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试generate always as1")
                .setDef("test-varchar2")
                .setNotNull(false));
        objects.add(new DDLFieldInfo()
                .setName("backField13")
                .setFieldClass(int.class)
                .setDataLength(30)
                .setGeneratedAlwaysAs(Where.get("backField11", "*", "3"))
                .setGeneratedAlwaysAsNotNull(false)
                .setAutoIncrement(true)
                .setStartWith(1000)
                .setIncrement(0)
                .setComment("测试generate always as2")
                .setDef("test-varchar2")
                .setNotNull(false));
        return objects;
    }

    @Test
    void getCreateTableDDLMysql() {

        OpContext opContext = new OpContext();
        opContext.setDbType(DbType.MYSQL.getDb());
        opContext.setOpConfig(new OpConfig());
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName("test_table_name");
        ddlTableInfo.setComment("测试MYSQL的表");
        ddlTableInfo.setIfNotExists(true);
        ddlTableInfo.setSchema("");
        List<DDLFieldInfo> ddlFieldInfoList = getDdlFieldInfoList();
        ddlTableInfo.setFieldInfoList(ddlFieldInfoList);
        opContext.setDdlTableInfo(ddlTableInfo);
        OpDdlCreateTable opColumnConstraints = OpSelector.selectOpCreateTable(opContext);
        String createTableDDL = opColumnConstraints.getCreateTableDDL();
        System.out.println(createTableDDL);
        List<String> createTableComments = opColumnConstraints.getCreateTableComments();
        System.out.println(ListTs.join(";\n", createTableComments));
    }

    @Test
    void getCreateTableDDLOracle() {

        OpContext opContext = new OpContext();
        opContext.setDbType(DbType.ORACLE.getDb());
        opContext.setOpConfig(new OpConfig());
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName("test_table_name");
        ddlTableInfo.setComment("测试ORACLE的表");
        ddlTableInfo.setSchema("");
        List<DDLFieldInfo> ddlFieldInfoList = getDdlFieldInfoList();
        ddlTableInfo.setFieldInfoList(ddlFieldInfoList);
        opContext.setDdlTableInfo(ddlTableInfo);
        OpDdlCreateTable opColumnConstraints = OpSelector.selectOpCreateTable(opContext);
        String createTableDDL = opColumnConstraints.getCreateTableDDL();
        System.out.println(createTableDDL);
        List<String> createTableComments = opColumnConstraints.getCreateTableComments();
        System.out.println(ListTs.join(";\n", createTableComments));
    }
    @Test
    void getCreateTableDDLPG() {

        OpContext opContext = new OpContext();
        opContext.setDbType(DbType.POSTGRE_SQL.getDb());
        opContext.setOpConfig(new OpConfig());
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName("test_table_name");
        ddlTableInfo.setComment("测试PG的表");
        ddlTableInfo.setSchema("");
        List<DDLFieldInfo> ddlFieldInfoList = getDdlFieldInfoList();
        ddlTableInfo.setFieldInfoList(ddlFieldInfoList);
        opContext.setDdlTableInfo(ddlTableInfo);
        OpDdlCreateTable opColumnConstraints = OpSelector.selectOpCreateTable(opContext);
        String createTableDDL = opColumnConstraints.getCreateTableDDL();
        System.out.println(createTableDDL);
        List<String> createTableComments = opColumnConstraints.getCreateTableComments();
        System.out.println(ListTs.join(";\n", createTableComments));
    }

    @Test
    void test222(){
        String build = new WhereBuild().inArray("backField10","'0'","'1'").build(ListTs.newList());
        System.out.println(build);
    }
}