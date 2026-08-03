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


public class WhereBuild implements Serializable,IWhereBuild {

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
    private final List<IWhere> subBuilders = new ArrayList<>();

    @Setter
    @Getter
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


    public boolean notExistsColumn(String column,CompareOperator compareOperator){
        if (logicOperator == LogicOperator.OR) {
            return true;
        }
        return conditions.stream().noneMatch(e->StrUtil.equals(e.getColumn(),column) && e.getOperator() == compareOperator);
    }

    @Override
    public Optional<IWhereBuild> getWhere() {
        return Optional.of(this);
    }

    @Override
    public Optional<IUpdateBuild> getUpdate() {
        return Optional.empty();
    }

    // 设置逻辑运算符
    public IWhereBuild withLogicOperator(LogicOperator operator) {
        this.logicOperator = operator;
        return this;
    }

    // 基础比较条件方法
    public IWhereBuild eq(String column, Object value) {
        if(notExistsColumn(column,CompareOperator.EQUAL)) conditions.add(new Condition(column, CompareOperator.EQUAL, value));
        return this;
    }

    public IWhereBuild eq(boolean option, String column, Object value) {
        if (option) {
            if(notExistsColumn(column,CompareOperator.EQUAL)) conditions.add(new Condition(column, CompareOperator.EQUAL, value));
        }
        return this;
    }

    public IWhereBuild ne(String column, Object value) {
        if(notExistsColumn(column,CompareOperator.NOT_EQUAL)) conditions.add(new Condition(column, CompareOperator.NOT_EQUAL, value));
        return this;
    }

    public IWhereBuild ne(boolean option, String column, Object value) {
        if (option) {
            if(notExistsColumn(column,CompareOperator.NOT_EQUAL)) conditions.add(new Condition(column, CompareOperator.NOT_EQUAL, value));
        }
        return this;
    }

    public IWhereBuild gt(String column, Object value) {
        if(notExistsColumn(column,CompareOperator.GREATER_THAN)) conditions.add(new Condition(column, CompareOperator.GREATER_THAN, value));
        return this;
    }

    public IWhereBuild gt(boolean option, String column, Object value) {
        if (option) {
            if(notExistsColumn(column,CompareOperator.GREATER_THAN)) conditions.add(new Condition(column, CompareOperator.GREATER_THAN, value));
        }
        return this;
    }

    public IWhereBuild lt(String column, Object value) {
        if(notExistsColumn(column,CompareOperator.LESS_THAN)) conditions.add(new Condition(column, CompareOperator.LESS_THAN, value));
        return this;
    }

    public IWhereBuild lt(boolean option, String column, Object value) {
        if (option) {
            if(notExistsColumn(column,CompareOperator.LESS_THAN)) conditions.add(new Condition(column, CompareOperator.LESS_THAN, value));
        }
        return this;
    }

    public IWhereBuild gte(String column, Object value) {
        if(notExistsColumn(column,CompareOperator.GREATER_OR_EQUAL)) conditions.add(new Condition(column, CompareOperator.GREATER_OR_EQUAL, value));
        return this;
    }

    public IWhereBuild gte(boolean option, String column, Object value) {
        if (option) {
            if(notExistsColumn(column,CompareOperator.GREATER_OR_EQUAL)) conditions.add(new Condition(column, CompareOperator.GREATER_OR_EQUAL, value));
        }
        return this;
    }

    public IWhereBuild lte(String column, Object value) {
        if(notExistsColumn(column,CompareOperator.LESS_OR_EQUAL)) conditions.add(new Condition(column, CompareOperator.LESS_OR_EQUAL, value));
        return this;
    }

    public IWhereBuild lte(boolean option, String column, Object value) {
        if (option) {
            if(notExistsColumn(column,CompareOperator.LESS_OR_EQUAL)) conditions.add(new Condition(column, CompareOperator.LESS_OR_EQUAL, value));
        }
        return this;
    }

