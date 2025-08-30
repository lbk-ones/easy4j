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
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.CommonDBAccess;
import easy4j.infra.dbaccess.Page;
import easy4j.infra.dbaccess.condition.Condition;
import easy4j.infra.dbaccess.condition.SqlBuild;
import easy4j.infra.dbaccess.condition.WhereBuild;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dynamic.schema.DynamicColumn;
import easy4j.infra.dbaccess.dynamic.schema.InformationSchema;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static easy4j.infra.dbaccess.condition.SqlBuild.distinctSql;

/**
 * DynamicTableQuery
 * 动态表查询
 *
 * @author bokun.li
 * @date 2025-07-31 19:41:07
 */
@EqualsAndHashCode(callSuper = true)
public class DynamicTableQuery extends CommonDBAccess {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Getter
    private DataSource dataSource;
    @Getter
    private String schema;
    @Getter
    private String tableName;

    @Getter
    private WhereBuild whereBuild;

    @Getter
    private boolean isPrintSqlLog;

    // all fields to underline,if false then field and condition will keep not change
    @Getter
    private boolean toUnderLine = true;

    // chec the fields is exists
    @Getter
    private boolean checkFieldExists = true;
    // check field ignore case
    @Getter
    private boolean checkFieldIgnoreCase = true;
    // pageSize
    @Getter
    private int pageSize;
    // pageNo
    @Getter
    private int pageNo;

    // connection
    protected Connection connection = null;

    // resultSet
    protected ResultSet resultSet = null;

    // preparedStatement
    protected PreparedStatement preparedStatement = null;

    // finalSql
    protected String sql = null;

    // record sql
    protected Pair<String, Date> stringDatePair = null;

    // effectRows
    protected int effectRows = -1;

    // db dialect
    protected Dialect dialect;


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
        super.setPrintLog(printSqlLog);
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

    public DynamicTableQuery setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public DynamicTableQuery setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public DynamicTableQuery setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public DynamicTableQuery setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public DynamicTableQuery setCheckFieldExists(boolean checkFieldExists) {
        this.checkFieldExists = checkFieldExists;
        return this;
    }

    public DynamicTableQuery setCheckFieldIgnoreCase(boolean checkFieldIgnoreCase) {
        this.checkFieldIgnoreCase = checkFieldIgnoreCase;
        return this;
    }

    public DynamicTableQuery setPageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public DynamicTableQuery setWhereBuild(WhereBuild whereBuild) {
        this.whereBuild = whereBuild;
        return this;
    }

