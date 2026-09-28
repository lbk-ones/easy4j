package easy4j.infra.dbaccess.orm.runner;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * PreparedStatement 执行器接口
 * 用于统一处理参数赋值和SQL执行
 *
 * @see ExecutorUtil
 * @author bokun.li
 * @since 2.1.5.8
 */
public interface Executor {

    /**
     * 设置 PreparedStatement 的参数
     *
     * @param pstmt 预编译语句
     * @throws SQLException 数据库异常
     */
    void setParameters(PreparedStatement pstmt) throws SQLException;

    /**
     * 执行查询操作
     *
     * @param pstmt 预编译语句
     * @return 查询结果集
     * @throws SQLException 数据库异常
     */
    default ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        setParameters(pstmt);
        return pstmt.executeQuery();
    }

    /**
     * 执行更新操作（INSERT/UPDATE/DELETE）
     *
     * @param pstmt 预编译语句
     * @return 影响的行数
     * @throws SQLException 数据库异常
     */
    default int executeUpdate(PreparedStatement pstmt) throws SQLException {
        setParameters(pstmt);
        return pstmt.executeUpdate();
    }

    /**
     * 执行批处理操作
     *
     * @param pstmt 预编译语句
     * @return 每条语句影响的行数数组
     * @throws SQLException 数据库异常
     */
    default int[] executeBatch(PreparedStatement pstmt) throws SQLException {
        setParameters(pstmt);
        return pstmt.executeBatch();
    }
}
