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
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.dbaccess.orm.RuntimeContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class UpdateBuild implements IUpdateBuild {

    public IUpdateBuild maybeDo(boolean condition, VoidFunc voidFunc0) {
        if (condition && voidFunc0 != null) {
            voidFunc0.call();
        }
        return instance;
    }

    @JsonIgnore
    public IUpdateBuild instance = this;

    IWhereBuild where;
    

    public UpdateBuild() {
        where = WhereBuild.get();
    }

    @Override
    public String getLast() {
        return where.getLast();
    }
    
    public IUpdateBuild set(boolean condition, String column, Object val) {
        return maybeDo(condition, () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), column) && e.getOperator() == CompareOperator.EQUAL)) {
                updateConditions.add(new Condition(column, CompareOperator.EQUAL, val));
            }
        });
    }

    // col1 = ? + ? | arg1,arg2
    public IUpdateBuild setSql(boolean condition, String setSql, Object... params) {
        return maybeDo(condition && StrUtil.isNotBlank(setSql), () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), setSql) && e.getOperator() == CompareOperator.UNKNOW)) {
                updateConditions.add(new Condition(setSql, CompareOperator.UNKNOW, params));
            }
        });
    }


    public IUpdateBuild setIncrBy(boolean condition, String column, Number val) {
        return maybeDo(condition, () -> {
            List<Condition> updateConditions = where.getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), column) && e.getOperator() == CompareOperator.INCR_BY)) {
                String s = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
                updateConditions.add(new Condition(column, CompareOperator.INCR_BY, s));
            }
        });
    }


    public IUpdateBuild setDecrBy(boolean condition, String column, Number val) {
        return maybeDo(condition, () -> {
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
        where.withLogicOperator(operator);
        return instance;
    }

    @Override
    public LogicOperator getLogicOperator() {
        return where.getLogicOperator();
    }

    @Override
    public IUpdateBuild eq(String column, Object value) {
        where.eq(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild eq(boolean option, String column, Object value) {
        where.eq(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild ne(String column, Object value) {
        where.ne(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild ne(boolean option, String column, Object value) {
        where.ne(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild gt(String column, Object value) {
        where.gt(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild gt(boolean option, String column, Object value) {
        where.gt(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild lt(String column, Object value) {
        where.lt(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild lt(boolean option, String column, Object value) {
        where.lt(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild gte(String column, Object value) {
        where.gte(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild gte(boolean option, String column, Object value) {
        where.gte(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild lte(String column, Object value) {
        where.lte(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild lte(boolean option, String column, Object value) {
        where.lte(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild like(String column, String value) {
        where.like(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild like(boolean option, String column, String value) {
        where.like(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild likeLeft(String column, String value) {
        where.likeLeft(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild likeLeft(boolean option, String column, String value) {
        where.likeLeft(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild likeRight(String column, String value) {
        where.likeRight(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild likeRight(boolean option, String column, String value) {
        where.likeRight(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild notLike(String column, String value) {
        where.notLike(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild notLike(boolean option, String column, String value) {
        where.notLike(option, column, value);
        return instance;
    }

    @Override
    public IUpdateBuild in(String column, Collection<?> values) {
        where.in(column, values);
        return instance;
    }

    @Override
    public IUpdateBuild in(boolean option, String column, Collection<?> values) {
        where.in(option, column, values);
        return instance;
    }

    @Override
    public IUpdateBuild inArray(String column, Object... values) {
        where.inArray(column, values);
        return instance;
    }

    @Override
    public IUpdateBuild inArray(boolean option, String column, Object... values) {
        where.inArray(option, column, values);
        return instance;
    }

    @Override
    public IUpdateBuild notIn(String column, Collection<?> values) {
        where.notIn(column, values);
        return instance;
    }

    @Override
    public IUpdateBuild notIn(boolean option, String column, Collection<?> values) {
        where.notIn(option, column, values);
        return instance;
    }

    @Override
    public IUpdateBuild notIn(String column, Object... values) {
        where.notIn(column, values);
        return instance;
    }

    @Override
    public IUpdateBuild notIn(boolean option, String column, Object... values) {
        where.notIn(option, column, values);
        return instance;
    }

    @Override
    public IUpdateBuild between(String column, Object value1, Object value2) {
        where.between(column, value1, value2);
        return instance;
    }

    @Override
    public IUpdateBuild between(boolean option, String column, Object value1, Object value2) {
        where.between(column, value1, value2);
        return instance;
    }

    @Override
    public IUpdateBuild isNull(String column) {
        where.isNull(column);
        return instance;
    }

    @Override
    public IUpdateBuild isNull(boolean option, String column) {
        where.isNull(column);
        return instance;
    }

    @Override
    public IUpdateBuild isNotNull(String column) {
        where.isNull(column);
        return instance;
    }

    @Override
    public IUpdateBuild isNotNull(boolean option, String column) {
        where.isNull(option, column);
        return instance;
    }

    @Override
    public IUpdateBuild sql(boolean option, String sql, Object... args_) {
        where.sql(option, sql, args_);
        return instance;
    }

    @Override
    public IUpdateBuild sql(String sql, Object... args_) {
        where.sql(sql, args_);
        return instance;
    }

    @Override
    public IUpdateBuild last(String last) {
        where.last(last);
        return instance;
    }
    

    @Override
    public IUpdateBuild select(String... columns) {
        where.select(columns);
        return instance;
    }

    @Override
    public IUpdateBuild groupBy(String... column) {
        where.groupBy(column);
        return instance;
    }

    @Override
    public IUpdateBuild asc(String... column) {
        where.asc(column);
        return instance;
    }

    @Override
    public IUpdateBuild desc(String... column) {
        where.desc(column);
        return instance;
    }

    @Override
    public IUpdateBuild having(String column, String value) {
        where.having(column, value);
        return instance;
    }

    @Override
    public IUpdateBuild and(IUpdateBuild subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        subBuilder.withLogicOperator(LogicOperator.AND);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild and(Consumer<IUpdateBuild> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        IUpdateBuild tifWhereBuild = get();
        tifWhereBuild.withLogicOperator(LogicOperator.AND);
        tifWhereBuild.setSubSql(true);
        subBuilder.accept(tifWhereBuild);
        subBuilders.add(tifWhereBuild);
        return instance;
    }

    @Override
    public IUpdateBuild and(boolean option, IUpdateBuild subBuilder) {
        if (option) return this.and(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild and(boolean option, Consumer<IUpdateBuild> subBuilder) {
        if (option) return this.and(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild or(IUpdateBuild subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        subBuilder.withLogicOperator(LogicOperator.OR);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild or(boolean option, IUpdateBuild subBuilder) {
        if (option) return this.or(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild or(Consumer<IUpdateBuild> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        IUpdateBuild tifWhereBuild = get();
        tifWhereBuild.withLogicOperator(LogicOperator.OR);
        tifWhereBuild.setSubSql(true);
        subBuilders.add(tifWhereBuild);
        return instance;
    }

    @Override
    public IUpdateBuild or(boolean option, Consumer<IUpdateBuild> subBuilder) {
        if (option) return this.or(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild not(IUpdateBuild subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        subBuilder.withLogicOperator(LogicOperator.NOT);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild not(boolean option, IUpdateBuild subBuilder) {
        if (option) return this.not(subBuilder);
        return instance;
    }

    @Override
    public IUpdateBuild not(Consumer<IUpdateBuild> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        IUpdateBuild tifWhereBuild = get();
        tifWhereBuild.withLogicOperator(LogicOperator.NOT);
        tifWhereBuild.setSubSql(true);
        subBuilder.accept(tifWhereBuild);
        subBuilders.add(tifWhereBuild);
        return instance;
    }

    @Override
    public IUpdateBuild not(boolean option, Consumer<IUpdateBuild> subBuilder) {
        if (option) return this.not(subBuilder);
        return instance;
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

    public interface VoidFunc {
        void call();
    }


}
