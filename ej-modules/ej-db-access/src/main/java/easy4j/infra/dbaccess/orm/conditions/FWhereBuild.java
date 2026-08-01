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

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * FWhereBuilder
 * Lambda条件构造器
 *
 * @author bokun.li
 * @date 2025-05-31 17:41:28
 */
public class FWhereBuild<T> implements IFWhereBuild<T> {
    @Setter
    @JsonIgnore
    private Class<T> aclass = null;

    @JsonIgnore
    public FWhereBuild<T> instance = this;

    private String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }



    IWhereBuild where;

    public FWhereBuild() {
        where = WhereBuild.get();
    }

    @Override
    public Optional<IWhereBuild> getWhere() {
        return Optional.of(where);
    }

    @Override
    public Optional<IUpdateBuild> getUpdate() {
        return Optional.empty();
    }

    @Override
    public String getLast() {
        return where.getLast();
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
    public IFWhereBuild<T> withLogicOperator(LogicOperator operator) {
        where.withLogicOperator(operator);
        return instance;
    }

    @Override
    public LogicOperator getLogicOperator() {
        return where.getLogicOperator();
    }

    @Override
    public IFWhereBuild<T> eq(Func1<T, ?> column, Object value) {
        where.eq(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> eq(boolean option, Func1<T, ?> column, Object value) {
        where.eq(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> ne(Func1<T, ?> column, Object value) {
        where.ne(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> ne(boolean option, Func1<T, ?> column, Object value) {
        where.ne(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> gt(Func1<T, ?> column, Object value) {
        where.gt(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> gt(boolean option, Func1<T, ?> column, Object value) {
        where.gt(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> lt(Func1<T, ?> column, Object value) {
        where.lt(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> lt(boolean option, Func1<T, ?> column, Object value) {
        where.lt(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> gte(Func1<T, ?> column, Object value) {
        where.gte(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> gte(boolean option, Func1<T, ?> column, Object value) {
        where.gte(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> lte(Func1<T, ?> column, Object value) {
        where.lte(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> lte(boolean option, Func1<T, ?> column, Object value) {
        where.lte(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> like(Func1<T, ?> column, String value) {
        where.like(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> like(boolean option, Func1<T, ?> column, String value) {
        where.like(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> likeLeft(Func1<T, ?> column, String value) {
        where.likeLeft(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> likeLeft(boolean option, Func1<T, ?> column, String value) {
        where.likeLeft(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> likeRight(Func1<T, ?> column, String value) {
        where.likeRight(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> likeRight(boolean option, Func1<T, ?> column, String value) {
        where.likeRight(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> notLike(Func1<T, ?> column, String value) {
        where.notLike(getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> notLike(boolean option, Func1<T, ?> column, String value) {
        where.notLike(option, getName(column), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> in(Func1<T, ?> column, Collection<?> values) {
        where.in(getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> in(boolean option, Func1<T, ?> column, Collection<?> values) {
        where.in(option, getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> inArray(Func1<T, ?> column, Object... values) {
        where.inArray(getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> inArray(boolean option, Func1<T, ?> column, Object... values) {
        where.inArray(option, getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> notIn(Func1<T, ?> column, Collection<?> values) {
        where.notIn(getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> notIn(boolean option, Func1<T, ?> column, Collection<?> values) {
        where.notIn(option, getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> notIn(Func1<T, ?> column, Object... values) {
        where.notIn(getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> notIn(boolean option, Func1<T, ?> column, Object... values) {
        where.notIn(option, getName(column), values);
        return instance;
    }

    @Override
    public IFWhereBuild<T> between(Func1<T, ?> column, Object value1, Object value2) {
        where.between(getName(column), value1, value2);
        return instance;
    }

    @Override
    public IFWhereBuild<T> between(boolean option, Func1<T, ?> column, Object value1, Object value2) {
        where.between(getName(column), value1, value2);
        return instance;
    }

    @Override
    public IFWhereBuild<T> isNull(Func1<T, ?> column) {
        where.isNull(getName(column));
        return instance;
    }

    @Override
    public IFWhereBuild<T> isNull(boolean option, Func1<T, ?> column) {
        where.isNull(getName(column));
        return instance;
    }

    @Override
    public IFWhereBuild<T> isNotNull(Func1<T, ?> column) {
        where.isNull(getName(column));
        return instance;
    }

    @Override
    public IFWhereBuild<T> isNotNull(boolean option, Func1<T, ?> column) {
        where.isNull(option, getName(column));
        return instance;
    }

    @Override
    public IFWhereBuild<T> sql(boolean option, String sql, Object... args_) {
        where.sql(option, sql, args_);
        return instance;
    }

    @Override
    public IFWhereBuild<T> sql(String sql, Object... args_) {
        where.sql(sql, args_);
        return instance;
    }

    @Override
    public IFWhereBuild<T> last(String last) {
        where.last(last);
        return instance;
    }

    @SafeVarargs
    public final String[] names(Func1<T, ?>... columns) {
        String[] names = new String[]{};
        for (Func1<T, ?> column : columns) {
            String name = getName(column);
            ArrayUtil.append(names, name);
        }
        return names;
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> select(Func1<T, ?>... columns) {
        where.select(names(columns));
        return instance;
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> groupBy(Func1<T, ?>... column) {
        where.groupBy(names(column));
        return instance;
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> asc(Func1<T, ?>... column) {
        where.asc(names(column));
        return instance;
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> desc(Func1<T, ?>... column) {
        where.desc(names(column));
        return instance;
    }

    @Override
    public IFWhereBuild<T> having(Func1<T, ?> name, String value) {
        where.having(getName(name), value);
        return instance;
    }

    @Override
    public IFWhereBuild<T> and(IFWhereBuild<T> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        subBuilder.withLogicOperator(LogicOperator.AND);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> and(Consumer<IFWhereBuild<T>> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        IFWhereBuild<T> tifWhereBuild = get(aclass);
        tifWhereBuild.withLogicOperator(LogicOperator.AND);
        tifWhereBuild.setSubSql(true);
        subBuilder.accept(tifWhereBuild);
        subBuilders.add(tifWhereBuild);
        return instance;
    }

    @Override
    public IFWhereBuild<T> and(boolean option, IFWhereBuild<T> subBuilder) {
        if (option) return this.and(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> and(boolean option, Consumer<IFWhereBuild<T>> subBuilder) {
        if (option) return this.and(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> or(IFWhereBuild<T> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        subBuilder.withLogicOperator(LogicOperator.OR);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> or(boolean option, IFWhereBuild<T> subBuilder) {
        if (option) return this.or(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> or(Consumer<IFWhereBuild<T>> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        IFWhereBuild<T> tifWhereBuild = get(aclass);
        tifWhereBuild.withLogicOperator(LogicOperator.OR);
        tifWhereBuild.setSubSql(true);
        subBuilders.add(tifWhereBuild);
        return instance;
    }

    @Override
    public IFWhereBuild<T> or(boolean option, Consumer<IFWhereBuild<T>> subBuilder) {
        if (option) return this.or(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> not(IFWhereBuild<T> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        subBuilder.withLogicOperator(LogicOperator.NOT);
        subBuilder.setSubSql(true);
        subBuilders.add(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> not(boolean option, IFWhereBuild<T> subBuilder) {
        if (option) return this.not(subBuilder);
        return instance;
    }

    @Override
    public IFWhereBuild<T> not(Consumer<IFWhereBuild<T>> subBuilder) {
        List<IWhere> subBuilders = where.getSubBuilders();
        IFWhereBuild<T> tifWhereBuild = get(aclass);
        tifWhereBuild.withLogicOperator(LogicOperator.NOT);
        tifWhereBuild.setSubSql(true);
        subBuilder.accept(tifWhereBuild);
        subBuilders.add(tifWhereBuild);
        return instance;
    }

    @Override
    public IFWhereBuild<T> not(boolean option, Consumer<IFWhereBuild<T>> subBuilder) {
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

    public static <T> IFWhereBuild<T> get(Class<T> aclass) {
        FWhereBuild<T> fWhereBuild = new FWhereBuild<>();
        fWhereBuild.setAclass(aclass);
        return fWhereBuild;
    }
}