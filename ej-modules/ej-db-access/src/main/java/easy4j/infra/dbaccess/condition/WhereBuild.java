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
package easy4j.infra.dbaccess.condition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.dbaccess.dialect.AbstractDialect;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.helper.JdbcHelper;
import easy4j.infra.common.exception.EasyException;
import jodd.util.StringPool;
import lombok.Getter;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * SQL 字符串条件构建器，支持 AND、OR、NOT 等逻辑组合，以及各种比较条件。
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
public class WhereBuild implements Serializable {
    @Getter
    private List<Condition> conditions = new ArrayList<>();
    @Getter
    private final List<Condition> groupBy = new ArrayList<>();
    @Getter
    private List<Condition> orderBy = new ArrayList<>();

    @Getter
    private final List<Condition> selectFields = new ArrayList<>();

    @Getter
    private final List<WhereBuild> subBuilders = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LogicOperator logicOperator = LogicOperator.AND; // 默认使用 AND 连接条件
    private boolean isSubSql = false;

    @Getter
    private boolean isDistinct;

    @JsonIgnore
    public Connection connection;

    @JsonIgnore
    public Dialect dialect;


    @Getter
    public boolean toUnderLine = true;

    public void setConditions(List<Condition> conditions) {
        if (null != conditions) {
            this.conditions = conditions;
        }
    }

    public void setOrderBy(List<Condition> orderBy) {
        if (null != orderBy) {
            this.orderBy = orderBy;
        }
    }

    public void setToUnderLine(boolean toUnderLine) {
        this.toUnderLine = toUnderLine;
    }



