package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.ListTs;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.jdbc.datasource.init.ScriptUtils.*;
import static org.springframework.jdbc.datasource.init.ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER;

@Slf4j
public class JdbcDbAccess extends AbstractDBAccess implements DBAccess {

    private final QueryRunner runner = new QueryRunner(true);

    private DataSource dataSource;

    @Setter
    private boolean inTransaction = false;

    @Override
    public void init(Object object) {
        if (object instanceof DataSource) {
            this.dataSource = (DataSource) object;
        }
    }

    // 查单个
    @Override
    public <T> T getObject(String sql, Class<T> clazz, Object... args) throws SQLException {
        BeanPropertyHandler<T> tBeanListHandler = new BeanPropertyHandler<>(clazz);
        List<T> query = runner.query(getConnection(), sql, tBeanListHandler, args);
        return JdbcHelper.requiredSingleResult(query);
    }

    // 查多个
    @Override
    public <T> List<T> getObjectList(String sql, Class<T> clazz, Object... args) throws SQLException {
        BeanPropertyHandler<T> tBeanListHandler = new BeanPropertyHandler<>(clazz);
        return runner.query(getConnection(), sql, tBeanListHandler, args);
    }

    // 更新 插入 删除
    @Override
    public int saveOrUpdate(Map<String, Object> map) throws SQLException {
        Object o = map.get(KEY_SQL);
        if (ObjectUtil.isEmpty(o)) {
            return 0;
        }
        Object args = map.get(KEY_ARGS);
        if (Objects.nonNull(args)) {
            args = (Object[]) args;
        }
        final String sql = (String) o;
        if (ObjectUtil.isNotEmpty(args)) {
            return runner.update(getConnection(), sql, args);
        } else {
            return runner.update(getConnection(), sql);
        }

    }


    // 执行脚本、执行sql文件
    @Override
    public void runScript(Resource resource) throws SQLException {
        Connection connection = dataSource.getConnection();
        EncodedResource encodedResource = new EncodedResource(resource, StandardCharsets.UTF_8);
        executeSqlScript(connection, encodedResource, false, false, DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
                DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
    }

    // 拿取Connection连接
    @Override
    public Connection getConnection() throws SQLException {
        Assert.notNull(dataSource);
        if (this.inTransaction) {
            return DataSourceUtils.getConnection(dataSource);
        } else {
            return dataSource.getConnection();
        }
    }

    /**
     * 查询指定列
     *
     * @param column 结果集的列索引号
     * @param sql    sql语句
     * @param params 查询参数
     * @return 指定列的结果对象
     */
    public Object query(int column, String sql, Object... params) throws SQLException {
        Object result;
        try {
            if (log.isDebugEnabled()) {
                log.debug("查询单列数据=\n" + sql);
            }
            result = runner.query(getConnection(), sql, new ScalarHandler<>(column), params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
        return result;
    }

    @Override
    protected Object queryCount(String sql, Object... args) throws SQLException {
        return query(1, sql, args);
    }
}
