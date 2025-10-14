package easy4j.infra.dbaccess.dynamic.dll.ct.table;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.enums.DbType;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dynamic.dll.DDLConfig;
import easy4j.infra.dbaccess.dynamic.dll.DDLFieldInfo;
import easy4j.infra.dbaccess.dynamic.dll.DDLTableInfo;
import easy4j.infra.dbaccess.dynamic.dll.idx.DDLIndexInfo;
import easy4j.infra.dbaccess.dynamic.dll.idx.IdxHandler;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PgDDLTableStrategy
 *
 * @author bokun.li
 * @date 2025/8/19
 * @see easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL
 */
@Deprecated
public class PgDDLTableStrategy extends AbstractIDDLTableStrategy {

    @Override
    public boolean match(DDLTableInfo ddlTableInfo) {
        String dbType = ddlTableInfo.getDbType();
        CheckUtils.notNull(dbType, "dbType");
        return StrUtil.equals(DbType.POSTGRE_SQL.getDb(), dbType);
    }

    @Override
    public String getTableTemplate(DDLTableInfo ddlTableInfo) {
        CheckUtils.checkByLambda(ddlTableInfo, DDLTableInfo::getDllConfig);
        DDLConfig dllConfig = ddlTableInfo.getDllConfig();
        String fTableName = getTableName(ddlTableInfo);
        List<String> segments = ListTs.newArrayList();
        handlerPgAndMysqlTitle(ddlTableInfo, fTableName, segments);
        segments.add(SP.LEFT_BRACKET);
        segments.add(SP.FORMAT_ZWF_0 + SP.COMMA);
        boolean hasExtraLine = handlerConstraint(ddlTableInfo, dllConfig, fTableName, segments);
        // remove last comma
        if (!hasExtraLine && CollUtil.isNotEmpty(segments)) {
            String remove = segments.remove(segments.size() - 1);
            segments.add(StrUtil.replaceLast(remove, SP.COMMA, ""));
        }
        String pgInherits = ddlTableInfo.getPgInherits();
        String inheritsTable = StrUtil.isNotBlank(pgInherits) ? (" inherits ( " + pgInherits + " )") : "";
        segments.add(SP.RIGHT_BRACKET + inheritsTable);
        return String.join(SP.NEWLINE, segments);
    }

