package easy4j.infra.dbaccess.dynamic.dll.op.impl.sc;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.dynamic.dll.op.OpConfig;
import easy4j.infra.dbaccess.dynamic.dll.op.OpContext;
import easy4j.infra.dbaccess.dynamic.dll.op.api.OpSqlCommands;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.*;
import easy4j.infra.dbaccess.helper.DDlHelper;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Getter;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractOpSqlCommands implements OpSqlCommands {

    OpContext opContext;

    @Override
    public boolean match(OpContext opContext) {
        return true;
    }

    @Override
    public void setOpContext(OpContext opContext) {
        this.opContext = opContext;
    }

    @Override
    public void exeDDLStr(String segment) {
        String ddl = StrUtil.trim(segment);
        if (StrUtil.isBlank(segment)) return;
        if (!ddl.endsWith(SP.SEMICOLON)) {
            ddl = ddl + SP.SEMICOLON;
        }
        if (StrUtil.isNotBlank(ddl)) {
            try {
                DDlHelper.execDDL(getOpContext().getConnection(), ddl, null, true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Map<String,Object> dynamicSave(Map<String, Object> dict) {
        if (CollUtil.isEmpty(dict)) return dict;
        OpContext opContext1 = this.getOpContext();
        TableMetadata tableMetadata = opContext1.getTableMetadata();
        if (null == tableMetadata) {
            throw EasyException.wrap(BusCode.A00060, opContext1.getTableName());
        }
        String tableType = tableMetadata.getTableType();
        if (!StrUtil.equalsIgnoreCase(tableType, "TABLE")) {
            throw EasyException.wrap(BusCode.A00047, tableType);
        }
        OpConfig opConfig = opContext1.getOpConfig();
        CommonDBAccess commonDBAccess = opConfig.getCommonDBAccess();
        commonDBAccess.setPrintLog(true);
        commonDBAccess.setToUnderline(opConfig.isToUnderLine());
        List<DatabaseColumnMetadata> dbColumns = opContext1.getDbColumns();
        List<DatabaseColumnMetadata> noAuto = dbColumns.stream().filter(e -> !"YES".equals(e.getIsAutoincrement())).collect(Collectors.toList());
        List<DatabaseColumnMetadata> AutoKey = dbColumns.stream().filter(e -> "YES".equals(e.getIsAutoincrement())).collect(Collectors.toList());
        List<String> dbColumnNmes = ListTs.map(noAuto, DatabaseColumnMetadata::getColumnName);
        if (CollUtil.isEmpty(dbColumnNmes)) {
            throw new EasyException(BusCode.A00059);
        }
        // ignore case
        List<Object> objects = ListTs.newList();
        List<String> zwf = ListTs.newList();
        Set<String> keys = dict.keySet();
        List<String> fieldNames = ListTs.newLinkedList();
        for (String key : keys) {
            String key2 = StrUtil.toUnderlineCase(key);
            if (!dbColumnNmes.contains(key2)) {
                if (!dbColumnNmes.contains(key2.toUpperCase())) {
                    if (!dbColumnNmes.contains(key2.toLowerCase())) {
                        continue;
                    }
                }
            }
            Object o = dict.get(key2);
            if (o == null) {
                o = dict.get(key2.toUpperCase());
                if (o == null) {
                    o = dict.get(key2.toLowerCase());
                }
            }
            objects.add(o);
            fieldNames.add(key2);
            zwf.add(SP.QUESTION_MARK);
        }
        String tableName = opContext1.getTableName();
        CheckUtils.notNull(tableName, "tableName");
        String schema = opContext1.getSchema();
        String tableName2 = ListTs.asList(schema, tableName).stream().filter(Objects::nonNull).collect(Collectors.joining("."));
        String finalSql = commonDBAccess.DDlLine(
                CommonDBAccess.INSERT,
                tableName2,
                "values "+SP.LEFT_BRACKET + String.join(SP.COMMA, zwf) + SP.RIGHT_BRACKET,
                fieldNames.toArray(new String[]{}));
        Pair<String, Date> stringDatePair = null;
        int effectRows = 0;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = opContext1.getConnection();
            stringDatePair = commonDBAccess.recordSql(finalSql, connection, objects);
            preparedStatement = connection.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
            StatementUtil.fillParams(preparedStatement, objects);
            effectRows = preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            Map<String,Object> map = Maps.newHashMap();
            map.putAll(dict);
            while (generatedKeys.next()) {
                for (DatabaseColumnMetadata primaryKe : AutoKey) {
                    String columnName = primaryKe.getColumnName();
                    Long id = generatedKeys.getLong(columnName);
                    map.put(columnName,id);
                }
            }
            return map;
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("dynamicSave", finalSql, e);
        } finally {
            commonDBAccess.printSql(stringDatePair, effectRows);
            JdbcHelper.close(preparedStatement);
        }
    }
}
