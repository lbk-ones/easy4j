package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
//@SpringBootTest(classes = DDLParseJavaClassTestPG.class, properties = {
//        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
//        "spring.datasource.username=drhi_user",
//        "spring.datasource.password=drhi_password",
//        "spring.datasource.url=jdbc:postgresql://10.0.32.19:30163/ds",
//        "spring.datasource.driver-class-name=org.postgresql.Driver",
//        "spring.datasource.hikari.maximum-pool-size=50"
//})
@SpringBootTest(classes = DDLParseModelTestPg.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=root",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:postgresql://localhost:5432/postgres",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class DDLParseModelTestPg {


    @Autowired
    DataSource dataSource;

    @Test
    void execDDL() {

        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName("test_create_table");
        ddlTableInfo.setComment("这是测试");
        ddlTableInfo.setIfNotExists(true);
//        ddlTableInfo.setPgInherits("parent");
//        ddlTableInfo.setPgUnlogged(true);
        ArrayList<DDLFieldInfo> objects = ListTs.newArrayList();
        objects.add(new DDLFieldInfo().setName("order_no").setPrimary(true).setAutoIncrement(true).setFieldClass(Long.class));
        objects.add(new DDLFieldInfo().setName("ordTxt").setFieldClass(String.class));
        objects.add(new DDLFieldInfo().setName("ordTxt").setDataLength(23).setFieldClass(String.class));
        objects.add(new DDLFieldInfo().setName("ordClass").setIndex(true).setFieldClass(String.class));
        objects.add(new DDLFieldInfo().setName("create_date").setIndex(true).setFieldClass(Date.class));
        objects.add(new DDLFieldInfo().setName("backField1").setDataLength(33).setFieldClass(int.class));
        objects.add(new DDLFieldInfo().setName("backField2").setFieldClass(short.class));
        objects.add(new DDLFieldInfo().setName("backField3").setFieldClass(byte.class));
        objects.add(new DDLFieldInfo().setName("backField4").setFieldClass(double.class));
        objects.add(new DDLFieldInfo().setName("backField5").setDataLength(7).setDataDecimal(3).setFieldClass(BigDecimal.class));
        objects.add(new DDLFieldInfo().setName("backField6").setFieldClass(LocalDateTime.class));
        objects.add(new DDLFieldInfo().setName("backField7").setDataLength(1).setFieldClass(char.class));
        objects.add(new DDLFieldInfo().setName("backField8").setDataLength(30).setFieldClass(String.class).setNotNull(true).setDef("xxxvvzvz").setUnique(true));
        ddlTableInfo.setFieldInfoList(objects);
        List<DDLIndexInfo> list = ListTs.newArrayList();
        list.add(new DDLIndexInfo().setKeys(new String[]{"ordTxt"}));
        list.add(new DDLIndexInfo().setKeys(new String[]{"ord_class"}));
        list.add(new DDLIndexInfo().setKeys(new String[]{"create_date"}));
        list.add(new DDLIndexInfo().setKeys(new String[]{"upper(ord_txt)"}));
        ddlTableInfo.setDdlIndexInfoList(list);
        DDLParse ddlParseModel = new DDLParseModel(ddlTableInfo, dataSource, null);
        String ddlFragment = ddlParseModel.getDDLFragment();
        System.out.println(ddlFragment);

    }
}