package easy4j.infra.dbaccess.dynamic.dll.ad;

import cn.hutool.core.collection.CollUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategyExecutor;
import easy4j.infra.dbaccess.dynamic.dll.ct.field.DDLFieldStrategySelector;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableCoreSelector;
import easy4j.infra.dbaccess.dynamic.dll.ct.table.DDLTableExecutor;

import java.util.List;

/**
 * AdFieldStrategy
 * <p>
 * ALTER TABLE users ADD email VARCHAR(100) NOT NULL DEFAULT 'unknown';
 *
 * @author bokun.li
 * @date 2025/8/20
 */
public class AnsiAdFieldStrategy implements AdFieldStrategy {


    @Override
    public String getColumnSegment(DDLConfig ddlConfig) {
        List<DDLFieldInfo> adColumns = ddlConfig.getAdColumns();
        if (CollUtil.isNotEmpty(adColumns)) {
            List<String> newList = ListTs.newList();
            for (DDLFieldInfo adColumn : adColumns) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("ALTER TABLE ");
                String tableName = ddlConfig.getTableName();
                stringBuilder.append(tableName);
                stringBuilder.append(" ADD COLUMN ");
                adColumn.setGenConstraint(true);
                adColumn.setDllConfig(ddlConfig);
                DDLFieldStrategyExecutor ddlFieldStrategyExecutor = DDLFieldStrategySelector.selectExecutor(adColumn);
                String executor = ddlFieldStrategyExecutor.executor();
                stringBuilder.append(executor);
                newList.add(stringBuilder.toString());
            }
            return String.join(SP.SEMICOLON + SP.NEWLINE, newList);
        } else {
            return "";
        }
    }

    @Override
    public String getColumnComment(DDLConfig ddlConfig) {
        List<DDLFieldInfo> adColumns = ddlConfig.getAdColumns();
        if (CollUtil.isNotEmpty(adColumns)) {
            DDLTableInfo ddlTableInfo = new DDLTableInfo();
            ddlTableInfo.setDbVersion(ddlConfig.getDbVersion());
            ddlTableInfo.setSchema(ddlConfig.getSchema());
            ddlTableInfo.setDbType(ddlConfig.getDbType());
            ddlTableInfo.setTableName(ddlConfig.getTableName());
            ddlTableInfo.setDllConfig(ddlConfig);
            ddlTableInfo.setFieldInfoList(adColumns);
            DDLTableExecutor dDlTableExecutor = DDLTableCoreSelector.getDDlTableExecutor(ddlTableInfo);
            List<String> comments = dDlTableExecutor.getComments();
            if (CollUtil.isNotEmpty(comments)) {
                return String.join(SP.SEMICOLON + SP.NEWLINE, comments);
            }
        }
        return "";
    }
}
