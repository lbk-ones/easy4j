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
package easy4j.infra.dbaccess.dynamic;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.db.StatementUtil;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.condition.SqlBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.InformationSchema;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamicTableQuery
 * 动态表查询
 *
 * @author bokun.li
 * @date 2025-07-31 19:41:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DynamicTableQuery extends CommonDBAccess {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DataSource dataSource;

    private String schema;

    private String tableName;


    private WhereBuild whereBuild;

    private boolean isPrintSqlLog;

    /**
     * @param whereBuild 条件构造器
     * @param dataSource 数据源
     * @param schema     schema
     * @param tableName  表名
     */
    public DynamicTableQuery(WhereBuild whereBuild, DataSource dataSource, String schema, String tableName) {

        this.dataSource = dataSource;
        this.schema = schema;
        this.tableName = tableName;
        this.whereBuild = whereBuild;
    }

    public DynamicTableQuery setPrintSqlLog(boolean printSqlLog) {
        isPrintSqlLog = printSqlLog;
        this.setPrintLog(printSqlLog);
        return this;
    }

    public List<Dict> query() {
        CheckUtils.notNull(whereBuild, "where build is not null");
        CheckUtils.notNull(dataSource, "datasource is not null");
        CheckUtils.notNull(tableName, "tableName is not null");
        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        String sql = null;
        Pair<String, Date> stringDatePair = null;
        try {
            connection = this.dataSource.getConnection();
            List<Object> args = ListTs.newArrayList();
            List<DynamicColumn> columns = InformationSchema.getColumns(this.dataSource, this.schema, this.tableName, connection);
            List<String> collect = columns.stream().map(DynamicColumn::getColumnName).collect(Collectors.toList());
            this.whereBuild.bind(connection);
            Dialect dialect1 = JdbcHelper.getDialect(connection);
            this.whereBuild.bind(dialect1);
            this.whereBuild.select(collect.toArray(new String[]{}));
            SqlBuild sqlBuild = SqlBuild.get();
            sql = sqlBuild.buildByTableName(
                    SqlBuild.SELECT,
                    this.whereBuild,
                    this.tableName,
                    null,
                    true,
                    args,
                    connection
            );
            MapListHandler tBeanListHandler = new MapListHandler();
            stringDatePair = recordSql(sql, connection, args.toArray(new Object[]{}));
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            List<Map<String, Object>> maps = tBeanListHandler.handle(resultSet);
            return maps.stream().map(Dict::new).collect(Collectors.toList());
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("dynamic query", sql, e);
        } finally {
            printSql(stringDatePair);
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }
}