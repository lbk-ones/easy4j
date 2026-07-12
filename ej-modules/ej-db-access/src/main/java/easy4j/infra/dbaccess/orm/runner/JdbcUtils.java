package easy4j.infra.dbaccess.orm.runner;

import cn.hutool.db.StatementUtil;

import java.sql.*;
import java.util.*;

public class JdbcUtils {

    private final Connection connection;

    public JdbcUtils(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * 增删改
     *
     * @return 影响行数
     */
    public int update(String sql, List<Object> params) {

        try (
                PreparedStatement ps = getConnection().prepareStatement(sql)
        ) {
            StatementUtil.fillParams(ps,params.toArray(new Object[]{}));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 查询
     * <p>
     * 返回:
     * <p>
     * [
     * {
     * id:1,
     * name:"张三"
     * }
     * ]
     */
    public List<Map<String, Object>> query(
            String sql,
            List<Object> params
    ) {
        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            setParams(ps, params);
            ResultSet rs = ps.executeQuery();
            List<Map<String, Object>> result =  new ArrayList<>();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String column = meta.getColumnLabel(i);
                    row.put(
                            column,
                            rs.getObject(i)
                    );
                }
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 批量新增
     * <p>
     * params:
     * <p>
     * [
     * [张三,18],
     * [李四,20]
     * ]
     */
    public int[] batchInsert(
            String sql,
            List<List<Object>> params
    ) {

        return batch(sql, params);
    }


    /**
     * 批量删除
     * <p>
     * 示例:
     * <p>
     * delete from user where id=?
     *
     */
    public int[] batchDelete(
            String sql,
            List<Object> ids
    ) {

        List<List<Object>> params = new ArrayList<>();

        for (Object id : ids) {
            params.add(List.of(id));
        }

        return batch(sql, params);
    }


    /**
     * 批量修改
     * <p>
     * 示例:
     * <p>
     * update user
     * set name=?
     * where id=?
     *
     */
    public int[] batchUpdate(
            String sql,
            List<List<Object>> params
    ) {
        return batch(sql, params);
    }


    /**
     * JDBC批处理
     */
    private int[] batch(
            String sql,
            List<List<Object>> params
    ) {

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            for (List<Object> row : params) {

                setParams(ps, row);

                ps.addBatch();
            }


            return ps.executeBatch();


        } catch (SQLException e) {

            throw new RuntimeException(e);
        }

    }


    /**
     * 设置参数
     */
    private void setParams(
            PreparedStatement ps,
            List<Object> params
    ) throws SQLException {


        if (params == null) {
            return;
        }


        StatementUtil.fillParams(ps,params.toArray(new Object[]{}));
    }

}