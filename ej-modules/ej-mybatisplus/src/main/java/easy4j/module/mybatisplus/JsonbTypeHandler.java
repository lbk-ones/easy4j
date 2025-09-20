package easy4j.module.mybatisplus;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 给pg得jsonb字段使用
 * 具体使用
 * <pre>
 *   @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
 *   private String testStrJsonB();
 * </pre>
 */
public class JsonbTypeHandler extends BaseTypeHandler<String> {
    private static final String JSONB_TYPE = "jsonb";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        PGobject jsonObject = new PGobject();
        jsonObject.setType(JSONB_TYPE);
        jsonObject.setValue(parameter);
        ps.setObject(i, jsonObject);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex);
    }
}
