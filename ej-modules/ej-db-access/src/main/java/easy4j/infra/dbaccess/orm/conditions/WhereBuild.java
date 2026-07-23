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
package easy4j.infra.dbaccess.orm.conditions;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import jodd.util.StringPool;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * SQL 字符串条件构建器，支持 AND、OR、NOT 等逻辑组合，以及各种比较条件。
 * 生成预编译的sql含占位符
 * 字段名称会自动转下化线
 * <pre>
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
 * </pre>
 *
 * @author bokun.li
 */
public class WhereBuild implements Serializable {

    @Getter
    private List<Condition> conditions = new ArrayList<>();

    @Getter
    private List<Condition> updateConditions = new ArrayList<>();

    @Getter
    private final List<Condition> groupBy = new ArrayList<>();
    @Getter
    private List<Condition> orderBy = new ArrayList<>();

    @Getter
    private final List<Condition> havingList = new ArrayList<>();

    @Getter
    private final List<Condition> selectFields = new ArrayList<>();

    @Getter
    private final List<WhereBuild> subBuilders = new ArrayList<>();

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LogicOperator logicOperator = LogicOperator.AND; // 默认使用 AND 连接条件

    @Setter
    private boolean isSubSql = false;

    @Getter
    private String last;

    @Getter
    private boolean isDistinct;


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

    public void clearSelectFields() {
        this.selectFields.clear();
    }


    // 设置逻辑运算符
    public WhereBuild withLogicOperator(LogicOperator operator) {
        this.logicOperator = operator;
        return this;
    }

