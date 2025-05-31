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
package easy4j.module.base.plugin.dbaccess.condition;

import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQL 条件构建器，支持 AND、OR、NOT 等逻辑组合，以及各种比较条件。
 * 生成预编译的sql含占位符
 * 字段名称会自动转下化线
 * <p>
 * // 示例 1：简单条件
 * String condition1 = SqlBuilder.get()
 * .equal("age", 30)
 * .and(SqlBuilder.get()
 * .equal("gender", "F")
 * .or(SqlBuilder.get()
 * .equal("department", "IT")
 * .ne("salary", 5000)
 * )).build(argList);
 * System.out.println("条件 1: " + condition1);
 * System.out.println("值 1: " + JacksonUtil.toJson(argList));
 * argList.clear();
 * // 输出: age = 30 AND (gender = 'F' OR (department = 'IT' AND salary != 5000))
 * <p>
 * // 示例 2：复杂条件
 * String condition2 = SqlBuilder.get()
 * .withLogicOperator(LogicOperator.OR)
 * .like("name", "A%")
 * .in("department", ListTs.asList("IT", "HR"))
 * .between("salary", 3000, 5000)
 * .not(SqlBuilder.get()
 * .isNull("email")
 * .or(SqlBuilder.get()
 * .equal("status", "INACTIVE")
 * )).build(argList);
 * System.out.println("条件 2: " + condition2);
 * System.out.println("值 2: " + JacksonUtil.toJson(argList));
 * // 输出: name LIKE 'A%' OR department IN ('IT', 'HR') OR salary BETWEEN 3000 AND 5000 OR NOT (email IS NULL OR status = 'INACTIVE')
 * <p>
 * // 示例 3：用于 SQL 查询
 * String sql = "SELECT * FROM employees WHERE " + condition1;
 * System.out.println("完整 SQL: " + sql);
 * <p>
 *
 * @author bokun.li
 */
public class SqlBuild {

    private final List<Condition> conditions = new ArrayList<>();
    private final List<SqlBuild> subBuilders = new ArrayList<>();
    private LogicOperator logicOperator = LogicOperator.AND; // 默认使用 AND 连接条件


    public Connection connection;
    public Dialect dialect;

    public void bind(Connection connection) {
        this.connection = connection;
    }

    public void bind(Dialect dialect) {
        this.dialect = dialect;
    }

    // 设置逻辑运算符
    public SqlBuild withLogicOperator(LogicOperator operator) {
        this.logicOperator = operator;
        return this;
    }

    // 基础比较条件方法
    public SqlBuild equal(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.EQUAL, value));
        return this;
    }

    public SqlBuild ne(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.NOT_EQUAL, value));
        return this;
    }

    public SqlBuild gt(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.GREATER_THAN, value));
        return this;
    }

    public SqlBuild lt(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.LESS_THAN, value));
        return this;
    }

    public SqlBuild gte(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.GREATER_OR_EQUAL, value));
        return this;
    }

    public SqlBuild lte(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.LESS_OR_EQUAL, value));
        return this;
    }

    // LIKE 条件
    public SqlBuild like(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, value));
        return this;
    }

    public SqlBuild notLike(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.NOT_LIKE, value));
        return this;
    }

    // IN 条件
    public SqlBuild in(String column, List<?> values) {
        conditions.add(new Condition(column, CompareOperator.IN, values));
        return this;
    }

    public SqlBuild in(String column, Object... values) {
        conditions.add(new Condition(column, CompareOperator.IN, Arrays.asList(values)));
        return this;
    }

    public SqlBuild notIn(String column, List<?> values) {
        conditions.add(new Condition(column, CompareOperator.NOT_IN, values));
        return this;
    }

    public SqlBuild notIn(String column, Object... values) {
        conditions.add(new Condition(column, CompareOperator.NOT_IN, Arrays.asList(values)));
        return this;
    }

    // BETWEEN 条件
    public SqlBuild between(String column, Object value1, Object value2) {
        conditions.add(new Condition(column, CompareOperator.BETWEEN, Arrays.asList(value1, value2)));
        return this;
    }

    // NULL 条件
    public SqlBuild isNull(String column) {
        conditions.add(new Condition(column, CompareOperator.IS_NULL, null));
        return this;
    }

    public SqlBuild isNotNull(String column) {
        conditions.add(new Condition(column, CompareOperator.IS_NOT_NULL, null));
        return this;
    }

    // 构建子条件
    public SqlBuild and(SqlBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.AND);
        subBuilders.add(subBuilder);
        return this;
    }

    public SqlBuild or(SqlBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.OR);
        subBuilders.add(subBuilder);
        return this;
    }

    public SqlBuild not(SqlBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.NOT);
        subBuilders.add(subBuilder);
        return this;
    }

    // 清除条件
    public void clear() {
        conditions.clear();
        subBuilders.clear();
        withLogicOperator(LogicOperator.AND);
    }

    // 构建最终 SQL 条件
    public String build(List<Object> argsList) {

        if (conditions.isEmpty() && subBuilders.isEmpty()) {
            return "";
        }

        if (this.connection == null) {
            throw new EasyException("condition is not bind connection please bind a connection");
        }

        if (this.dialect == null) {
            this.dialect = JdbcHelper.getDialect(this.connection);
        }

        List<String> parts = new ArrayList<>();
        // 添加基本条件
        for (Condition condition : conditions) {
            parts.add(condition.getSqlSegment(argsList, this.dialect));
        }
        // 添加子条件
        for (SqlBuild subBuilder : subBuilders) {
            subBuilder.bind(dialect);
            subBuilder.bind(connection);
            String subCondition = subBuilder.build(argsList);
            if (!subCondition.isEmpty()) {
                if (subBuilder.logicOperator == LogicOperator.NOT) {
                    parts.add("NOT (" + subCondition + ")");
                } else {
                    parts.add("(" + subCondition + ")");
                }
            }
        }

        // 使用逻辑运算符连接所有条件
        String operator = logicOperator == LogicOperator.AND ? " AND " : " OR ";
        return String.join(operator, parts);
    }

    // 静态工厂方法
    public static SqlBuild get() {
        return new SqlBuild();
    }

    public static SqlBuild get(Connection connection, Dialect dialect) {

        SqlBuild sqlBuilder = new SqlBuild();
        sqlBuilder.bind(connection);
        sqlBuilder.bind(dialect);
        return sqlBuilder;
    }

    public static SqlBuild get(Connection connection) {

        SqlBuild sqlBuilder = new SqlBuild();
        sqlBuilder.bind(connection);
        return sqlBuilder;
    }
}