package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Getter
@Setter
@Deprecated
public abstract class AbstractIDDLTableStrategy implements IDDLTableStrategy {
    boolean toUnderLine;
    boolean toLowCase = true;
    boolean toUpperCase;

    /**
     * 兼容获取表名
     *
     * @author bokun.li
     * @date 2025/8/19
     */
    public String getTableName(DDLTableInfo ddlTableInfo) {
        String tableName = ddlTableInfo.getTableName();
        String tableInfoSchema = ddlTableInfo.getSchema();
        return StrUtil.isNotBlank(tableInfoSchema) ? tableInfoSchema + SP.DOT + tableName : tableName;


    }

    protected void handlerPgAndMysqlTitle(DDLTableInfo ddlTableInfo, String fTableName, List<String> segments) {
        List<String> tempList = ListTs.newArrayList();
        tempList.add("create");
        if (ddlTableInfo.isTemporary()) {
            tempList.add("temporary");
        }
        if (ddlTableInfo.isPgUnlogged()) {
            tempList.add("unlogged");
        }
        tempList.add("table");
        if (ddlTableInfo.isIfNotExists()) {
            tempList.add("if not exists");
        }
        tempList.add(fTableName);
        segments.add(String.join(SP.SPACE, tempList));
        tempList.clear();
    }
}
