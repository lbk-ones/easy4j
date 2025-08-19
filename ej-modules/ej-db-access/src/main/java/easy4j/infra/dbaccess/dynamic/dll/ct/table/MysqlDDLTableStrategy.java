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
        String fTableName = getTableName(ddlTableInfo);
        String comment = ddlTableInfo.getComment();
        List<String> segments = ListTs.newArrayList();
        handlerPgAndMysqlTitle(ddlTableInfo, fTableName, segments);
        segments.add(SP.LEFT_BRACKET);
        segments.add(SP.FORMAT_ZWF_0 + SP.COMMA);
        boolean hasExtraLine = handlerExtraLineAndReturnHasExtraLine(ddlTableInfo, dllConfig, fTableName, segments);
        // remove last comma
        if (!hasExtraLine && CollUtil.isNotEmpty(segments)) {
            String remove = segments.remove(segments.size() - 1);
            segments.add(StrUtil.replaceLast(remove, SP.COMMA, ""));
        }
        segments.add(SP.RIGHT_BRACKET);
        segments.add("engine = " + StrUtil.blankToDefault(ddlTableInfo.getEngine(), DEFAULT_ENGINE));
        segments.add("default charset = " + StrUtil.blankToDefault(ddlTableInfo.getCharset(), DEFAULT_CHARSET));
        segments.add("collate = " + StrUtil.blankToDefault(ddlTableInfo.getCollate(), DEFAULT_CHARSET_COLLATE));
        if (StrUtil.isNotBlank(comment)) {
            segments.add("comment = " + dllConfig.wrapQuote(comment));
        }
        return String.join(SP.NEWLINE, segments);
    }

    /**
     * 处理索引 和其他约束
     *
     * @param ddlTableInfo 表格信息
     * @param dllConfig    配置信息
     * @param fTableName   表格名称
     * @param segments     需要合并的片段集合
     * @return
     */
    private static boolean handlerExtraLineAndReturnHasExtraLine(DDLTableInfo ddlTableInfo, DDLConfig dllConfig, String fTableName, List<String> segments) {
        boolean hasExtraLine = false;
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            List<String> indexes = ListTs.newArrayList();
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
                    indexes.add(wt);
                } else if (unique) {
                    uniqueKey.add("unique key `uk_" + fTableName.replace(".", "_") + "_" + columnName + "` (" + columnName + ")");
                } else if (isIndex) {
                    indexKey.add("index `idx_" + fTableName.replace(".", "_") + "_" + columnName + "` (" + columnName + ")");
                }
            }
            indexes.addAll(uniqueKey);
            indexes.addAll(indexKey);
            if (CollUtil.isNotEmpty(indexes)) {
                String join = String.join(",\n", indexes);
                if (StrUtil.isNotBlank(join)) {
                    hasExtraLine = true;
                    segments.add(join);
                }
            }
        }
        return hasExtraLine;
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
                    if (hasIndex.contains(columnName) || Objects.isNull(fieldMaps.get(columnName))) {
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
