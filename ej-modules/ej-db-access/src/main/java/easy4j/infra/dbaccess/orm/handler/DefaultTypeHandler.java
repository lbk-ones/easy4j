package easy4j.infra.dbaccess.orm.handler;

import cn.hutool.db.meta.JdbcType;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.dbaccess.orm.runner.StatementUtils;

import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 默认类型转换器实现
 */
public class DefaultTypeHandler extends BaseTypeHandler<Object> implements TypeHandler<Object>{

    public static final DefaultTypeHandler INSTANCE = new DefaultTypeHandler();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        StatementUtils.setParam(ps,i,parameter,null);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Type rawType = getRawType();
        Class<?> rawType1 = (Class<?>) rawType;
        return JdbcHelper.getResultSetValue(rs, columnName, rawType1);
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Type rawType = getRawType();
        Class<?> rawType1 = (Class<?>) rawType;
        return JdbcHelper.getResultSetValue(rs, columnIndex, rawType1);
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getObject(columnIndex);
    }
}
