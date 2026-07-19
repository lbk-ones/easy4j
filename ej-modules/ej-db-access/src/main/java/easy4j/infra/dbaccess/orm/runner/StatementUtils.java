package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.core.collection.ArrayIter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.db.sql.SqlUtil;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.Map;

public class StatementUtils {

    public static PreparedStatement fillParams(RuntimeContext<?> runtimeContext,PreparedStatement ps, Object... params) throws SQLException {
        if (ArrayUtil.isEmpty(params)) {
            return ps;
        }
        return fillParams(runtimeContext,ps, new ArrayIter<>(params));
    }
    public static PreparedStatement fillParams(RuntimeContext<?> runtimeContext,PreparedStatement ps, Iterable<?> params) throws SQLException {
        return fillParams(runtimeContext,ps, params, null);
    }
    public static PreparedStatement fillParams(RuntimeContext<?> runtimeContext,PreparedStatement ps, Iterable<?> params, Map<Integer, Integer> nullTypeCache) throws SQLException {
        if (null == params) {
            return ps;// 无参数
        }

        int paramIndex = 1;//第一个参数从1计数
        for (Object param : params) {
            setParam(ps, paramIndex++, param, nullTypeCache);
        }
        return ps;
    }
    private static void setParam(PreparedStatement ps, int paramIndex, Object param, Map<Integer, Integer> nullTypeCache) throws SQLException {
        if (null == param) {
            Integer type = (null == nullTypeCache) ? null : nullTypeCache.get(paramIndex);
            if (null == type) {
                type = getTypeOfNull(ps, paramIndex);
                if (null != nullTypeCache) {
                    nullTypeCache.put(paramIndex, type);
                }
            }
            ps.setNull(paramIndex, type);
        }

        // 日期特殊处理，默认按照时间戳传入，避免毫秒丢失
        if (param instanceof java.util.Date) {
            if (param instanceof java.sql.Date) {
                ps.setDate(paramIndex, (java.sql.Date) param);
            } else if (param instanceof java.sql.Time) {
                ps.setTime(paramIndex, (java.sql.Time) param);
            } else {
                ps.setTimestamp(paramIndex, SqlUtil.toSqlTimestamp((java.util.Date) param));
            }
            return;
        }

        // 针对大数字类型的特殊处理
        if (param instanceof Number) {
            if (param instanceof BigDecimal) {
                // BigDecimal的转换交给JDBC驱动处理
                ps.setBigDecimal(paramIndex, (BigDecimal) param);
                return;
            }
            if (param instanceof BigInteger) {
                // BigInteger转为BigDecimal
                ps.setBigDecimal(paramIndex, new BigDecimal((BigInteger) param));
                return;
            }
            // 忽略其它数字类型，按照默认类型传入
        }

        //InputStream，解决oracle情况下setObject(inputStream)报错问题，java.sql.SQLException: 无效的列类型
        if(param instanceof InputStream){
            ps.setBinaryStream(paramIndex, (InputStream) param);
            return;
        }

        //java.sql.Blob
        if(param instanceof Blob){
            ps.setBlob(paramIndex, (Blob) param);
        }


        // 其它参数类型
        ps.setObject(paramIndex, param);
    }

    public static int getTypeOfNull(PreparedStatement ps, int paramIndex) {
        int sqlType = Types.VARCHAR;

        final ParameterMetaData pmd;
        try {
            pmd = ps.getParameterMetaData();
            if (pmd == null) {
                return sqlType;
            }
            sqlType = pmd.getParameterType(paramIndex);
        } catch (SQLException ignore) {
            // ignore
            // log.warn("Null param of index [{}] type get failed, by: {}", paramIndex, e.getMessage());
        }

        return sqlType;
    }

}
