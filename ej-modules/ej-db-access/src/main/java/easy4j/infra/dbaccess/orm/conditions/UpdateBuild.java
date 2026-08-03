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


import cn.hutool.core.util.StrUtil;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class UpdateBuild implements IUpdateBuild {

    IWhereBuild where;


    public UpdateBuild() {
        where = WhereBuild.get();
    }

    @Override
    public String getLast() {
        return where.getLast();
    }

    public IUpdateBuild set(boolean condition, String column, Object val) {
        return optionDo(condition, () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), column) && e.getOperator() == CompareOperator.EQUAL)) {
                updateConditions.add(new Condition(column, CompareOperator.EQUAL, val));
            }
        });
    }

    // col1 = ? + ? | arg1,arg2
    public IUpdateBuild setSql(boolean condition, String setSql, Object... params) {
        return optionDo(condition && StrUtil.isNotBlank(setSql), () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), setSql) && e.getOperator() == CompareOperator.UNKNOW)) {
                updateConditions.add(new Condition(setSql, CompareOperator.UNKNOW, params));
            }
        });
    }


    public IUpdateBuild setIncrBy(boolean condition, String column, Number val) {
        return optionDo(condition, () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), column) && e.getOperator() == CompareOperator.INCR_BY)) {
                String s = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
                updateConditions.add(new Condition(column, CompareOperator.INCR_BY, s));
            }
        });
    }


    public IUpdateBuild setDecrBy(boolean condition, String column, Number val) {
        return optionDo(condition, () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), column) && e.getOperator() == CompareOperator.DECR_BY)) {
                String s = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
                updateConditions.add(new Condition(column, CompareOperator.DECR_BY, s));
            }

        });
    }

    @Override
    public Optional<IWhereBuild> getWhere() {
        return Optional.of(where);
    }

    @Override
    public Optional<IUpdateBuild> getUpdate() {
        return Optional.of(this);
    }

    @Override
    public List<Condition> getSelectFields() {
        return where.getSelectFields();
    }

    @Override
    public void setSubSql(boolean flag) {
        where.setSubSql(flag);
    }

    @Override
    public IUpdateBuild withLogicOperator(LogicOperator operator) {
        return optionDo(true, () -> where.withLogicOperator(operator));
    }

    @Override
    public LogicOperator getLogicOperator() {
        return where.getLogicOperator();
    }

    @Override
    public IUpdateBuild eq(String column, Object value) {
        return optionDo(true, () -> where.eq(column, value));
    }

    @Override
    public IUpdateBuild eq(boolean option, String column, Object value) {
        return optionDo(option, () -> where.eq(column, value));
    }

    @Override
    public IUpdateBuild ne(String column, Object value) {
        return optionDo(true, () -> where.ne(column, value));
    }

    @Override
    public IUpdateBuild ne(boolean option, String column, Object value) {
        return optionDo(option, () -> where.ne(column, value));
    }

    @Override
    public IUpdateBuild gt(String column, Object value) {
        return optionDo(true, () -> where.gt(column, value));
    }

    @Override
    public IUpdateBuild gt(boolean option, String column, Object value) {
        return optionDo(option, () -> where.gt(column, value));
    }

    @Override
    public IUpdateBuild lt(String column, Object value) {
        return optionDo(true, () -> where.lt(column, value));
    }

    @Override
    public IUpdateBuild lt(boolean option, String column, Object value) {
        return optionDo(option, () -> where.lt(column, value));
    }

    @Override
    public IUpdateBuild gte(String column, Object value) {
        return optionDo(true, () -> where.gte(column, value));
    }

    @Override
    public IUpdateBuild gte(boolean option, String column, Object value) {
        return optionDo(option, () -> where.gte(column, value));
    }

    @Override
    public IUpdateBuild lte(String column, Object value) {
        return optionDo(true, () -> where.lte(column, value));
    }

    @Override
    public IUpdateBuild lte(boolean option, String column, Object value) {
        return optionDo(option, () -> where.lte(column, value));
    }

    @Override
    public IUpdateBuild like(String column, String value) {
        return optionDo(true, () -> where.like(column, value));
    }

    @Override
    public IUpdateBuild like(boolean option, String column, String value) {
        return optionDo(option, () -> where.like(column, value));
    }

    @Override
    public IUpdateBuild likeLeft(String column, String value) {
        return optionDo(true, () -> where.likeLeft(column, value));
    }

    @Override
    public IUpdateBuild likeLeft(boolean option, String column, String value) {
        return optionDo(option, () -> where.likeLeft(column, value));
    }

    @Override
    public IUpdateBuild likeRight(String column, String value) {
        return optionDo(true, () -> where.likeRight(column, value));
    }

    @Override
    public IUpdateBuild likeRight(boolean option, String column, String value) {
        return optionDo(option, () -> where.likeRight(column, value));
    }

    @Override
    public IUpdateBuild notLike(String column, String value) {
        return optionDo(true, () -> where.notLike(column, value));
    }

    @Override
    public IUpdateBuild notLike(boolean option, String column, String value) {
        return optionDo(option, () -> where.notLike(column, value));
    }

    @Override
    public IUpdateBuild in(String column, Collection<?> values) {
        return optionDo(true, () -> where.in(column, values));
    }

    @Override
    public IUpdateBuild in(boolean option, String column, Collection<?> values) {
        return optionDo(option, () -> where.in(column, values));
    }

    @Override
    public IUpdateBuild inArray(String column, Object... values) {
        return optionDo(true, () -> where.inArray(column, values));
    }

    @Override
    public IUpdateBuild inArray(boolean option, String column, Object... values) {
        return optionDo(option, () -> where.inArray(column, values));
    }

    @Override
    public IUpdateBuild notIn(String column, Collection<?> values) {
        return optionDo(true, () -> where.notIn(column, values));
    }

    @Override
    public IUpdateBuild notIn(boolean option, String column, Collection<?> values) {
        return optionDo(option, () -> where.notIn(column, values));
    }

    @Override
    public IUpdateBuild notIn(String column, Object... values) {
        return optionDo(true, () -> where.notIn(column, values));
    }

    @Override
    public IUpdateBuild notIn(boolean option, String column, Object... values) {
        return optionDo(option, () -> where.notIn(column, values));
    }

    @Override
    public IUpdateBuild between(String column, Object value1, Object value2) {
        return optionDo(true, () -> where.between(column, value1, value2));
    }

    @Override
    public IUpdateBuild between(boolean option, String column, Object value1, Object value2) {
        return optionDo(option, () -> where.between(column, value1, value2));
    }

    @Override
    public IUpdateBuild isNull(String column) {
        return optionDo(true, () -> where.isNull(column));
    }

    @Override
    public IUpdateBuild isNull(boolean option, String column) {
        return optionDo(option, () -> where.isNull(column));
    }

    @Override
    public IUpdateBuild isNotNull(String column) {
        return optionDo(true, () -> where.isNotNull(column));
    }

    @Override
    public IUpdateBuild isNotNull(boolean option, String column) {
        return optionDo(option, () -> where.isNotNull(column));
    }

    @Override
    public IUpdateBuild sql(boolean option, String sql, Object... args_) {
        return optionDo(option && StrUtil.isNotBlank(sql), () -> where.sql(sql, args_));
    }

    @Override
    public IUpdateBuild sql(String sql, Object... args_) {
        return optionDo(true, () -> where.sql(sql, args_));
    }

    @Override
    public IUpdateBuild last(String last) {
        return optionDo(true, () -> where.last(last));
    }


    @Override
    public IUpdateBuild select(String... columns) {
        return optionDo(true, () -> where.select(columns));
    }

    @Override
    public IUpdateBuild groupBy(String... column) {
        return optionDo(true, () -> where.groupBy(column));
    }

    @Override
    public IUpdateBuild asc(String... column) {
        return optionDo(true, () -> where.asc(column));
    }

    @Override
    public IUpdateBuild desc(String... column) {
        return optionDo(true, () -> where.desc(column));
    }

    @Override
    public IUpdateBuild having(String column, String value) {
        return optionDo(true, () -> where.having(column, value));
    }

    @Override
    public IUpdateBuild last(boolean option, String last) {
        return optionDo(option, () -> this.last(last));
    }

    @Override
    public IUpdateBuild select(boolean option, String... columns) {
        return optionDo(option, () -> this.select(columns));
    }

    @Override
    public IUpdateBuild groupBy(boolean option, String... column) {
        return optionDo(option, () -> this.groupBy(column));
    }

    @Override
    public IUpdateBuild asc(boolean option, String... column) {
        return optionDo(option, () -> this.asc(column));
    }

    @Override
    public IUpdateBuild desc(boolean option, String... column) {
        return optionDo(option, () -> this.desc(column));
    }

    @Override
    public IUpdateBuild having(boolean option, String column, String value) {
        return optionDo(option, () -> this.having(column, value));
    }

    @Override
    public IUpdateBuild and(IUpdateBuild subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.AND);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IUpdateBuild and(Consumer<IUpdateBuild> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            IUpdateBuild tifWhereBuild = get();
            tifWhereBuild.withLogicOperator(LogicOperator.AND);
            tifWhereBuild.setSubSql(true);
            subBuilder.accept(tifWhereBuild);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IUpdateBuild and(boolean option, IUpdateBuild subBuilder) {
        return optionDo(option, () -> this.and(subBuilder));
    }

    @Override
    public IUpdateBuild and(boolean option, Consumer<IUpdateBuild> subBuilder) {
        return optionDo(option, () -> this.and(subBuilder));
    }

    @Override
    public IUpdateBuild or(IUpdateBuild subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.OR);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IUpdateBuild or(boolean option, IUpdateBuild subBuilder) {
        return optionDo(option, () -> this.or(subBuilder));
    }

    @Override
    public IUpdateBuild or(Consumer<IUpdateBuild> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            IUpdateBuild tifWhereBuild = get();
            tifWhereBuild.withLogicOperator(LogicOperator.OR);
            tifWhereBuild.setSubSql(true);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IUpdateBuild or(boolean option, Consumer<IUpdateBuild> subBuilder) {
        return optionDo(option, () -> this.or(subBuilder));
    }

    @Override
    public IUpdateBuild not(IUpdateBuild subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.NOT);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IUpdateBuild not(boolean option, IUpdateBuild subBuilder) {
        return optionDo(option, () -> this.not(subBuilder));
    }

    @Override
    public IUpdateBuild not(Consumer<IUpdateBuild> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            IUpdateBuild tifWhereBuild = get();
            tifWhereBuild.withLogicOperator(LogicOperator.NOT);
            tifWhereBuild.setSubSql(true);
            subBuilder.accept(tifWhereBuild);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IUpdateBuild not(boolean option, Consumer<IUpdateBuild> subBuilder) {
        return optionDo(option, () -> this.not(subBuilder));
    }

    @Override
    public void clear() {
        where.clear();
    }

    @Override
    public String buildQuery(List<Object> whereArgs, RuntimeContext<?> runtimeContext, boolean skipTail) {
        return where.buildQuery(whereArgs, runtimeContext, skipTail);
    }

    @Override
    public List<String> buildUpdate(List<Object> argList, RuntimeContext<?> context) {
        return where.buildUpdate(argList, context);
    }

    public static IUpdateBuild get() {
        return new UpdateBuild();
    }


}