    @JsonIgnore
    public List<String> getSelectFieldsStr() {
        Wrapper wrapper = getDialect().getWrapper();
        return selectFields.stream()
                .peek(e -> e.setToUnderLine(this.toUnderLine))
                .map(e -> wrapper.wrap(e.getColumn()))
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    public void clearSelectFields() {
        this.selectFields.clear();
    }

    public Dialect getDialect() {
        if (this.dialect != null) {
            return this.dialect;
        } else if (this.connection != null) {
            this.dialect = JdbcHelper.getDialect(this.connection);
            return this.dialect;
        } else {
            return new AbstractDialect();
        }
    }

    public void bind(Connection connection) {
        this.connection = connection;
    }

    public void bind(Dialect dialect) {
        this.dialect = dialect;
    }

    // 设置逻辑运算符
    public WhereBuild withLogicOperator(LogicOperator operator) {
        this.logicOperator = operator;
        return this;
    }

    // 基础比较条件方法
    public WhereBuild equal(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.EQUAL, value));
        return this;
    }

    public WhereBuild ne(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.NOT_EQUAL, value));
        return this;
    }

    public WhereBuild gt(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.GREATER_THAN, value));
        return this;
    }

    public WhereBuild lt(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.LESS_THAN, value));
        return this;
    }

    public WhereBuild gte(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.GREATER_OR_EQUAL, value));
        return this;
    }

    public WhereBuild lte(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.LESS_OR_EQUAL, value));
        return this;
    }

    // LIKE 条件
    public WhereBuild like(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value + "%"));
        return this;
    }

    public WhereBuild likeLeft(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, value + "%"));
        return this;
    }

    public WhereBuild likeRight(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value));
        return this;
    }

    public WhereBuild notLike(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.NOT_LIKE, "%" + value + "%"));
        return this;
    }

    // IN 条件
    public WhereBuild in(String column, List<?> values) {
        conditions.add(new Condition(column, CompareOperator.IN, values));
        return this;
    }

    public WhereBuild inArray(String column, Object... values) {
        conditions.add(new Condition(column, CompareOperator.IN, Arrays.asList(values)));
        return this;
    }

    public WhereBuild notIn(String column, List<?> values) {
        conditions.add(new Condition(column, CompareOperator.NOT_IN, values));
        return this;
    }

    public WhereBuild notIn(String column, Object... values) {
        conditions.add(new Condition(column, CompareOperator.NOT_IN, Arrays.asList(values)));
        return this;
    }

    // BETWEEN 条件
    public WhereBuild between(String column, Object value1, Object value2) {
        conditions.add(new Condition(column, CompareOperator.BETWEEN, Arrays.asList(value1, value2)));
        return this;
    }

    // NULL 条件
    @JsonIgnore
    public WhereBuild isNull(String column) {
        conditions.add(new Condition(column, CompareOperator.IS_NULL, null));
        return this;
    }

    @JsonIgnore
    public WhereBuild isNotNull(String column) {
        conditions.add(new Condition(column, CompareOperator.IS_NOT_NULL, null));
        return this;
    }

    public WhereBuild distinct() {
        this.isDistinct = true;
        return this;
    }

    public WhereBuild select(String... columns) {
        if (!this.isSubSql) {
            List<Condition> map = ListTs.objectToListT(columns, Condition.class, e -> {
                String string = e.toString();
                return new Condition(string, CompareOperator.EMPTY, null);
            });
            if (CollUtil.isNotEmpty(map)) {
                selectFields.addAll(map);
            }
        }
        return this;
    }


    public WhereBuild groupBy(String... column) {
        if (!this.isSubSql) {
            List<Condition> map = ListTs.objectToListT(column, Condition.class, e -> {
                String string = e.toString();
                return new Condition(string, CompareOperator.EMPTY, null);
            });
            if (CollUtil.isNotEmpty(map)) {
                groupBy.addAll(map);
            }
        }
        return this;
    }

    public WhereBuild asc(String... column) {
        if (!this.isSubSql) {
            List<Condition> map = ListTs.objectToListT(column, Condition.class, e -> {
                String string = e.toString();
                return new Condition(string, CompareOperator.EMPTY, "ASC");
            });
            if (CollUtil.isNotEmpty(map)) {
                orderBy.addAll(map);
            }
        }
        return this;
    }

    public WhereBuild desc(String... column) {
        if (!this.isSubSql) {
            List<Condition> map = ListTs.objectToListT(column, Condition.class, e -> {
                String string = e.toString();
                return new Condition(string, CompareOperator.EMPTY, "DESC");
            });
            if (CollUtil.isNotEmpty(map)) {
                orderBy.addAll(map);
            }
        }
        return this;
    }

    // 构建子条件
    public WhereBuild and(WhereBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.AND);
        subBuilder.bind(this.dialect);
        subBuilder.bind(this.connection);
        subBuilder.isSubSql = true;
        subBuilders.add(subBuilder);
        return this;
    }

    public WhereBuild and(Consumer<WhereBuild> subBuilder) {
        WhereBuild whereBuild = get();
        subBuilder.accept(whereBuild);
        whereBuild.withLogicOperator(LogicOperator.AND);
        whereBuild.bind(this.dialect);
        whereBuild.bind(this.connection);
        whereBuild.isSubSql = true;
        subBuilders.add(whereBuild);
        return this;
    }

    public WhereBuild or(WhereBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.OR);
        subBuilder.bind(this.dialect);
        subBuilder.bind(this.connection);
        subBuilders.add(subBuilder);
        subBuilder.isSubSql = true;
        return this;
    }

    public WhereBuild or(Consumer<WhereBuild> subBuilder) {
        WhereBuild whereBuild = get();
        subBuilder.accept(whereBuild);
        whereBuild.withLogicOperator(LogicOperator.OR);
        whereBuild.bind(this.dialect);
        whereBuild.bind(this.connection);
        subBuilders.add(whereBuild);
        whereBuild.isSubSql = true;
        return this;
    }

    public WhereBuild not(WhereBuild subBuilder) {
        subBuilder.isSubSql = true;
        subBuilder.withLogicOperator(LogicOperator.NOT);
        subBuilder.bind(this.dialect);
        subBuilder.bind(this.connection);
        subBuilders.add(subBuilder);
        return this;
    }

    public WhereBuild not(Consumer<WhereBuild> subBuilder) {
        WhereBuild whereBuild = get();
        subBuilder.accept(whereBuild);
        whereBuild.isSubSql = true;
        whereBuild.withLogicOperator(LogicOperator.NOT);
        whereBuild.bind(this.dialect);
        whereBuild.bind(this.connection);
        subBuilders.add(whereBuild);
        return this;
    }


    // 清除条件
    public void clear() {
        conditions.clear();
        subBuilders.clear();
        orderBy.clear();
        groupBy.clear();
        selectFields.clear();
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
        Dialect sqlDialect = getDialect();
        Wrapper wrapper = sqlDialect.getWrapper();


        List<String> parts = new ArrayList<>();
        // 添加基本条件
        for (Condition condition : conditions) {
            condition.setToUnderLine(this.toUnderLine);
            parts.add(condition.getSqlSegment(argsList, sqlDialect));
        }
        // 添加子条件
        for (WhereBuild subBuilder : subBuilders) {
            subBuilder.setToUnderLine(this.toUnderLine);
            subBuilder.bind(sqlDialect);
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
        String join = String.join(operator, parts);

        String groupBySegment = groupBy.stream().map(e -> {
            e.setToUnderLine(this.toUnderLine);
            String column = e.getColumn();
            return wrapper.wrap(column);
        }).filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.COMMA + StringPool.SPACE));

        if (StrUtil.isNotBlank(groupBySegment)) {
            join += " GROUP BY " + groupBySegment;
        }

        String orderBySegment = orderBy.stream().map(e -> {
            e.setToUnderLine(this.toUnderLine);
            String column = e.getColumn();
            String value = Convert.toStr(e.getValue());
            return wrapper.wrap(column) + StringPool.SPACE + value;
        }).filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.COMMA + StringPool.SPACE));

        if (StrUtil.isNotBlank(orderBySegment)) {
            join += " ORDER BY " + orderBySegment;
        }
        return join;
    }

    // 静态工厂方法
    public static WhereBuild get() {
        return new WhereBuild();
    }

    public static WhereBuild get(Connection connection, Dialect dialect) {

        WhereBuild whereBuilder = new WhereBuild();
        whereBuilder.bind(connection);
        whereBuilder.bind(dialect);
        return whereBuilder;
    }

    public static WhereBuild get(Connection connection) {

        WhereBuild whereBuilder = new WhereBuild();
        whereBuilder.bind(connection);
        return whereBuilder;
    }
}