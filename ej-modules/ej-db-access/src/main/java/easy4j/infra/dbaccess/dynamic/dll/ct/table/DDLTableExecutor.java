package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;

import java.util.List;

public class DDLTableExecutor {

    private final IDDLTableStrategy iddlTableCore;

    private final DDLTableInfo ddlFieldInfo;

    public DDLTableExecutor(IDDLTableStrategy iddlTableCore, DDLTableInfo ddlFieldInfo) {
        CheckUtils.notNull(iddlTableCore, "iddlTableCore");
        CheckUtils.notNull(ddlFieldInfo, "ddlFieldInfo");
        this.iddlTableCore = iddlTableCore;
        this.ddlFieldInfo = ddlFieldInfo;
    }

    public String getTableInfo() {
        return this.iddlTableCore.getTableTemplate(ddlFieldInfo);
    }

    public List<String> getComments() {
        List<String> comments = this.iddlTableCore.getComments(ddlFieldInfo);
        return comments == null ? ListTs.newArrayList() : comments;
    }

    public List<String> getIndexes() {
        List<String> indexes = this.iddlTableCore.getIndexes(ddlFieldInfo);
        return indexes == null ? ListTs.newArrayList() : indexes;
    }
}