    // 基础比较条件方法
    public WhereBuild eq(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.EQUAL, value));
        return this;
    }

    public WhereBuild eq(boolean option, String column, Object value) {
        if (option) {
            conditions.add(new Condition(column, CompareOperator.EQUAL, value));
        }
        return this;
    }

    public WhereBuild ne(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.NOT_EQUAL, value));
        return this;
    }

    public WhereBuild ne(boolean option, String column, Object value) {
        if (option) {
            conditions.add(new Condition(column, CompareOperator.NOT_EQUAL, value));
        }
        return this;
    }

    public WhereBuild gt(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.GREATER_THAN, value));
        return this;
    }

    public WhereBuild gt(boolean option, String column, Object value) {
        if (option) {
            conditions.add(new Condition(column, CompareOperator.GREATER_THAN, value));
        }
        return this;
    }

    public WhereBuild lt(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.LESS_THAN, value));
        return this;
    }

    public WhereBuild lt(boolean option, String column, Object value) {
        if (option) {
            conditions.add(new Condition(column, CompareOperator.LESS_THAN, value));
        }
        return this;
    }

    public WhereBuild gte(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.GREATER_OR_EQUAL, value));
        return this;
    }

    public WhereBuild gte(boolean option, String column, Object value) {
        if (option) {
            conditions.add(new Condition(column, CompareOperator.GREATER_OR_EQUAL, value));
        }
        return this;
    }

    public WhereBuild lte(String column, Object value) {
        conditions.add(new Condition(column, CompareOperator.LESS_OR_EQUAL, value));
        return this;
    }

    public WhereBuild lte(boolean option, String column, Object value) {
        if (option) {
            conditions.add(new Condition(column, CompareOperator.LESS_OR_EQUAL, value));
        }
        return this;
    }

    // LIKE 条件
    public WhereBuild like(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value + "%"));
        return this;
    }

    public WhereBuild like(boolean option, String column, String value) {
        if (option) conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value + "%"));
        return this;
    }

    public WhereBuild likeLeft(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, value + "%"));
        return this;
    }

    public WhereBuild likeLeft(boolean option, String column, String value) {
        if (option) conditions.add(new Condition(column, CompareOperator.LIKE, value + "%"));
        return this;
    }

    public WhereBuild likeRight(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value));
        return this;
    }

    public WhereBuild likeRight(boolean option, String column, String value) {
        if (option) conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value));
        return this;
    }

    public WhereBuild notLike(String column, String value) {
        conditions.add(new Condition(column, CompareOperator.NOT_LIKE, "%" + value + "%"));
        return this;
    }

    public WhereBuild notLike(boolean option, String column, String value) {
        if (option) conditions.add(new Condition(column, CompareOperator.NOT_LIKE, "%" + value + "%"));
        return this;
    }

    // IN 条件
    public WhereBuild in(String column, Collection<?> values) {
        conditions.add(new Condition(column, CompareOperator.IN, values));
        return this;
    }

    public WhereBuild in(boolean option, String column, Collection<?> values) {
        if (option) conditions.add(new Condition(column, CompareOperator.IN, values));
        return this;
    }

    public WhereBuild inArray(String column, Object... values) {
        conditions.add(new Condition(column, CompareOperator.IN, Arrays.asList(values)));
        return this;
    }

    public WhereBuild inArray(boolean option, String column, Object... values) {
        if (option) conditions.add(new Condition(column, CompareOperator.IN, Arrays.asList(values)));
        return this;
    }

    public WhereBuild notIn(String column, Collection<?> values) {
        conditions.add(new Condition(column, CompareOperator.NOT_IN, values));
        return this;
    }

    public WhereBuild notIn(boolean option, String column, Collection<?> values) {
        if (option) conditions.add(new Condition(column, CompareOperator.NOT_IN, values));
        return this;
    }

    public WhereBuild notIn(String column, Object... values) {
        conditions.add(new Condition(column, CompareOperator.NOT_IN, Arrays.asList(values)));
        return this;
    }

    public WhereBuild notIn(boolean option, String column, Object... values) {
        if (option) conditions.add(new Condition(column, CompareOperator.NOT_IN, Arrays.asList(values)));
        return this;
    }

    // BETWEEN 条件
    public WhereBuild between(String column, Object value1, Object value2) {
        conditions.add(new Condition(column, CompareOperator.BETWEEN, Arrays.asList(value1, value2)));
        return this;
    }

    public WhereBuild between(boolean option, String column, Object value1, Object value2) {
        if (option) conditions.add(new Condition(column, CompareOperator.BETWEEN, Arrays.asList(value1, value2)));
        return this;
    }

    // NULL 条件
    @JsonIgnore
    public WhereBuild isNull(String column) {
        conditions.add(new Condition(column, CompareOperator.IS_NULL, null));
        return this;
    }

    @JsonIgnore
    public WhereBuild isNull(boolean option, String column) {
        if (option) conditions.add(new Condition(column, CompareOperator.IS_NULL, null));
        return this;
    }

    @JsonIgnore
    public WhereBuild isNotNull(String column) {
        conditions.add(new Condition(column, CompareOperator.IS_NOT_NULL, null));
        return this;
    }

    @JsonIgnore
    public WhereBuild isNotNull(boolean option, String column) {
        if (option) conditions.add(new Condition(column, CompareOperator.IS_NOT_NULL, null));
        return this;
    }


    public WhereBuild sql(boolean option,String sql,Object ...args_){
        if(option) {
            conditions.add(new Condition(sql, CompareOperator.UNKNOW, args_));
        }
        return this;
    }
    public WhereBuild sql(String sql,Object ...args_){
        conditions.add(new Condition(sql, CompareOperator.UNKNOW, args_));
        return this;
    }

    public WhereBuild last(String last) {
        this.last = last;
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
                return new Condition(string, CompareOperator.EMPTY, "asc");
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
                return new Condition(string, CompareOperator.EMPTY, "desc");
            });
            if (CollUtil.isNotEmpty(map)) {
                orderBy.addAll(map);
            }
        }
        return this;
    }

    public WhereBuild having(String name, String value) {
        if (!this.isSubSql && StrUtil.isNotBlank(name) && StrUtil.isNotBlank(value)) {
            havingList.add(new Condition(name, CompareOperator.EMPTY, value));
        }
        return this;
    }

    // 构建子条件
    public WhereBuild and(WhereBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.AND);
        subBuilder.isSubSql = true;
        subBuilders.add(subBuilder);
        return this;
    }

    public WhereBuild and(Consumer<WhereBuild> subBuilder) {
        WhereBuild whereBuild = get();
        subBuilder.accept(whereBuild);
        whereBuild.withLogicOperator(LogicOperator.AND);
        whereBuild.isSubSql = true;
        subBuilders.add(whereBuild);
        return this;
    }

    public WhereBuild or(WhereBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.OR);
        subBuilders.add(subBuilder);
        subBuilder.isSubSql = true;
        return this;
    }

    public WhereBuild or(Consumer<WhereBuild> subBuilder) {
        WhereBuild whereBuild = get();
        subBuilder.accept(whereBuild);
        whereBuild.withLogicOperator(LogicOperator.OR);
        subBuilders.add(whereBuild);
        whereBuild.isSubSql = true;
        return this;
    }

    public WhereBuild not(WhereBuild subBuilder) {
        subBuilder.isSubSql = true;
        subBuilder.withLogicOperator(LogicOperator.NOT);
        subBuilders.add(subBuilder);
        return this;
    }

    public WhereBuild not(Consumer<WhereBuild> subBuilder) {
        WhereBuild whereBuild = get();
        subBuilder.accept(whereBuild);
        whereBuild.isSubSql = true;
        whereBuild.withLogicOperator(LogicOperator.NOT);
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
    public String build(List<Object> whereArgs, RuntimeContext<?> runtimeContext, boolean skipTail) {

        List<String> parts = new ArrayList<>();
        // 添加基本条件
        for (Condition condition : conditions) {
            parts.add(condition.getSqlSegment(whereArgs, runtimeContext));
        }
        // 添加子条件
        for (WhereBuild subBuilder : subBuilders) {
            String subCondition = subBuilder.build(whereArgs, runtimeContext, skipTail);
            if (!subCondition.isEmpty()) {
                if (subBuilder.logicOperator == LogicOperator.NOT) {
                    parts.add("not (" + subCondition + ")");
                } else {
                    parts.add("(" + subCondition + ")");
                }
            }
        }

        // 使用逻辑运算符连接所有条件
        String operator = logicOperator == LogicOperator.AND ? " and " : " or ";
        String join = String.join(operator, parts);

        // 跳过尾部sql解析
        if (skipTail) {
            return join;
        }
        AccessUtils accessUtils = runtimeContext.getAccessUtils();
        String groupBySegment = groupBy.stream().map(e -> {
            String column = accessUtils.fn(e.getColumn());
            return accessUtils.escapeCn(column, runtimeContext.getDialectV2(), false);
        }).filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.COMMA + StringPool.SPACE));

        if (StrUtil.isNotBlank(groupBySegment)) {
            join += " group by " + groupBySegment;
        }

        if (CollUtil.isNotEmpty(havingList)) {
            StringBuilder builder = new StringBuilder();
            for (Condition condition : havingList) {
                builder.append(SP.SPACE).append(condition.getSqlSegment(whereArgs, runtimeContext));
            }
            if (!builder.toString().isEmpty()) {
                join += " having" + builder;
            }
        }

        String orderBySegment = orderBy.stream().map(e -> {
            String column = accessUtils.fn(e.getColumn());
            String value = Convert.toStr(e.getValue());
            return accessUtils.escapeCn(column, runtimeContext.getDialectV2(), false) + StringPool.SPACE + value;
        }).filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.COMMA + StringPool.SPACE));

        if (StrUtil.isNotBlank(orderBySegment)) {
            join += " order by " + orderBySegment;
        }
        return join;
    }

    public <T> List<String> parseUpdateConditions(List<Object> argList,RuntimeContext<T> context){
        List<String> updateSet = new ArrayList<>();
        for (Condition updateCondition : updateConditions) {
            String sqlSegment = updateCondition.getSqlSegment(argList, context);
            if(StrUtil.isNotBlank(sqlSegment)){
                updateSet.add(sqlSegment);
            }
        }
        return updateSet;
    }

    // 静态工厂方法
    public static WhereBuild get() {
        return new WhereBuild();
    }
}