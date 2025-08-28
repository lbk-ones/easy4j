package easy4j.infra.dbaccess.dynamic.dll.op.impl.al;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.OpSelector;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpDdlAlter;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Map;

/**
 * AbstractOpDdlAlter
 * alter第一层抽象实现
 *
 * @author bokun.li
 * @date 2025/8/28
 */
@Getter
public abstract class AbstractOpDdlAlter implements OpDdlAlter {
    public static final String TABLE_NAME = "table_name";
    public static final String COLUMN_CONSTRAINT = "column_constraint";
    public static final String COLUMN_NAME = "column_name";
    public static final String NEW_COLUMN_NAME = "new_column_name";

    OpContext opContext;

    public final String addColumnTemplate = "alter table [" + TABLE_NAME + "] add column [" + COLUMN_CONSTRAINT + "]";
    public final String dropColumnTemplate = "alter table [" + TABLE_NAME + "] drop column [" + COLUMN_CONSTRAINT + "]";
    public final String renameColumnTemplate = "alter table [" + TABLE_NAME + "] rename column ["+COLUMN_NAME+"] TO ["+NEW_COLUMN_NAME+"]";

    public static final Map<String, String> COLUMN_MAP = Maps.newHashMap();
    public final Map<String, String> EXT_MAP = Maps.newHashMap();

    static {
        COLUMN_MAP.put(TABLE_NAME, TABLE_NAME);
        COLUMN_MAP.put(COLUMN_CONSTRAINT, COLUMN_CONSTRAINT);
        COLUMN_MAP.put(COLUMN_NAME, COLUMN_NAME);
        COLUMN_MAP.put(NEW_COLUMN_NAME, NEW_COLUMN_NAME);
    }

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    public Map<String, String> getAddColumnSegmentMap(DDLFieldInfo fieldInfo) {
        OpContext opContext1 = getOpContext();
        Map<@Nullable String, @Nullable String> resMap = Maps.newHashMap();
        resMap.put(TABLE_NAME, obtainTableName());
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext1);
        String columnConstraints = opColumnConstraints.getColumnConstraints(fieldInfo);
        resMap.put(COLUMN_CONSTRAINT, columnConstraints);
        return resMap;
    }

    @Override
    public String getAddColumnSegment(DDLFieldInfo fieldInfo) {
        String addColumnTemplate1 = getAddColumnTemplate();
        return this.getOpContext().getOpConfig().patchStrWithTemplate(fieldInfo, addColumnTemplate1, COLUMN_MAP, EXT_MAP, this::getAddColumnSegmentMap);
    }

    public Map<String, String> getRemoveColumnSegmentMap(DDLFieldInfo fieldInfo) {
        Map<@Nullable String, @Nullable String> resMap = Maps.newHashMap();
        resMap.put(TABLE_NAME, obtainTableName());
        resMap.put(COLUMN_CONSTRAINT, fieldInfo.getName());
        return resMap;
    }

    private String obtainTableName() {
        OpContext opContext1 = getOpContext();
        String tableName = opContext1.getTableName();
        String schema = opContext1.getSchema();
        OpConfig opConfig = opContext1.getOpConfig();
        DDLTableInfo ddlTableInfo = new DDLTableInfo();
        ddlTableInfo.setTableName(tableName);
        ddlTableInfo.setSchema(schema);
        return opConfig.getTableName(ddlTableInfo);
    }

    @Override
    public String getRemoveColumnSegment(DDLFieldInfo fieldInfo) {
        String dropColumnTemplate1 = getDropColumnTemplate();
        return this.getOpContext().getOpConfig().patchStrWithTemplate(fieldInfo, dropColumnTemplate1, COLUMN_MAP, EXT_MAP, this::getRemoveColumnSegmentMap);
    }

    /**
     * 供子类重载
     * @author bokun.li
     * @date 2025/8/28
     */
    public Map<String, String> getRenameColumnNameSegmentMap(Pair<String, String> newName) {
        String oldName = newName.getKey();
        String s = obtainTableName();
        Map<@Nullable String, @Nullable String> resMap = Maps.newHashMap();
        resMap.put(TABLE_NAME, s);
        resMap.put(COLUMN_NAME, oldName);
        resMap.put(NEW_COLUMN_NAME, newName.getValue());
        OpContext opContext1 = getOpContext();
        OpConfig opConfig = opContext1.getOpConfig();
        DDLTableInfo ddlTableInfo = opContext1.getDdlTableInfo();
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        CheckUtils.notNull(fieldInfoList,"the fieldInfoList is should not empty!");
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext1);
        String columnName = opConfig.getColumnName(oldName);
        String columnConstraints = "";
        boolean exist = false;
        for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
            String name = ddlFieldInfo.getName();
            if (StrUtil.equals(columnName, name)) {
                exist = true;
                columnConstraints = opColumnConstraints.getColumnConstraints(ddlFieldInfo);
                break;
            }
        }
        if (!exist) {
            throw new EasyException(String.format("the field %s is not exists in %s",oldName, s));
        }
        String trim = StrUtil.trim(columnConstraints.replaceFirst(oldName, ""));
        resMap.put(COLUMN_CONSTRAINT, trim);
        return resMap;
    }

    @Override
    public String getRenameColumnNameSegment(String oldName, String newColumnName) {
        return this.getOpContext()
                .getOpConfig()
                .patchStrWithTemplate(new Pair<>(oldName, newColumnName), getRenameColumnTemplate(), COLUMN_MAP, EXT_MAP, this::getRenameColumnNameSegmentMap);
    }

    @Override
    public String getRenameConstraintNameSegment(String newConstraintName) {
        return null;
    }

    @Override
    public String getRenameTableNameSegment(String newTableName) {
        return null;
    }

    @Override
    public String getSetSchemaNewNameSegment(String schemaNewName) {
        return null;
    }

    @Override
    public String getSetNewTableSpaceSegment(String newTableSpaceName) {
        return null;
    }
}
