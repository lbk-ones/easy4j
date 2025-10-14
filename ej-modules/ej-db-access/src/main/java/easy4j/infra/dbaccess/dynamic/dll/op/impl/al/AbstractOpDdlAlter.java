package easy4j.infra.dbaccess.dynamic.dll.op.impl.al;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dialect.v2.DialectFactory;
import easy4j.infra.dbaccess.dialect.v2.DialectV2;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.OpSelector;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpColumnConstraints;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpDdlAlter;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpSqlCommands;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.IOpMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.OpDbMeta;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.TableMetadata;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public final String renameColumnTemplate = "alter table [" + TABLE_NAME + "] rename column [" + COLUMN_NAME + "] TO [" + NEW_COLUMN_NAME + "]";
    public final String dropTableTemplate = "drop table if exists [" + TABLE_NAME + "]";

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
        OpConfig opConfig = opContext1.getOpConfig();
        Map<@Nullable String, @Nullable String> resMap = Maps.newHashMap();
        resMap.put(TABLE_NAME, opConfig.splitStrAndEscape(obtainTableName(), SP.DOT, opContext1.getConnection(), false));
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext1);
        String columnConstraints = opColumnConstraints.getCreateColumnSql(fieldInfo);
        resMap.put(COLUMN_CONSTRAINT, columnConstraints);
        return resMap;
    }

    @Override
    public String addColumn(DDLFieldInfo fieldInfo) {
        String addColumnTemplate1 = getAddColumnTemplate();
        return this.getOpContext().getOpConfig().patchStrWithTemplate(fieldInfo, addColumnTemplate1, COLUMN_MAP, EXT_MAP, this::getAddColumnSegmentMap);
    }

    public Map<String, String> getRemoveColumnSegmentMap(DDLFieldInfo fieldInfo) {
        OpConfig opConfig = this.getOpContext().getOpConfig();
        Connection connection = this.getOpContext().getConnection();
        Map<@Nullable String, @Nullable String> resMap = Maps.newHashMap();
        resMap.put(TABLE_NAME, opConfig.splitStrAndEscape(obtainTableName(), SP.DOT, connection, false));
        resMap.put(COLUMN_CONSTRAINT, opConfig.getColumnNameAndEscape(fieldInfo.getName(), connection, false));
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
    public String removeColumn(DDLFieldInfo fieldInfo) {
        String dropColumnTemplate1 = getDropColumnTemplate();
        return this.getOpContext().getOpConfig().patchStrWithTemplate(fieldInfo, dropColumnTemplate1, COLUMN_MAP, EXT_MAP, this::getRemoveColumnSegmentMap);
    }

    /**
     * 供子类重载
     * @author bokun.li
     * @date 2025/8/28
     */
    public Map<String, String> getRenameColumnNameSegmentMap(Pair<String, String> newName) {
        OpContext opContext1 = getOpContext();
        OpConfig opConfig = opContext1.getOpConfig();
        String oldName = newName.getKey();
        String s = obtainTableName();
        Map<@Nullable String, @Nullable String> resMap = Maps.newHashMap();
        Connection connection = opContext1.getConnection();
        resMap.put(TABLE_NAME, opConfig.splitStrAndEscape(s, SP.DOT, connection, false));
        resMap.put(COLUMN_NAME, opConfig.getColumnNameAndEscape(oldName, connection, false));
        resMap.put(NEW_COLUMN_NAME, opConfig.getColumnNameAndEscape(newName.getValue(), connection, false));
        DDLTableInfo ddlTableInfo = opContext1.getDdlTableInfo();
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        CheckUtils.notNull(fieldInfoList, "the fieldInfoList is should not empty!");
        OpColumnConstraints opColumnConstraints = OpSelector.selectOpCC(opContext1);
        String columnName = opConfig.getColumnNameAndEscape(oldName, connection, false);
        String columnConstraints = "";
        boolean exist = false;
        for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
            String name = ddlFieldInfo.getName();
            if (StrUtil.equals(columnName, name)) {
                exist = true;
                columnConstraints = opColumnConstraints.getCreateColumnSql(ddlFieldInfo);
                break;
            }
        }
        if (!exist) {
            throw new EasyException(String.format("the field %s is not exists in %s", oldName, s));
        }
        String trim = StrUtil.trim(columnConstraints.replaceFirst(oldName, ""));
        resMap.put(COLUMN_CONSTRAINT, trim);
        return resMap;
    }

    @Override
    public String renameColumnName(String oldName, String newColumnName) {
        return this.getOpContext()
                .getOpConfig()
                .patchStrWithTemplate(new Pair<>(oldName, newColumnName), getRenameColumnTemplate(), COLUMN_MAP, EXT_MAP, this::getRenameColumnNameSegmentMap);
    }

    @Override
    public String renameConstraintName(String newConstraintName) {
        return null;
    }

    @Override
    public String renameTableName(String newTableName) {
        return null;
    }

    @Override
    public String setSchemaNewName(String schemaNewName) {
        return null;
    }

    @Override
    public String setNewTableSpace(String newTableSpaceName) {
        return null;
    }

    public Map<String, String> getDropTableMap(String tableName) {
        Map<@Nullable String, @Nullable String> res = Maps.newHashMap();
        String schema = this.getOpContext().getSchema();
        String joinStr = ListTs.asList(schema, tableName).stream().filter(StrUtil::isNotBlank).collect(Collectors.joining(SP.DOT));
        res.put(TABLE_NAME, joinStr);
        return res;
    }

    @Override
    public String dropTableIfExists(String tableName, boolean isExe) {
        OpContext opContext1 = this.getOpContext();
        Connection connection = Optional.ofNullable(opContext1).map(OpContext::getConnection).orElseThrow(() -> new IllegalArgumentException("the connection is null"));
        DialectV2 select = DialectFactory.get(connection);
        List<TableMetadata> tableInfos = select.getAllTableInfoByTableType(tableName, new String[]{"TABLE"});
        String s = this.getOpContext().getOpConfig().patchStrWithTemplate(tableName, this.getDropTableTemplate(), COLUMN_MAP, EXT_MAP, this::getDropTableMap);
        if (ListTs.isEmpty(tableInfos)) {
            return s;
        }
        if (isExe) {
            OpSqlCommands opSqlCommands = OpSelector.selectOpSqlCommands(this.getOpContext());
            opSqlCommands.exeDDLStr(connection, s, false);
        }
        return s;
    }

    @Override
    public List<String> dropALlTableIfExists(boolean isExe) {
        OpContext opContext1 = this.getOpContext();
        Connection connection = Optional.ofNullable(opContext1).map(OpContext::getConnection).orElseThrow(() -> new IllegalArgumentException("the connection is null"));
        DialectV2 select = DialectFactory.get(connection);
        List<TableMetadata> allTableInfoByTableType = select.getAllTableInfoByTableType(null, new String[]{"TABLE"});
        List<String> res = ListTs.newList();
        if (ListTs.isNotEmpty(allTableInfoByTableType)) {
            for (TableMetadata tableMetadata : allTableInfoByTableType) {
                String s = this.getOpContext()
                        .getOpConfig()
                        .patchStrWithTemplate(tableMetadata.getTableName(), this.getDropTableTemplate(), COLUMN_MAP, EXT_MAP, this::getDropTableMap);
                res.add(s);
            }
            OpSqlCommands opSqlCommands = OpSelector.selectOpSqlCommands(this.getOpContext());
            if (isExe) {
                for (String re : res) {
                    if (StrUtil.isNotBlank(re)) {
                        opSqlCommands.exeDDLStr(connection, re, false);
                    }
                }
            }
        }
        return res;
    }
}