    /**
     * 处理约束，不处理外键约束
     *
     * @param ddlTableInfo 表格信息
     * @param dllConfig    配置信息
     * @param fTableName   表格名称
     * @param segments     需要合并的片段集合
     * @return
     */
    private static boolean handlerConstraint(DDLTableInfo ddlTableInfo, DDLConfig dllConfig, String fTableName, List<String> segments) {
        boolean hasExtraLine = false;
        List<DDLFieldInfo> fieldInfoList = ddlTableInfo.getFieldInfoList();
        String tableName = ddlTableInfo.getTableName();
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            List<DDLFieldInfo> primaryKey = ListTs.newList();
            List<DDLFieldInfo> uniqueKey = ListTs.newList();
            List<DDLFieldInfo> checkKey = ListTs.newList();
            List<DDLFieldInfo> constraintKey = ListTs.newList();
            for (DDLFieldInfo ddlFieldInfo : fieldInfoList) {
                String[] constraint = ddlFieldInfo.getConstraint();
                // primary key
                if (ddlFieldInfo.isPrimary()) {
                    primaryKey.add(ddlFieldInfo);
                }
                // unique
                if (ddlFieldInfo.isUnique()) {
                    uniqueKey.add(ddlFieldInfo);
                }
                // check
                if (StrUtil.isNotBlank(ddlFieldInfo.getCheck())) {
                    checkKey.add(ddlFieldInfo);
                }
                // custom constraint
                if (constraint != null && constraint.length > 0) {
                    constraintKey.add(ddlFieldInfo);
                }
            }
            int idx = 0;
            for (DDLFieldInfo ddlFieldInfo : primaryKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = dllConfig.getColumnName(name);
                String tem = "CONSTRAINT pk_" + tableName + "_" + columnName + "_" + idx + " PRIMARY KEY (" + columnName + ")" + SP.COMMA;
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : uniqueKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = dllConfig.getColumnName(name);
                String tem = "CONSTRAINT uk_" + tableName + "_" + columnName + "_" + idx + " UNIQUE (" + columnName + ")" + SP.COMMA;
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : checkKey) {
                hasExtraLine = true;
                String check = ddlFieldInfo.getCheck();
                String name = ddlFieldInfo.getName();
                String columnName = dllConfig.getColumnName(name);
                String tem = "CONSTRAINT check_" + tableName + "_" + columnName + "_" + idx + " CHECK (" + check + ")" + SP.COMMA;
                segments.add(tem);
                idx++;
            }
            for (DDLFieldInfo ddlFieldInfo : constraintKey) {
                hasExtraLine = true;
                String name = ddlFieldInfo.getName();
                String columnName = dllConfig.getColumnName(name);
                String[] constraint = ddlFieldInfo.getConstraint();
                for (String s : constraint) {
                    String tem = "CONSTRAINT ctk_" + tableName + "_" + columnName + "_" + idx + SP.SPACE + s + SP.COMMA;
                    segments.add(tem);
                    idx++;
                }
            }
        }
        if (hasExtraLine) {
            String remove = segments.remove(segments.size() - 1);
            segments.add(StrUtil.replaceLast(remove, SP.COMMA, ""));
        }
        return hasExtraLine;
    }


    @Override
    public List<String> getComments(DDLTableInfo ddlFieldInfo) {
        DDLConfig dllConfig = ddlFieldInfo.getDllConfig();
        List<String> comments = ListTs.newList();
        List<DDLFieldInfo> fieldInfoList = ddlFieldInfo.getFieldInfoList();
        String tableComments = ddlFieldInfo.getComment();
        Class<?> domainClass = ddlFieldInfo.getDomainClass();
        if (null != domainClass && StrUtil.isBlank(tableComments)) {
            if (StrUtil.isBlank(tableComments) && domainClass.isAnnotationPresent(Schema.class)) {
                tableComments = domainClass.getAnnotation(Schema.class).description();
            }
            if (StrUtil.isBlank(tableComments) && domainClass.isAnnotationPresent(Desc.class)) {
                tableComments = domainClass.getAnnotation(Desc.class).value();

            }
        }
        if (StrUtil.isNotBlank(tableComments)) {
            comments.add(String.format("COMMENT ON TABLE %s IS '%s'", getTableName(ddlFieldInfo), tableComments));
        }
        if (CollUtil.isNotEmpty(fieldInfoList)) {
            for (DDLFieldInfo fieldInfo : fieldInfoList) {
                Class<?> fieldClass = fieldInfo.getFieldClass();
                String comment = fieldInfo.getComment();
                if (null != fieldClass) {
                    if (StrUtil.isBlank(comment) && fieldClass.isAnnotationPresent(Desc.class)) {
                        comment = fieldClass.getAnnotation(Desc.class).value();
                    }
                    if (StrUtil.isBlank(comment) && fieldClass.isAnnotationPresent(Schema.class)) {
                        comment = fieldClass.getAnnotation(Schema.class).description();
                    }
                }
                if (StrUtil.isNotBlank(comment)) {
                    comments.add(String.format("COMMENT ON COLUMN %s IS '%s'", getTableName(ddlFieldInfo) + SP.DOT + dllConfig.getColumnName(fieldInfo.getName()), comment));
                }
            }
        }
        return comments;
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
            return isPrimary || unique;
        }).map(e -> dllConfig.getColumnName(e.getName())).collect(Collectors.toSet());
        List<String> re = ListTs.newArrayList();
        for (DDLIndexInfo indexInfo : ddlIndexInfoList) {
            String[] keys = indexInfo.getKeys();
            boolean skip = false;
            if (null != keys) {
                for (String key : keys) {
                    String columnName = dllConfig.getColumnName(key);
                    if (hasIndex.contains(columnName)) {
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
