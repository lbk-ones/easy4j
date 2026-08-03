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
import cn.hutool.core.util.StrUtil;
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
        return optionDo(true, () -> {
            where.withLogicOperator(operator);
        });
    }

    @Override
    public LogicOperator getLogicOperator() {
        return where.getLogicOperator();
    }

    @Override
    public IFWhereBuild<T> eq(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> {
            where.eq(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> eq(boolean option, Func1<T, ?> column, Object value) {
        return optionDo(option, () -> {
            where.eq(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> ne(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> {
            where.ne(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> ne(boolean option, Func1<T, ?> column, Object value) {
        return optionDo(option, () -> {
            where.ne(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> gt(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> {
            where.gt(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> gt(boolean option, Func1<T, ?> column, Object value) {
        return optionDo(option, () -> {
            where.gt(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> lt(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> {
            where.lt(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> lt(boolean option, Func1<T, ?> column, Object value) {
        return optionDo(option, () -> {
            where.lt(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> gte(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> {
            where.gte(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> gte(boolean option, Func1<T, ?> column, Object value) {
        return optionDo(option, () -> {
            where.gte(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> lte(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> {
            where.lte(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> lte(boolean option, Func1<T, ?> column, Object value) {
        return optionDo(option, () -> {
            where.lte(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> like(Func1<T, ?> column, String value) {
        return optionDo(true, () -> {
            where.like(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> like(boolean option, Func1<T, ?> column, String value) {
        return optionDo(option, () -> {
            where.like(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> likeLeft(Func1<T, ?> column, String value) {
        return optionDo(true, () -> {
            where.likeLeft(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> likeLeft(boolean option, Func1<T, ?> column, String value) {
        return optionDo(option, () -> {
            where.likeLeft(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> likeRight(Func1<T, ?> column, String value) {
        return optionDo(true, () -> {
            where.likeRight(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> likeRight(boolean option, Func1<T, ?> column, String value) {
        return optionDo(option, () -> {
            where.likeRight(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> notLike(Func1<T, ?> column, String value) {
        return optionDo(true, () -> {
            where.notLike(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> notLike(boolean option, Func1<T, ?> column, String value) {
        return optionDo(option, () -> {
            where.notLike(getName(column), value);
        });
    }

    @Override
    public IFWhereBuild<T> in(Func1<T, ?> column, Collection<?> values) {
        return optionDo(true, () -> {
            where.in(getName(column), values);
        });

    }

    @Override
    public IFWhereBuild<T> in(boolean option, Func1<T, ?> column, Collection<?> values) {
        return optionDo(option, () -> {
            where.in(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> inArray(Func1<T, ?> column, Object... values) {
        return optionDo(true, () -> {
            where.inArray(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> inArray(boolean option, Func1<T, ?> column, Object... values) {
        return optionDo(option, () -> {
            where.inArray(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> notIn(Func1<T, ?> column, Collection<?> values) {
        return optionDo(true, () -> {
            where.notIn(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> notIn(boolean option, Func1<T, ?> column, Collection<?> values) {
        return optionDo(option, () -> {
            where.notIn(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> notIn(Func1<T, ?> column, Object... values) {
        return optionDo(true, () -> {
            where.notIn(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> notIn(boolean option, Func1<T, ?> column, Object... values) {
        return optionDo(option, () -> {
            where.notIn(getName(column), values);
        });
    }

    @Override
    public IFWhereBuild<T> between(Func1<T, ?> column, Object value1, Object value2) {
        return optionDo(true, () -> {
            where.between(getName(column), value1, value2);
        });
    }

    @Override
    public IFWhereBuild<T> between(boolean option, Func1<T, ?> column, Object value1, Object value2) {
        return optionDo(option, () -> {
            where.between(getName(column), value1, value2);
        });
    }

    @Override
    public IFWhereBuild<T> isNull(Func1<T, ?> column) {
        return optionDo(true, () -> {
            where.isNull(getName(column));
        });
    }

    @Override
    public IFWhereBuild<T> isNull(boolean option, Func1<T, ?> column) {
        return optionDo(option, () -> {
            where.isNull(getName(column));
        });
    }

    @Override
    public IFWhereBuild<T> isNotNull(Func1<T, ?> column) {
        return optionDo(true, () -> {
            where.isNotNull(getName(column));
        });
    }

    @Override
    public IFWhereBuild<T> isNotNull(boolean option, Func1<T, ?> column) {
        return optionDo(option, () -> {
            where.isNotNull(getName(column));
        });
    }

    @Override
    public IFWhereBuild<T> sql(boolean option, String sql, Object... args_) {
        return optionDo(option && StrUtil.isNotBlank(sql), () -> {
            where.sql(sql, args_);
        });
    }

    @Override
    public IFWhereBuild<T> sql(String sql, Object... args_) {
        return optionDo(true, () -> {
            where.sql(sql, args_);
        });
    }

    @Override
    public IFWhereBuild<T> last(String last) {
        return optionDo(true, () -> {
            where.last(last);
        });
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
        return optionDo(true, () -> {
            where.select(names(columns));
        });
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> groupBy(Func1<T, ?>... column) {
        return optionDo(true, () -> {
            where.groupBy(names(column));
        });
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> asc(Func1<T, ?>... column) {
        return optionDo(true, () -> {
            where.asc(names(column));
        });
    }

    @SafeVarargs
    @Override
    public final IFWhereBuild<T> desc(Func1<T, ?>... column) {
        return optionDo(true, () -> {
            where.desc(names(column));
        });
    }

    @Override
    public IFWhereBuild<T> having(Func1<T, ?> name, String value) {
        return optionDo(true, () -> {
            where.having(getName(name), value);
        });
    }

    @Override
    public IFWhereBuild<T> last(boolean option, String last) {
        return optionDo(option, () -> this.last(last));
    }

    @Override
    public IFWhereBuild<T> select(boolean option, Func1<T, ?>... columns) {
        return optionDo(option, () -> this.select(columns));
    }

    @Override
    public IFWhereBuild<T> groupBy(boolean option, Func1<T, ?>... column) {
        return optionDo(option, () -> this.groupBy(column));
    }

    @Override
    public IFWhereBuild<T> asc(boolean option, Func1<T, ?>... column) {
        return optionDo(option, () -> this.asc(column));
    }

    @Override
    public IFWhereBuild<T> desc(boolean option, Func1<T, ?>... column) {
        return optionDo(option, () -> this.desc(column));
    }

    @Override
    public IFWhereBuild<T> having(boolean option, Func1<T, ?> column, String value) {
        return optionDo(option, () -> this.having(column, value));
    }

    @Override
    public IFWhereBuild<T> and(IFWhereBuild<T> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.AND);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> and(Consumer<IFWhereBuild<T>> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            IFWhereBuild<T> tifWhereBuild = get(aclass);
            tifWhereBuild.withLogicOperator(LogicOperator.AND);
            tifWhereBuild.setSubSql(true);
            subBuilder.accept(tifWhereBuild);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IFWhereBuild<T> and(boolean option, IFWhereBuild<T> subBuilder) {
        return optionDo(option, () -> {
            this.and(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> and(boolean option, Consumer<IFWhereBuild<T>> subBuilder) {
        return optionDo(option, () -> {
            this.and(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> or(IFWhereBuild<T> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.OR);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> or(boolean option, IFWhereBuild<T> subBuilder) {
        return optionDo(option, () -> {
            this.or(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> or(Consumer<IFWhereBuild<T>> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            IFWhereBuild<T> tifWhereBuild = get(aclass);
            tifWhereBuild.withLogicOperator(LogicOperator.OR);
            tifWhereBuild.setSubSql(true);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IFWhereBuild<T> or(boolean option, Consumer<IFWhereBuild<T>> subBuilder) {
        return optionDo(option, () -> {
            this.or(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> not(IFWhereBuild<T> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.NOT);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> not(boolean option, IFWhereBuild<T> subBuilder) {
        return optionDo(option, () -> {
            this.not(subBuilder);
        });
    }

    @Override
    public IFWhereBuild<T> not(Consumer<IFWhereBuild<T>> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = where.getSubBuilders();
            IFWhereBuild<T> tifWhereBuild = get(aclass);
            tifWhereBuild.withLogicOperator(LogicOperator.NOT);
            tifWhereBuild.setSubSql(true);
            subBuilder.accept(tifWhereBuild);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IFWhereBuild<T> not(boolean option, Consumer<IFWhereBuild<T>> subBuilder) {
        return optionDo(option, () -> {
            this.not(subBuilder);
        });
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