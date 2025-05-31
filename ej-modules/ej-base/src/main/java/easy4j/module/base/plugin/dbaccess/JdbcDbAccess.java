/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.StatementUtil;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.jdbc.datasource.init.ScriptUtils.*;

/**
 * JdbcDbAccess
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class JdbcDbAccess extends AbstractDBAccess implements DBAccess {

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
    public <T> T getObject(String sql, Class<T> clazz, Object... args) {
        List<T> query = getObjectList(sql, clazz, args);
        return JdbcHelper.requiredSingleResult(query);
    }

    // 查多个
    @Override
    public <T> List<T> getObjectList(String sql, Class<T> clazz, Object... args) {
        Connection connection = getConnection();
        BeanPropertyHandler<T> tBeanListHandler = new BeanPropertyHandler<>(clazz);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            logSql(sql, args);
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            List<T> t = tBeanListHandler.handle(resultSet);

            return ObjectUtil.defaultIfNull(t, new ArrayList<>());
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("getObjectList", sql, e);
        } finally {
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    // 更新 插入 删除
    @Override
    public int saveOrUpdate(Map<String, Object> map) {
        Object o = map.get(KEY_SQL);
        if (ObjectUtil.isEmpty(o)) {
            return 0;
        }
        Connection connection = Convert.convert(Connection.class, map.get(CONNECTION));
        if (connection == null) {
            connection = getConnection();
        }
        Object args = map.get(KEY_ARGS);
        final String sql = (String) o;
        PreparedStatement preparedStatement = null;
        try {
            logSql(sql, args);
            int effectRows;
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, (Object[]) args);
                effectRows = preparedStatement.executeUpdate();
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
                effectRows = preparedStatement.executeUpdate();
            }
            return effectRows;
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("saveOrUpdate", sql, e);
        } finally {
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }


    }


    // 执行脚本、执行sql文件
    @Override
    public void runScript(Resource resource) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            EncodedResource encodedResource = new EncodedResource(resource, StandardCharsets.UTF_8);
            executeSqlScript(connection, encodedResource, false, false, DEFAULT_COMMENT_PREFIX, DEFAULT_STATEMENT_SEPARATOR,
                    DEFAULT_BLOCK_COMMENT_START_DELIMITER, DEFAULT_BLOCK_COMMENT_END_DELIMITER);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("saveOrUpdate", null, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }

    }

    // 拿取Connection连接
    @Override
    public Connection getConnection() {
        try {
            Assert.notNull(dataSource);
            if (this.inTransaction) {
                return DataSourceUtils.getConnection(dataSource);
            } else {
                return dataSource.getConnection();
            }
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("getConnection", null, e);
        }
    }

    /**
     * 查询指定列
     *
     * @return 指定列的结果对象
     */
    public Object query(Map<String, Object> map) {
        Connection connection = Convert.convert(Connection.class, map.get(CONNECTION));
        if (connection == null) {
            connection = getConnection();
        }
        String sql = Convert.toStr(map.get(KEY_SQL));
        Object args = map.get(KEY_ARGS);
        Integer column = Convert.toInt(map.get(INDEX_COLUMN));
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            logSql(sql, args);
            ScalarHandler<Object> objectScalarHandler = new ScalarHandler<>(column);
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, (Object[]) args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            return objectScalarHandler.handle(resultSet);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("query", sql, e);
        } finally {
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    @Override
    protected Object queryCount(Map<String, Object> map) {
        map.put(INDEX_COLUMN, 1);
        return query(map);
    }
}
