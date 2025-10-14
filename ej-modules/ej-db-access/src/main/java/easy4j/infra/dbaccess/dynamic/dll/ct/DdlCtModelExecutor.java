package easy4j.infra.dbaccess.dynamic.dll.ct;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategyExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategySelector;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableCoreSelector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 根据模型来逆向
 *
 * @author bokun.li
 * @date 2025-08-03
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Deprecated
public class DdlCtModelExecutor extends AbstractDDLParseExecutor implements DDLParseExecutor {

    DDLConfig dllConfig;

    public DdlCtModelExecutor(DDLConfig dllConfig) {
        CheckUtils.notNull(dllConfig, "dllConfig");
        this.dllConfig = dllConfig;
        CheckUtils.checkByPath(dllConfig, "tableName");
        CheckUtils.checkByPath(dllConfig, "ddlTableInfo");
        CheckUtils.checkByPath(dllConfig, "ddlTableInfo.fieldInfoList.[].dbType");
        CheckUtils.checkByPath(dllConfig, "ddlTableInfo.fieldInfoList.[].fieldClass");
    }

    public String execute() {
        DDLTableInfo ddlTableInfo = this.dllConfig.getDdlTableInfo();
        ddlTableInfo.setDllConfig(this.dllConfig);
        DDLTableExecutor dDlTableCoreExecutor = DDLTableCoreSelector.getDDlTableExecutor(ddlTableInfo);
        List<DDLFieldInfo> ddlFieldInfos = ddlTableInfo.getFieldInfoList();
        List<String> objects = ListTs.newLinkedList();
        Set<String> distinctSet = new HashSet<>();
        for (DDLFieldInfo ddlFieldInfo : ddlFieldInfos) {
            String name = ddlFieldInfo.getName();
            String underlineCase = StrUtil.toUnderlineCase(name);
            if (distinctSet.contains(underlineCase)) {
                continue;
            } else {
                distinctSet.add(underlineCase);
            }
            ddlFieldInfo.setDllConfig(this.dllConfig);
            DDLFieldStrategyExecutor ddlFieldCoreContext = DDLFieldStrategySelector.selectExecutor(ddlFieldInfo);
            String fieldStrList = ddlFieldCoreContext.executor();
            objects.add(fieldStrList);
        }
        return getString(dDlTableCoreExecutor, objects);
    }


}
