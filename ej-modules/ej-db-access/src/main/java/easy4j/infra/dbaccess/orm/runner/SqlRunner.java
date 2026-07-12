package easy4j.infra.dbaccess.orm.runner;

import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.sql.Connection;
import java.util.List;

public class SqlRunner {

    private final AccessUtils accessUtils;

    public SqlRunner(AccessUtils accessUtils) {
        this.accessUtils = accessUtils;
    }

    public <T> Integer run(String sql, RuntimeContext<T> context){
        Connection connection = context.getConnection();
        List<Object> args = context.getUpdateArgs();
        JdbcUtils jdbcUtils = new JdbcUtils(connection);
        int update = jdbcUtils.update(sql, args);

        return update;
    }
}
