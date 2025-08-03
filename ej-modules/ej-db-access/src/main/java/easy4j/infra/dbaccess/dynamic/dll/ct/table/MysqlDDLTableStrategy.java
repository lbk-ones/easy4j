package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.idx.IdxHandler;

import java.util.*;
import java.util.stream.Collectors;

public class MysqlDDLTableStrategy extends AbstractIDDLTableStrategy {

    public static final String DEFAULT_ENGINE = "InnoDB";
    public static final String DEFAULT_CHARSET = "utf8mb4";
    public static final String DEFAULT_CHARSET_COLLATE = "utf8mb4_general_ci";


    @Override
    public boolean match(DDLTableInfo ddlTableInfo) {
        String dbType = ddlTableInfo.getDbType();
        CheckUtils.notNull(dbType, "dbType");
        return StrUtil.equals("mysql", dbType);
    }

    @Override
    public String getTableTemplate(DDLTableInfo ddlTableInfo) {
        DDLConfig dllConfig = ddlTableInfo.getDllConfig();
        CheckUtils.checkByLambda(ddlTableInfo, DDLTableInfo::getTableName);
        String tableName = ddlTableInfo.getTableName();
        String tableInfoSchema = ddlTableInfo.getSchema();
        String fTableName = StrUtil.isNotBlank(tableInfoSchema) ? tableInfoSchema + SP.DOT + tableName : tableName;
        String comment = ddlTableInfo.getComment();
        List<String> objects = ListTs.newArrayList();
        objects.add("create");
        if (ddlTableInfo.isTemporary()) {
            objects.add(" temporary");
        }
        objects.add(" table");
        if (ddlTableInfo.isIfNotExists()) {
            objects.add(" if not exists");
        }
        objects.add(" " + fTableName);
        objects.add("(\n{0},\n");
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            List<String> primaryKey = ListTs.newArrayList();
            List<String> uniqueKey = ListTs.newArrayList();
            List<String> indexKey = ListTs.newArrayList();
            for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
                String name = ddlFieldInfo.getName();
                String columnName = dllConfig.getColumnName(name);
                boolean unique = ddlFieldInfo.isUnique();
                boolean isPrimary = ddlFieldInfo.isPrimary();
                boolean isIndex = ddlFieldInfo.isIndex();
                if (isPrimary) {
                    String wt = "primary key (" + columnName + ")";
                    primaryKey.add(wt);
                } else if (unique) {
                    uniqueKey.add("unique key `uk_" + fTableName.replace(".", "_") + "_" + columnName + "` (" + columnName + ")");
                } else if (isIndex) {
                    indexKey.add("index `idx_" + fTableName.replace(".", "_") + "_" + columnName + "` (" + columnName + ")");
                }
            }
            primaryKey.addAll(uniqueKey);
            primaryKey.addAll(indexKey);
            String join = String.join(",\n", primaryKey);
            if (StrUtil.isNotBlank(join)) {
                objects.add(join + "\n");
            }
        }
        objects.add(")\nengine = " + StrUtil.blankToDefault(ddlTableInfo.getEngine(), DEFAULT_ENGINE) + "\n");
        objects.add("default charset = " + StrUtil.blankToDefault(ddlTableInfo.getCharset(), DEFAULT_CHARSET) + "\n");
        objects.add("collate = " + StrUtil.blankToDefault(ddlTableInfo.getCollate(), DEFAULT_CHARSET_COLLATE) + "\n");
        if (StrUtil.isNotBlank(comment)) {
            objects.add("comment = " + dllConfig.wrapQuote(comment));
        }
        return String.join("", objects);
    }

    @Override
    public List<String> getComments(DDLTableInfo ddlFieldInfo) {
        return null;
    }

    @Override
    public List<String> getIndexes(DDLTableInfo ddlFieldInfo) {
        DDLConfig dllConfig = ddlFieldInfo.getDllConfig();
        List<DDLIndexInfo> ddlIndexInfoList = ddlFieldInfo.getDdlIndexInfoList();
        if (CollUtil.isEmpty(ddlIndexInfoList)) return null;
        List<DDLFieldInfo> fieldInfoList = ddlFieldInfo.getFieldInfoList();
        Map<String, DDLFieldInfo> fieldMaps = ListTs.mapOne(fieldInfoList, e -> dllConfig.getColumnName(e.getName()));
        Set<String> hasIndex = fieldInfoList.stream().filter(e -> {
            boolean isPrimary = e.isPrimary();
            boolean unique = e.isUnique();
            boolean index = e.isIndex();
            return isPrimary || unique || index;
        }).map(e -> dllConfig.getColumnName(e.getName())).collect(Collectors.toSet());
        List<String> re = ListTs.newArrayList();

        for (DDLIndexInfo indexInfo : ddlIndexInfoList) {
            String[] keys = indexInfo.getKeys();
            boolean skip = false;
            if (null != keys) {
                for (String key : keys) {
                    String columnName = dllConfig.getColumnName(key);
                    if (
                            hasIndex.contains(columnName) ||
                                    Objects.isNull(fieldMaps.get(columnName))
                    ) {
                        skip = true;
                        break;
                    }
                }
            }
            if (skip) continue;
            indexInfo.setSchema(ddlFieldInfo.getSchema());
            indexInfo.setTableName(ddlFieldInfo.getTableName());
            String s = IdxHandler.handlerIdx(indexInfo);
            re.add(s);
        }
        return re;
    }
}
