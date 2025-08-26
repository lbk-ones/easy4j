package easy4j.infra.dbaccess.dynamic.dll.op.impl.idx;

import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonOpDdlIndexImplTest {

    @Test
    void getIndexes() {
        CommonOpDdlIndexImpl commonOpDdlIndex = new CommonOpDdlIndexImpl();
        OpContext opContext = new OpContext();
        opContext.setOpConfig(new OpConfig());
        commonOpDdlIndex.setOpContext(opContext);
        DDLIndexInfo ddlIndexInfo = new DDLIndexInfo();
        ddlIndexInfo.setIndexTypeName("unique");
//        ddlIndexInfo.setName("idx_xxxx");
        ddlIndexInfo.setKeys(new String[]{"key1","key2"});
        ddlIndexInfo.setTableName("test_table");
        String indexes = commonOpDdlIndex.getIndexes(ddlIndexInfo);
        System.out.println(indexes);

    }
}