package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

import java.util.List;

/**
 * OracleDDLTableStrategy
 *
 * @author bokun.li
 * @date 2025/8/19
 */
public class OracleDDLTableStrategy extends PgDDLTableStrategy {

    @Override
    public boolean match(DDLTableInfo ddlTableInfo) {
        String dbType = ddlTableInfo.getDbType();
        CheckUtils.notNull(dbType, "dbType");
        return StrUtil.equals("oracle", dbType);
    }


    @Override
    protected void handlerPgAndMysqlTitle(DDLTableInfo ddlTableInfo, String fTableName, List<String> segments) {
        List<String> tempList = ListTs.newArrayList();
        tempList.add("create");
        tempList.add("table");
        tempList.add(fTableName);
        segments.add(String.join(SP.SPACE, tempList));
        tempList.clear();
    }


}
