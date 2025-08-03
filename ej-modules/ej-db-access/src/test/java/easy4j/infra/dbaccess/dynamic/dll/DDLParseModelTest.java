package easy4j.infra.dbaccess.dynamic.dll;

import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.schema.InformationSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Easy4JStarter(
        serverName = "test-db-access",
        serverPort = 9090,
        enableH2 = true
)
@SpringBootTest(classes = DDLParseModelTest.class, properties = {
        "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
        "spring.datasource.username=root",
        "spring.datasource.password=123456",
        "spring.datasource.url=jdbc:mysql://localhost:3306/seata",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.hikari.maximum-pool-size=50"
})
class DDLParseModelTest {


    @Autowired
    DataSource dataSource;

    @Test
    void execDDL() {

        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName("test_create_table");
        ddlTableInfo.setComment("这是测试");
        ddlTableInfo.setIfNotExists(true);
        ArrayList<DDLFieldInfo> objects = ListTs.newArrayList();
        DDLFieldInfo ddlFieldInfo = new DDLFieldInfo();
        ddlFieldInfo.setName("ordTxt");
        ddlFieldInfo.setFieldClass(String.class);
        objects.add(ddlFieldInfo);


        objects.add(new DDLFieldInfo().setName("order_no").setPrimary(true).setFieldClass(Long.class));
        objects.add(new DDLFieldInfo().setName("ordTxt").setDataLength(23).setFieldClass(String.class));
        objects.add(new DDLFieldInfo().setName("ordClass").setIndex(true).setFieldClass(String.class));
        objects.add(new DDLFieldInfo().setName("create_date").setIndex(true).setFieldClass(Date.class));
        objects.add(new DDLFieldInfo().setName("backField1").setDataLength(33).setFieldClass(int.class));
        objects.add(new DDLFieldInfo().setName("backField2").setFieldClass(short.class));
        objects.add(new DDLFieldInfo().setName("backField3").setFieldClass(byte.class));
        objects.add(new DDLFieldInfo().setName("backField4").setFieldClass(double.class));
        objects.add(new DDLFieldInfo().setName("backField5").setDataLength(7).setDataDecimal(3).setFieldClass(BigDecimal.class));
        objects.add(new DDLFieldInfo().setName("backField6").setFieldClass(LocalDateTime.class));
        ddlTableInfo.setFieldInfoList(objects);
        List<DDLIndexInfo> list = ListTs.newArrayList();
        list.add(new DDLIndexInfo().setKeys(new String[]{"ordTxt"}));
        list.add(new DDLIndexInfo().setKeys(new String[]{"ord_class"}));
        list.add(new DDLIndexInfo().setKeys(new String[]{"create_date"}));
        ddlTableInfo.setDdlIndexInfoList(list);
        DDLParse ddlParseModel = new DDLParseModel(ddlTableInfo, dataSource, null);
        String ddlFragment = ddlParseModel.getDDLFragment();
        System.out.println(ddlFragment);

    }
}