    private String handlerColumn(String columnName) {
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

    private Set<String> getAllFields(WhereBuild whereBuild1) {
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

    private void getFields(WhereBuild whereBuild, Set<String> allFields) {
        allFields.addAll(getAllFields(whereBuild));
        List<WhereBuild> subBuilders = whereBuild.getSubBuilders();
        for (WhereBuild subBuilder : subBuilders) {
            getFields(subBuilder, allFields);
        }
    }

    private void checkFields(Set<String> allFields, List<DynamicColumn> columns) {
        Set<String> notEqualFields = new HashSet<>();
        for (String allField : allFields) {
            boolean result = false;
            for (DynamicColumn column : columns) {
                String s = column.getColumnName();
                allField = handlerColumn(allField);
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

    // 会检查参数是否存在
    public List<Dict> query() {
        CheckUtils.notNull(dataSource, "datasource is not null");
        CheckUtils.notNull(tableName, "tableName is not null");
        try {
            List<Object> args = init();
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
            return queryByArgs(args);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("dynamic query", sql, e);
        } finally {
            printSql(stringDatePair, effectRows);
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }

    /**
     * 判断字符串中是否包含被圆括号()包裹的内容
     */
    public static boolean hasParenthesesContent(String str) {
        // 只匹配完整的圆括号对及其内容
        String regex = "\\([^)]*\\)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);

        // 判断是否有匹配项
        return matcher.find();
    }

    // 指定参数且不检查参数是否存在
    public List<Dict> queryNoCheck(List<String> fields, List<String> checkFields) {
        CheckUtils.notNull(dataSource, "datasource is not null");
        CheckUtils.notNull(tableName, "tableName is not null");
        try {
            if (null == fields) {
                fields = ListTs.newList();
            }
            List<Object> args = init();
            super.setToUnderline(toUnderLine);
            // get sql
            String build = this.whereBuild.build(args);
            List<String> selectFieldsStr = this.whereBuild.getSelectFieldsStr();
            if (CollUtil.isNotEmpty(fields)) {
                selectFieldsStr.addAll(fields);
            }
            checkPickFields(fields, checkFields);
            sql = DDlLine(SELECT, tableName, where(build), selectFieldsStr.toArray(new String[]{}));
            sql = distinctSql(this.whereBuild, selectFieldsStr, sql);
            // page
            return queryByArgs(args);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("dynamic query", sql, e);
        } finally {
            printSql(stringDatePair, effectRows);
            JdbcHelper.close(resultSet);
            JdbcHelper.close(preparedStatement);
            DataSourceUtils.releaseConnection(connection, getDataSource());
        }
    }

    /**
     * CheckPickFields
     * 兼容检查字段是否存在
     *
     * @author bokun.li
     * @date 2025/8/7
     */
    private void checkPickFields(List<String> selectFieldsStr, List<String> checkFields) {
        try {
            Set<String> allFields = new HashSet<>();
            if (CollUtil.isNotEmpty(selectFieldsStr)) {
                Wrapper wrapper = this.dialect.getWrapper();
                String preWrapQuote = "";
                String sufWrapQuote = "";
                try {
                    preWrapQuote = String.valueOf(wrapper.getPreWrapQuote());
                    sufWrapQuote = String.valueOf(wrapper.getSufWrapQuote());
                } catch (Exception ignored) {
                }
                String finalPreWrapQuote = preWrapQuote;
                String finalSufWrapQuote = sufWrapQuote;
                // skip special character
                // skip  contain empty str 、 contain () 、contain as 、 escape wrap
                List<String> collect = selectFieldsStr.stream().filter(e ->
                        {
                            e = StrUtil.trim(e);
                            if (StrUtil.isEmpty(e)) {
                                return false;
                            }
                            boolean isEscape = StrUtil.isNotBlank(finalPreWrapQuote) && StrUtil.isNotBlank(finalSufWrapQuote) && StrUtil.isWrap(e, finalPreWrapQuote, finalSufWrapQuote);
                            if (isEscape) {
                                return false;
                            }
                            return !(e.contains(SP.SPACE) || hasParenthesesContent(e) || StrUtil.containsIgnoreCase(e, " as "));
                        }
                ).collect(Collectors.toList());
                if (CollUtil.isNotEmpty(collect)) {
                    allFields.addAll(collect);
                }
            }
            if (CollUtil.isNotEmpty(checkFields)) {
                allFields.addAll(checkFields);
            }
            getFields(this.whereBuild, allFields);
            List<DynamicColumn> columns = InformationSchema.getColumns(this.dataSource, this.schema, this.tableName, connection);
            checkFields(allFields, columns);
        } catch (SQLException e) {
            throw JdbcHelper.translateSqlException("checkPickFields", null, e);
        }

    }

    private List<Dict> queryByArgs(List<Object> args) throws SQLException {
        sql = page(sql, dialect);
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
        effectRows = CollUtil.isEmpty(maps) ? 0 : maps.size();
        return maps.stream().map(Dict::new).collect(Collectors.toList());
    }

    // 初始化
    private List<Object> init() throws SQLException {
        if (this.whereBuild == null) {
            this.whereBuild = WhereBuild.get();
        }
        connection = DataSourceUtils.getConnection(this.dataSource);
        List<Object> args = ListTs.newArrayList();
        dialect = JdbcHelper.getDialect(connection);
        this.whereBuild.setToUnderLine(this.toUnderLine);
        this.whereBuild.bind(connection);
        this.whereBuild.bind(dialect);
        return args;
    }

    private String page(String sql, Dialect dialect1) {
        if (pageSize > 0) {
            Page<Object> objectPage = new Page<>();
            objectPage.setPageNo(pageNo);
            objectPage.setPageSize(pageSize);
            sql = dialect1.getPageSql(sql, objectPage);
        }
        return sql;
    }
}