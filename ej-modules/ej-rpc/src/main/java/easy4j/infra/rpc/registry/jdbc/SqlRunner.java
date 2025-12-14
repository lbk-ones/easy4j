package easy4j.infra.rpc.registry.jdbc;

import cn.hutool.db.StatementUtil;
import org.apache.commons.dbutils.QueryRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 重写下参数填值逻辑
 *
 * @author bokun
 * @since 2.0.1
 */
public class SqlRunner extends QueryRunner {

    @Override
    public void fillStatement(PreparedStatement stmt, Object... params) throws SQLException {
        StatementUtil.fillParams(stmt, params);
    }

}
