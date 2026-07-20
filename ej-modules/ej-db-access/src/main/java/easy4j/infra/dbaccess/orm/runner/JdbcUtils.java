package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.dbaccess.dynamic.dll.op.meta.DatabaseColumnMetadata;
import easy4j.infra.dbaccess.orm.AccessException;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.OperateType;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@Slf4j
@Getter
public class JdbcUtils {

    private final Connection connection;

    public JdbcUtils(Connection connection) {
        this.connection = connection;
    }

    /**
     * 增删改
     *
     * @return 影响行数
     */
    public int update(RuntimeContext<?> runtimeContext) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        Connection connection1 = getConnection();
        OperateType operateType = runtimeContext.getOperateType();
        List<?> params = runtimeContext.getParams();
        boolean isInsert = operateType == OperateType.INSERT;
        AccessUtils accessUtils = runtimeContext.getAccessUtils();
        ResultSet generatedKeys = null;
        if (isInsert) {
            try (
                    PreparedStatement ps = connection1.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
            ) {
                StatementUtils.fillParams(runtimeContext, ps, args.toArray(new Object[]{}));
                int i = ps.executeUpdate();
                // 回写
                generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys == null) return i;
                MapListHandler mapListHandler = new MapListHandler();
                List<Map<String, Object>> handle = mapListHandler.handle(generatedKeys);
                for (int i1 = 0; i1 < params.size(); i1++) {
                    Object param = params.get(i1);
                    Class<?> aClass = param.getClass();
                    Map<String, Object> stringObjectMap = handle.get(i1);
                    if (stringObjectMap == null) continue;
                    for (Map.Entry<String, Object> stringObjectEntry : stringObjectMap.entrySet()) {
                        String fieldName = stringObjectEntry.getKey();
                        Object value = stringObjectEntry.getValue();
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
                return i;
            } catch (SQLException e) {
                throw new AccessException(e);
            } finally {
                accessUtils.close(generatedKeys);
            }
        } else {
            try (
                    PreparedStatement ps = connection1.prepareStatement(sql)
            ) {
                StatementUtils.fillParams(runtimeContext, ps, args.toArray(new Object[]{}));

                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new AccessException(e);
            }
        }

    }


    public ResultSet query(
            RuntimeContext<?> runtimeContext
    ) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        Connection conn = getConnection();
        try (
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            Integer fetchSize = runtimeContext.getConfig().getFetchSize();
            if (fetchSize != null) {
                ps.setFetchSize(fetchSize);
            }
            StatementUtils.fillParams(runtimeContext, ps, args.toArray(new Object[]{}));
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new AccessException(e);
        }
    }


}