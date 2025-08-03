package easy4j.infra.dbaccess.dynamic.dll.ct.field;

import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;

/**
 * DDLFieldStrategyExecutor
 * 执行器
 *
 * @author bokun.li
 * @date 2025-08-03
 */
public class DDLFieldStrategyExecutor {

    private final IDDLFieldStrategy iddlFieldCore;

    private final DDLFieldInfo ddlFieldInfo;

    public DDLFieldStrategyExecutor(IDDLFieldStrategy iddlFieldCore, DDLFieldInfo ddlFieldInfo) {
        CheckUtils.checkByPath(ddlFieldInfo, "fieldClass");
        this.iddlFieldCore = iddlFieldCore;
        this.ddlFieldInfo = ddlFieldInfo;
    }

    public String executor() {
        return this.iddlFieldCore.getResColumn(ddlFieldInfo);
    }
}
