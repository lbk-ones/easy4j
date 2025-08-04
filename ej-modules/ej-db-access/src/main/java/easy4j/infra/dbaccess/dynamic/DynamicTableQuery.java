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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.StatementUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.header.CheckUtils;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.condition.Condition;
import easy4j.infra.dbaccess.condition.SqlBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.InformationSchema;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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
@Accessors(chain = true)
public class DynamicTableQuery extends CommonDBAccess {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DataSource dataSource;

    private String schema;

    private String tableName;


    private WhereBuild whereBuild;

    private boolean isPrintSqlLog;

    // all fields to underline,if false then field and condition will keep not change
    private boolean toUnderLine = true;

    // chec the fields is exists
    private boolean checkFieldExists = true;

    // check field ignore case
    private boolean checkFieldIgnoreCase = true;


    private int pageSize;

    private int pageNo;

    /**
     * 条件查询
     *
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

    /**
     * 全查询
     *
     * @param dataSource 数据源
     * @param schema     schema
     * @param tableName  表名
     */
    public DynamicTableQuery(DataSource dataSource, String schema, String tableName) {
        this.dataSource = dataSource;
        this.schema = schema;
        this.tableName = tableName;
    }

    public DynamicTableQuery setPrintSqlLog(boolean printSqlLog) {
        isPrintSqlLog = printSqlLog;
        this.setPrintLog(printSqlLog);
        return this;
    }

    public DynamicTableQuery setToUnderLine(boolean toUnderLine) {
        this.toUnderLine = toUnderLine;
        WhereBuild whereBuild1 = this.getWhereBuild();
        if (null != whereBuild1) {
            whereBuild1.setToUnderLine(this.toUnderLine);
        }
        return this;
    }

    public String handlerColumn(String columnName) {
        Dialect dialect = this.whereBuild.getDialect();
        Wrapper wrapper = dialect.getWrapper();
        String s = columnName;
        try {
            char preWrapQuote = wrapper.getPreWrapQuote();
            char sufWrapQuote = wrapper.getSufWrapQuote();
            s = StrUtil.unWrap(columnName, preWrapQuote, sufWrapQuote);
        } catch (Throwable ignored) {
        }
        String underlineCase = s;
        if (toUnderLine) {
            underlineCase = StrUtil.toUnderlineCase(s);
        }
        return underlineCase;
    }

    public Set<String> getAllFields(WhereBuild whereBuild1) {
        Set<String> allFields = new HashSet<>();
        List<Condition> conditions = whereBuild1.getConditions();
        conditions.forEach(e -> allFields.add(handlerColumn(e.getColumn())));
        List<String> fields = whereBuild1.getSelectFieldsStr();
        fields.forEach(e -> allFields.add(handlerColumn(e)));
        List<Condition> orderBy = whereBuild1.getOrderBy();
        orderBy.forEach(e -> allFields.add(handlerColumn(e.getColumn())));
        List<Condition> groupBy = whereBuild1.getGroupBy();
        groupBy.forEach(e -> allFields.add(handlerColumn(e.getColumn())));
        return allFields;
    }

    public void getFields(WhereBuild whereBuild, Set<String> allFields) {
        allFields.addAll(getAllFields(whereBuild));
        List<WhereBuild> subBuilders = whereBuild.getSubBuilders();
        for (WhereBuild subBuilder : subBuilders) {
            getFields(subBuilder, allFields);
        }
    }

    public void checkFields(Set<String> allFields, List<DynamicColumn> columns) {
        Set<String> notEqualFields = new HashSet<>();
        for (String allField : allFields) {
            boolean result = false;
            for (DynamicColumn column : columns) {
                String columnName = column.getColumnName();
                String s = handlerColumn(columnName);
                if (checkFieldIgnoreCase) {
                    allField = allField.toLowerCase();
                    s = s.toLowerCase();
                }
                if (StrUtil.equals(allField, s)) {
                    result = true;
                    break;
                }
            }
            if (!result) {
                notEqualFields.add(allField);
            }
        }
        if (!notEqualFields.isEmpty()) {
            String join = String.join("、", notEqualFields);
            String finalTableName = (StrUtil.isBlank(this.schema) ? "" : this.schema + ".") + this.tableName;
            throw new EasyException("the field 【" + join + "】 is not exists in table " + finalTableName);
        }
    }

    public List<Dict> query() {
        if (this.whereBuild == null) {
            this.whereBuild = WhereBuild.get();
        }
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
            Dialect dialect1 = JdbcHelper.getDialect(connection);
            this.whereBuild.setToUnderLine(this.toUnderLine);
            this.whereBuild.bind(connection);
            this.whereBuild.bind(dialect1);
            // get columns information schema from db
            List<DynamicColumn> columns = InformationSchema.getColumns(this.dataSource, this.schema, this.tableName, connection);
            if (CollUtil.isEmpty(this.whereBuild.getSelectFieldsStr())) {
                List<String> collect = columns.stream()
                        .map(DynamicColumn::getColumnName)
                        .filter(StrUtil::isNotBlank)
                        .collect(Collectors.toList());
                this.whereBuild.select(collect.toArray(new String[]{}));
            }
            // select fields transform to underline fields
            if (toUnderLine) {
                List<String> selectFields = this.whereBuild.getSelectFieldsStr().stream().map(StrUtil::toUnderlineCase).collect(Collectors.toList());
                this.whereBuild.clearSelectFields();
                this.whereBuild.select(selectFields.toArray(new String[]{}));
            }
            // check fields is exists in table
            if (checkFieldExists) {
                Set<String> allFields = new HashSet<>();
                getFields(this.whereBuild, allFields);
                checkFields(allFields, columns);
            }
            // build sql
            SqlBuild sqlBuild = SqlBuild.get();
            String finalTableName = (StrUtil.isBlank(this.schema) ? "" : this.schema + ".") + this.tableName;
            sql = sqlBuild.buildByTableName(
                    SqlBuild.SELECT,
                    this.whereBuild,
                    finalTableName, null, true, args, connection
            );
            // page
            if (pageSize > 0) {
                Page<Object> objectPage = new Page<>();
                objectPage.setPageNo(pageNo);
                objectPage.setPageSize(pageSize);
                sql = dialect1.getPageSql(sql, objectPage);
            }
            // prepare statement
            MapListHandler tBeanListHandler = new MapListHandler();
            stringDatePair = recordSql(sql, connection, args.toArray(new Object[]{}));
            if (ObjectUtil.isNotEmpty(args)) {
                preparedStatement = StatementUtil.prepareStatement(connection, sql, args);
            } else {
                preparedStatement = StatementUtil.prepareStatement(connection, sql);
            }
            resultSet = preparedStatement.executeQuery();
            // handler result
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