    // LIKE 条件
    public IWhereBuild like(String column, String value) {
        if(notExistsColumn(column,CompareOperator.LIKE)) conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value + "%"));
        return this;
    }

    public IWhereBuild like(boolean option, String column, String value) {
        if (option && notExistsColumn(column,CompareOperator.LIKE) ) conditions.add(new Condition(column, CompareOperator.LIKE, "%" + value + "%"));
        return this;
    }

    public IWhereBuild likeLeft(String column, String value) {
        if(notExistsColumn(column,CompareOperator.LIKE_LEFT)) conditions.add(new Condition(column, CompareOperator.LIKE_LEFT, value + "%"));
        return this;
    }

    public IWhereBuild likeLeft(boolean option, String column, String value) {
        if (option && notExistsColumn(column,CompareOperator.LIKE_LEFT)) conditions.add(new Condition(column, CompareOperator.LIKE_LEFT, value + "%"));
        return this;
    }

    public IWhereBuild likeRight(String column, String value) {
        if(notExistsColumn(column,CompareOperator.LIKE_RIGHT)) conditions.add(new Condition(column, CompareOperator.LIKE_RIGHT, "%" + value));
        return this;
    }

    public IWhereBuild likeRight(boolean option, String column, String value) {
        if (option && notExistsColumn(column,CompareOperator.LIKE_RIGHT)) conditions.add(new Condition(column, CompareOperator.LIKE_RIGHT, "%" + value));
        return this;
    }

    public IWhereBuild notLike(String column, String value) {
        if(notExistsColumn(column,CompareOperator.NOT_LIKE)) conditions.add(new Condition(column, CompareOperator.NOT_LIKE, "%" + value + "%"));
        return this;
    }

    public IWhereBuild notLike(boolean option, String column, String value) {
        if (option && notExistsColumn(column,CompareOperator.NOT_LIKE)) conditions.add(new Condition(column, CompareOperator.NOT_LIKE, "%" + value + "%"));
        return this;
    }

    // IN 条件
    public IWhereBuild in(String column, Collection<?> values) {
        if(notExistsColumn(column,CompareOperator.IN)) conditions.add(new Condition(column, CompareOperator.IN, values));
        return this;
    }

    public IWhereBuild in(boolean option, String column, Collection<?> values) {
        if (option && notExistsColumn(column,CompareOperator.IN)) conditions.add(new Condition(column, CompareOperator.IN, values));
        return this;
    }

    public IWhereBuild inArray(String column, Object... values) {
        if(notExistsColumn(column,CompareOperator.IN)) conditions.add(new Condition(column, CompareOperator.IN, Arrays.asList(values)));
        return this;
    }

    public IWhereBuild inArray(boolean option, String column, Object... values) {
        if (option && notExistsColumn(column,CompareOperator.IN)) conditions.add(new Condition(column, CompareOperator.IN, Arrays.asList(values)));
        return this;
    }

    public IWhereBuild notIn(String column, Collection<?> values) {
        if(notExistsColumn(column,CompareOperator.NOT_IN)) conditions.add(new Condition(column, CompareOperator.NOT_IN, values));
        return this;
    }

    public IWhereBuild notIn(boolean option, String column, Collection<?> values) {
        if (option && notExistsColumn(column,CompareOperator.NOT_IN)) conditions.add(new Condition(column, CompareOperator.NOT_IN, values));
        return this;
    }

    public IWhereBuild notIn(String column, Object... values) {
        if(notExistsColumn(column,CompareOperator.NOT_IN)) conditions.add(new Condition(column, CompareOperator.NOT_IN, Arrays.asList(values)));
        return this;
    }

    public IWhereBuild notIn(boolean option, String column, Object... values) {
        if (option && notExistsColumn(column,CompareOperator.NOT_IN)) conditions.add(new Condition(column, CompareOperator.NOT_IN, Arrays.asList(values)));
        return this;
    }

    // BETWEEN 条件
    public IWhereBuild between(String column, Object value1, Object value2) {
        if(notExistsColumn(column,CompareOperator.BETWEEN)) conditions.add(new Condition(column, CompareOperator.BETWEEN, Arrays.asList(value1, value2)));
        return this;
    }

    public IWhereBuild between(boolean option, String column, Object value1, Object value2) {
        if (option && notExistsColumn(column,CompareOperator.BETWEEN)) conditions.add(new Condition(column, CompareOperator.BETWEEN, Arrays.asList(value1, value2)));
        return this;
    }

    // NULL 条件
    @JsonIgnore
    public IWhereBuild isNull(String column) {
        if(notExistsColumn(column,CompareOperator.IS_NULL)) conditions.add(new Condition(column, CompareOperator.IS_NULL, (Object) null));
        return this;
    }

    @JsonIgnore
    public IWhereBuild isNull(boolean option, String column) {
        if (option && notExistsColumn(column,CompareOperator.IS_NULL)) conditions.add(new Condition(column, CompareOperator.IS_NULL, (Object) null));
        return this;
    }

    @JsonIgnore
    public IWhereBuild isNotNull(String column) {
        if(notExistsColumn(column,CompareOperator.IS_NOT_NULL)) conditions.add(new Condition(column, CompareOperator.IS_NOT_NULL, (Object) null));
        return this;
    }

    @JsonIgnore
    public IWhereBuild isNotNull(boolean option, String column) {
        if (option && notExistsColumn(column,CompareOperator.IS_NOT_NULL)) conditions.add(new Condition(column, CompareOperator.IS_NOT_NULL, (Object) null));
        return this;
    }


    public IWhereBuild sql(boolean option,String sql,Object ...args_){
        if(option && StrUtil.isNotBlank(sql)) {
            if(notExistsColumn(sql,CompareOperator.UNKNOW)) conditions.add(new Condition(sql, CompareOperator.UNKNOW, args_));
        }
        return this;
    }
    public IWhereBuild sql(String sql,Object ...args_){
        if(notExistsColumn(sql,CompareOperator.UNKNOW)) conditions.add(new Condition(sql, CompareOperator.UNKNOW, args_));
        return this;
    }

    public IWhereBuild last(String last) {
        this.last = last;
        return this;
    }


    public IWhereBuild select(String... columns) {
        if (!this.isSubSql) {
            List<Condition> map = ListTs.objectToListT(columns, Condition.class, e -> {
                String string = e.toString();
                return new Condition(string, CompareOperator.EMPTY, (Object) null);
            });
            if (CollUtil.isNotEmpty(map)) {
                selectFields.addAll(map);
            }
        }
        return this;
    }



    public IWhereBuild groupBy(String... column) {
        if (!this.isSubSql) {
            List<Condition> map = ListTs.objectToListT(column, Condition.class, e -> {
                String string = e.toString();
                return new Condition(string, CompareOperator.EMPTY, (Object) null);
            });
            if (CollUtil.isNotEmpty(map)) {
                groupBy.addAll(map);
            }
        }
        return this;
    }

    public IWhereBuild asc(String... column) {
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

    public IWhereBuild desc(String... column) {
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

    public IWhereBuild having(String name, String value) {
        if (!this.isSubSql && StrUtil.isNotBlank(name) && StrUtil.isNotBlank(value)) {
            havingList.add(new Condition(name, CompareOperator.EMPTY, value));
        }
        return this;
    }

    @Override
    public IWhereBuild last(boolean option, String last) {
        return optionDo(option,()->this.last(last));
    }

    @Override
    public IWhereBuild select(boolean option, String... columns) {
        return optionDo(option,()->this.select(columns));
    }

    @Override
    public IWhereBuild groupBy(boolean option, String... column) {
        return optionDo(option,()->this.groupBy(column));
    }

    @Override
    public IWhereBuild asc(boolean option, String... column) {
        return optionDo(option,()->this.asc(column));
    }

    @Override
    public IWhereBuild desc(boolean option, String... column) {
        return optionDo(option,()->this.desc(column));
    }

    @Override
    public IWhereBuild having(boolean option, String column, String value) {
        return optionDo(option,()->this.having(column,value));
    }

    // 构建子条件
    public IWhereBuild and(IWhereBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.AND);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return this;
    }

    public IWhereBuild and(boolean option,IWhereBuild subBuilder) {
        if(option) return this.and(subBuilder);
        return this;
    }

    public IWhereBuild and(Consumer<IWhereBuild> subBuilder) {
        IWhereBuild whereBuild = get();
        whereBuild.setSubSql(true);
        whereBuild.withLogicOperator(LogicOperator.AND);
        subBuilder.accept(whereBuild);
        subBuilders.add(whereBuild);
        return this;
    }
    public IWhereBuild and(boolean option,Consumer<IWhereBuild> subBuilder) {
        if(option) return this.and(subBuilder);
        return this;
    }

    public IWhereBuild or(IWhereBuild subBuilder) {
        subBuilder.withLogicOperator(LogicOperator.OR);
        subBuilders.add(subBuilder);
        subBuilder.setSubSql(true);
        return this;
    }
    public IWhereBuild or(boolean option,IWhereBuild subBuilder) {
        if(option) return this.or(subBuilder);
        return this;
    }

    public IWhereBuild or(Consumer<IWhereBuild> subBuilder) {
        IWhereBuild whereBuild = get();
        whereBuild.withLogicOperator(LogicOperator.OR);
        whereBuild.setSubSql(true);
        subBuilder.accept(whereBuild);
        subBuilders.add(whereBuild);
        return this;
    }
    public IWhereBuild or(boolean option,Consumer<IWhereBuild> subBuilder) {
        if(option) return this.or(subBuilder);
        return this;
    }
    public IWhereBuild not(IWhereBuild subBuilder) {
        subBuilder.setSubSql(true);
        subBuilder.withLogicOperator(LogicOperator.NOT);
        subBuilders.add(subBuilder);
        return this;
    }
    public IWhereBuild not(boolean option,IWhereBuild subBuilder) {
        if(option) return this.not(subBuilder);
        return this;
    }
    public IWhereBuild not(Consumer<IWhereBuild> subBuilder) {
        IWhereBuild whereBuild = get();
        whereBuild.setSubSql(true);
        whereBuild.withLogicOperator(LogicOperator.NOT);
        subBuilder.accept(whereBuild);
        subBuilders.add(whereBuild);
        return this;
    }
    public IWhereBuild not(boolean option,Consumer<IWhereBuild> subBuilder) {
        if(option) return this.not(subBuilder);
        return this;
    }

    // 清除条件
    public void clear() {
        conditions.clear();
        subBuilders.clear();
        orderBy.clear();
        groupBy.clear();
        selectFields.clear();
        updateConditions.clear();
        withLogicOperator(LogicOperator.AND);
    }

    // 构建最终 SQL 条件
    public String buildQuery(List<Object> whereArgs, RuntimeContext<?> runtimeContext, boolean skipTail) {

        List<String> parts = new ArrayList<>();
        // 添加基本条件
        for (Condition condition : conditions) {
            parts.add(condition.getSqlSegment(whereArgs, runtimeContext));
        }
        // 添加子条件
        for (IWhere subBuilder : subBuilders) {
            String subCondition = subBuilder.buildQuery(whereArgs, runtimeContext, skipTail);
            if (!subCondition.isEmpty()) {
                if (subBuilder.getLogicOperator() == LogicOperator.NOT) {
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
            return accessUtils.sqlNameEscape(column, runtimeContext.getDialectV2(), false);
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
            return accessUtils.sqlNameEscape(column, runtimeContext.getDialectV2(), false) + StringPool.SPACE + value;
        }).filter(StrUtil::isNotBlank).collect(Collectors.joining(StringPool.COMMA + StringPool.SPACE));

        if (StrUtil.isNotBlank(orderBySegment)) {
            join += " order by " + orderBySegment;
        }
        return join;
    }

    public List<String> buildUpdate(List<Object> argList, RuntimeContext<?> context){
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
    public static IWhereBuild get() {
        return new WhereBuild();
    }
}