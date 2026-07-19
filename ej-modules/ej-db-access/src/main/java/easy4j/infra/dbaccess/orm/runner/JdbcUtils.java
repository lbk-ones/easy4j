package easy4j.infra.dbaccess.orm.runner;

import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.Getter;

import java.sql.*;
import java.util.*;

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

        try (
                Connection connection1 = getConnection();
                PreparedStatement ps = connection1.prepareStatement(sql)
        ) {
            StatementUtils.fillParams(runtimeContext,ps,args.toArray(new Object[]{}));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public ResultSet query(
            RuntimeContext<?> runtimeContext
    ) {
        String sql = runtimeContext.getSql();
        List<Object> args = runtimeContext.getArgs();
        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            Integer fetchSize = runtimeContext.getAccessUtils().getAccessConfig().getFetchSize();
            if(fetchSize!=null){
                ps.setFetchSize(fetchSize);
            }
            StatementUtils.fillParams(runtimeContext,ps,args.toArray(new Object[]{}));
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}