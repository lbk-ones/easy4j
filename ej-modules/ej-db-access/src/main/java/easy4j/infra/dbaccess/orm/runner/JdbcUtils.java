package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.orm.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@Slf4j
@Getter
public class JdbcUtils {

    public static final String STAND_GENERATED_KEY_NAME = "GENERATED_KEY";

    private final Connection connection;

    public JdbcUtils(Connection connection) {
        this.connection = connection;
    }

    /**
     * 增删改
     *
     * @return 影响行数
     */
    public PsRes update(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        Connection connection1 = getConnection();
        OperateType operateType = runtimeContext.getOperateType();
        List<?> params = runtimeContext.getParams();
        boolean isInsert = operateType == OperateType.INSERT;
        AccessUtils accessUtils = runtimeContext.getAccessUtils();
        ResultSet generatedKeys = null;
        PreparedStatement ps = null;
        int effectRows = 0;
        k:
        {
            if (isInsert) {
                try {
                    List<AccessField> autoIncrementColumns = runtimeContext
                            .getColumnInfoList()
                            .stream()
                            .filter(AccessField::isAutoIncrementIs)
                            .toList();
                    ps = connection1.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    StatementUtils.fillParams(runtimeContext, ps, args.toArray(new Object[]{}));
                    effectRows = ps.executeUpdate();
                    // 回写
                    generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys == null) break k;
                    MapListHandler mapListHandler = new MapListHandler();
                    List<Map<String, Object>> handle = mapListHandler.handle(generatedKeys);
                    for (int i1 = 0; i1 < params.size(); i1++) {
                        Object param = params.get(i1);
                        // 讲道理这里不会为null 但是严谨一点 还是判断一下
                        if (param == null) continue;
                        Class<?> aClass = param.getClass();
                        Map<String, Object> stringObjectMap = handle.get(i1);
                        if (stringObjectMap == null) continue;
                        int lastIndex = 0;
                        for (Map.Entry<String, Object> stringObjectEntry : stringObjectMap.entrySet()) {
                            String fieldName = stringObjectEntry.getKey();
                            Object value = stringObjectEntry.getValue();
                            // 标准名称
                            if (StrUtil.equals(STAND_GENERATED_KEY_NAME, fieldName)) {
                                if (autoIncrementColumns.size() == 1) {
                                    AccessField accessField = autoIncrementColumns.get(lastIndex);
                                    Field field = accessField.getField();
                                    Object convert = Convert.convert(field.getType(), value);
                                    ReflectUtil.setFieldValue(param, field, convert);
                                } else if (autoIncrementColumns.size() > 1) {
                                    AccessField accessField = ListTs.get(autoIncrementColumns, lastIndex);
                                    if (accessField != null) {
                                        Field field = accessField.getField();
                                        Object convert = Convert.convert(field.getType(), value);
                                        ReflectUtil.setFieldValue(param, field, convert);
                                    }
                                    if (lastIndex == autoIncrementColumns.size() - 1) {
                                        lastIndex = 0;
                                    } else {
                                        lastIndex++;
                                    }
                                }
                                continue;
                            }
                            // 非标准名称处理
                            String camelCase = StrUtil.toCamelCase(fieldName.toLowerCase());
                            Field field = ReflectUtil.getField(aClass, camelCase);
                            if (field != null) {
                                Object convert = Convert.convert(field.getType(), value);
                                ReflectUtil.setFieldValue(param, camelCase, convert);
                            } else {
                                field = ReflectUtil.getField(aClass, fieldName);
                                if (field != null) {
                                    Object convert = Convert.convert(field.getType(), value);
                                    ReflectUtil.setFieldValue(param, fieldName, convert);
                                } else {
                                    log.info("not found auto increment keys {}", fieldName);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    throw JdbcHelper.translateSqlException("update",sql,e,runtimeContext.getConfig().getDataSource());
                } finally {
                    accessUtils.close(generatedKeys);
                }
            } else {
                try {
                    ps = connection1.prepareStatement(sql);
                    StatementUtils.fillParams(runtimeContext, ps, args.toArray(new Object[]{}));

                    effectRows = ps.executeUpdate();
                } catch (SQLException e) {
                    throw JdbcHelper.translateSqlException("update",sql,e,runtimeContext.getConfig().getDataSource());
                }
            }
        }
        return new PsRes().setStatement(ps).setEffectRows(effectRows);


    }


    public PsRes query(
            RuntimeContext<?> runtimeContext
    ) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        Connection conn = getConnection();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = conn.prepareStatement(sql);
            Integer fetchSize = runtimeContext.getConfig().getFetchSize();
            if (fetchSize != null) {
                ps.setFetchSize(fetchSize);
            }
            StatementUtils.fillParams(runtimeContext, ps, args.toArray(new Object[]{}));
            resultSet = ps.executeQuery();
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("query",sql,e,runtimeContext.getConfig().getDataSource());

        }
        return new PsRes(resultSet, ps);
    }


}