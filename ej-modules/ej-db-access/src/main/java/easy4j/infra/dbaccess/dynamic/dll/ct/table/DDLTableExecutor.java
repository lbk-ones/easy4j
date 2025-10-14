package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

import java.util.List;

/**
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Deprecated
public class DDLTableExecutor {

    private final IDDLTableStrategy iddlTableCore;

    private final DDLTableInfo ddlTableInfo;

    public DDLTableExecutor(IDDLTableStrategy iddlTableCore, DDLTableInfo ddlTableInfo) {
        CheckUtils.notNull(iddlTableCore, "iddlTableCore");
        CheckUtils.notNull(ddlTableInfo, "ddlTableInfo");
        this.iddlTableCore = iddlTableCore;
        this.ddlTableInfo = ddlTableInfo;
    }

    public String getTableInfo() {
        return this.iddlTableCore.getTableTemplate(ddlTableInfo);
    }

    public List<String> getComments() {
        List<String> comments = this.iddlTableCore.getComments(ddlTableInfo);
        return comments == null ? ListTs.newArrayList() : comments;
    }

    public List<String> getIndexes() {
        List<String> indexes = this.iddlTableCore.getIndexes(ddlTableInfo);
        return indexes == null ? ListTs.newArrayList() : indexes;
    }
}
