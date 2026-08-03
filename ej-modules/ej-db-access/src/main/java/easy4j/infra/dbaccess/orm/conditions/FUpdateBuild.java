package easy4j.infra.dbaccess.orm.conditions;


import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FUpdateBuild<T> implements IFUpdateBuild<T> {
    @Setter
    @JsonIgnore
    private Class<T> aclass = null;

    private String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    IUpdateBuild update;

    public FUpdateBuild() {
        update = UpdateBuild.get();
    }

    public IFUpdateBuild<T> set(boolean condition, Func1<T, ?> column, Object val) {
        return optionDo(condition, () -> {
            List<Condition> updateConditions = update.getWhere().orElseThrow().getUpdateConditions();
            String name = getName(column);
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), name) && e.getOperator() == CompareOperator.EQUAL)) {
                updateConditions.add(new Condition(name, CompareOperator.EQUAL, val));
            }
        });
    }

    // col1 = ? + ? | arg1,arg2
    public IFUpdateBuild<T> setSql(boolean condition, String setSql, Object... params) {
        return optionDo(condition && StrUtil.isNotBlank(setSql), () -> {
            List<Condition> updateConditions = update.getWhere().orElseThrow().getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), setSql) && e.getOperator() == CompareOperator.UNKNOW)) {
                updateConditions.add(new Condition(setSql, CompareOperator.UNKNOW, params));
            }
        });
    }


    public IFUpdateBuild<T> setIncrBy(boolean condition, Func1<T, ?> column, Number val) {
        return optionDo(condition, () -> {
            String name = getName(column);
            List<Condition> updateConditions = update.getWhere().orElseThrow().getUpdateConditions();
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), name) && e.getOperator() == CompareOperator.INCR_BY)) {
                String s = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
                updateConditions.add(new Condition(name, CompareOperator.INCR_BY, s));
            }
        });
    }


    public IFUpdateBuild<T> setDecrBy(boolean condition, Func1<T, ?> column, Number val) {
        return optionDo(condition, () -> {
            List<Condition> updateConditions = update.getWhere().orElseThrow().getUpdateConditions();
            String name = getName(column);
            if (updateConditions.stream().noneMatch(e -> StrUtil.equals(e.getColumn(), name) && e.getOperator() == CompareOperator.DECR_BY)) {
                String s = val instanceof BigDecimal ? ((BigDecimal) val).toPlainString() : String.valueOf(val);
                updateConditions.add(new Condition(name, CompareOperator.DECR_BY, s));
            }

        });
    }

    @Override
    public Optional<IWhereBuild> getWhere() {
        return Optional.empty();
    }

    @Override
    public Optional<IUpdateBuild> getUpdate() {
        return Optional.of(update);
    }

    @Override
    public String getLast() {
        return update.getLast();
    }

    @Override
    public List<Condition> getSelectFields() {
        return update.getSelectFields();
    }

    @Override
    public void setSubSql(boolean flag) {
        update.setSubSql(flag);
    }

    @Override
    public IFUpdateBuild<T> withLogicOperator(LogicOperator operator) {

        return optionDo(true, () -> update.withLogicOperator(operator));
    }

    @Override
    public LogicOperator getLogicOperator() {
        return update.getLogicOperator();
    }

    @Override
    public IFUpdateBuild<T> eq(Func1<T, ?> column, Object value) {
        return optionDo(true, () -> update.eq(getName(column), value));
    }

    @Override
    public IFUpdateBuild<T> eq(boolean option, Func1<T, ?> column, Object value) {

        return optionDo(option, () -> {
            update.eq(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> ne(Func1<T, ?> column, Object value) {

        return optionDo(true, () -> {
            update.ne(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> ne(boolean option, Func1<T, ?> column, Object value) {

        return optionDo(option, () -> {
            update.ne(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> gt(Func1<T, ?> column, Object value) {

        return optionDo(true, () -> {
            update.gt(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> gt(boolean option, Func1<T, ?> column, Object value) {

        return optionDo(option, () -> {
            update.gt(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> lt(Func1<T, ?> column, Object value) {

        return optionDo(true, () -> {
            update.lt(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> lt(boolean option, Func1<T, ?> column, Object value) {

        return optionDo(option, () -> {
            update.lt(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> gte(Func1<T, ?> column, Object value) {

        return optionDo(true, () -> {
            update.gte(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> gte(boolean option, Func1<T, ?> column, Object value) {

        return optionDo(option, () -> {
            update.gte(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> lte(Func1<T, ?> column, Object value) {

        return optionDo(true, () -> {
            update.lte(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> lte(boolean option, Func1<T, ?> column, Object value) {

        return optionDo(option, () -> {
            update.lte(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> like(Func1<T, ?> column, String value) {

        return optionDo(true, () -> {
            update.like(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> like(boolean option, Func1<T, ?> column, String value) {

        return optionDo(option, () -> {
            update.like(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> likeLeft(Func1<T, ?> column, String value) {

        return optionDo(true, () -> {
            update.likeLeft(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> likeLeft(boolean option, Func1<T, ?> column, String value) {

        return optionDo(option, () -> {
            update.likeLeft(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> likeRight(Func1<T, ?> column, String value) {

        return optionDo(true, () -> {
            update.likeRight(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> likeRight(boolean option, Func1<T, ?> column, String value) {

        return optionDo(option, () -> {
            update.likeRight(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> notLike(Func1<T, ?> column, String value) {

        return optionDo(true, () -> {
            update.notLike(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> notLike(boolean option, Func1<T, ?> column, String value) {

        return optionDo(option, () -> {
            update.notLike(getName(column), value);
        });
    }

    @Override
    public IFUpdateBuild<T> in(Func1<T, ?> column, Collection<?> values) {

        return optionDo(true, () -> {
            update.in(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> in(boolean option, Func1<T, ?> column, Collection<?> values) {

        return optionDo(option, () -> {
            update.in(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> inArray(Func1<T, ?> column, Object... values) {

        return optionDo(true, () -> {
            update.inArray(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> inArray(boolean option, Func1<T, ?> column, Object... values) {

        return optionDo(option, () -> {
            update.inArray(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> notIn(Func1<T, ?> column, Collection<?> values) {
        update.notIn(getName(column), values);
        return optionDo(true, () -> {
        });
    }

    @Override
    public IFUpdateBuild<T> notIn(boolean option, Func1<T, ?> column, Collection<?> values) {

        return optionDo(option, () -> {
            update.notIn(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> notIn(Func1<T, ?> column, Object... values) {

        return optionDo(true, () -> {
            update.notIn(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> notIn(boolean option, Func1<T, ?> column, Object... values) {

        return optionDo(option, () -> {
            update.notIn(getName(column), values);
        });
    }

    @Override
    public IFUpdateBuild<T> between(Func1<T, ?> column, Object value1, Object value2) {

        return optionDo(true, () -> {
            update.between(getName(column), value1, value2);
        });
    }

    @Override
    public IFUpdateBuild<T> between(boolean option, Func1<T, ?> column, Object value1, Object value2) {

        return optionDo(option, () -> {
            update.between(getName(column), value1, value2);
        });
    }

    @Override
    public IFUpdateBuild<T> isNull(Func1<T, ?> column) {

        return optionDo(true, () -> {
            update.isNull(getName(column));
        });
    }

    @Override
    public IFUpdateBuild<T> isNull(boolean option, Func1<T, ?> column) {

        return optionDo(option, () -> {
            update.isNull(getName(column));
        });
    }

    @Override
    public IFUpdateBuild<T> isNotNull(Func1<T, ?> column) {

        return optionDo(true, () -> {
            update.isNotNull(getName(column));
        });
    }

    @Override
    public IFUpdateBuild<T> isNotNull(boolean option, Func1<T, ?> column) {

        return optionDo(option, () -> {
            update.isNotNull(getName(column));
        });
    }

    @Override
    public IFUpdateBuild<T> sql(boolean option, String sql, Object... args_) {

        return optionDo(option && StrUtil.isNotBlank(sql), () -> {
            update.sql(sql, args_);
        });
    }

    @Override
    public IFUpdateBuild<T> sql(String sql, Object... args_) {

        return optionDo(true, () -> {
            update.sql(sql, args_);
        });
    }

    @Override
    public IFUpdateBuild<T> last(String last) {

        return optionDo(true, () -> {
            update.last(last);
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
    public final IFUpdateBuild<T> select(Func1<T, ?>... columns) {

        return optionDo(true, () -> {
            update.select(names(columns));
        });
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> groupBy(Func1<T, ?>... column) {

        return optionDo(true, () -> {
            update.groupBy(names(column));
        });
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> asc(Func1<T, ?>... column) {

        return optionDo(true, () -> {
            update.asc(names(column));
        });
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> desc(Func1<T, ?>... column) {

        return optionDo(true, () -> {
            update.desc(names(column));
        });
    }

    @Override
    public IFUpdateBuild<T> having(Func1<T, ?> name, String value) {

        return optionDo(true, () -> {
            update.having(getName(name), value);
        });
    }

    @Override
    public IFUpdateBuild<T> last(boolean option, String last) {
        return optionDo(option, () -> this.last(last));
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> select(boolean option, Func1<T, ?>... columns) {
        return optionDo(option, () -> this.select(columns));
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> groupBy(boolean option, Func1<T, ?>... column) {
        return optionDo(option, () -> this.groupBy(column));
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> asc(boolean option, Func1<T, ?>... column) {
        return optionDo(option, () -> this.asc(column));
    }

    @SafeVarargs
    @Override
    public final IFUpdateBuild<T> desc(boolean option, Func1<T, ?>... column) {
        return optionDo(option, () -> this.desc(column));
    }

    @Override
    public IFUpdateBuild<T> having(boolean option, Func1<T, ?> column, String value) {
        return optionDo(option, () -> this.having(column, value));
    }

    @Override
    public IFUpdateBuild<T> and(IFUpdateBuild<T> subBuilder) {

        return optionDo(true, () -> {
            List<IWhere> subBuilders = update.getWhere().orElseThrow().getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.AND);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> and(Consumer<IFUpdateBuild<T>> subBuilder) {

        return optionDo(true, () -> {
            List<IWhere> subBuilders = update.getWhere().orElseThrow().getSubBuilders();
            IFUpdateBuild<T> tifWhereBuild = get(aclass);
            tifWhereBuild.withLogicOperator(LogicOperator.AND);
            tifWhereBuild.setSubSql(true);
            subBuilder.accept(tifWhereBuild);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IFUpdateBuild<T> and(boolean option, IFUpdateBuild<T> subBuilder) {
        return optionDo(option, () -> {
            this.and(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> and(boolean option, Consumer<IFUpdateBuild<T>> subBuilder) {
        return optionDo(option, () -> {
            this.and(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> or(IFUpdateBuild<T> subBuilder) {

        return optionDo(true, () -> {
            List<IWhere> subBuilders = update.getWhere().orElseThrow().getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.OR);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> or(boolean option, IFUpdateBuild<T> subBuilder) {
        return optionDo(option, () -> {
            this.or(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> or(Consumer<IFUpdateBuild<T>> subBuilder) {

        return optionDo(true, () -> {
            List<IWhere> subBuilders = update.getWhere().orElseThrow().getSubBuilders();
            IFUpdateBuild<T> tifWhereBuild = get(aclass);
            tifWhereBuild.withLogicOperator(LogicOperator.OR);
            tifWhereBuild.setSubSql(true);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IFUpdateBuild<T> or(boolean option, Consumer<IFUpdateBuild<T>> subBuilder) {
        return optionDo(option, () -> {
            this.or(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> not(IFUpdateBuild<T> subBuilder) {

        return optionDo(true, () -> {
            List<IWhere> subBuilders = update.getWhere().orElseThrow().getSubBuilders();
            subBuilder.withLogicOperator(LogicOperator.NOT);
            subBuilder.setSubSql(true);
            subBuilders.add(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> not(boolean option, IFUpdateBuild<T> subBuilder) {
        return optionDo(option, () -> {
            this.not(subBuilder);
        });
    }

    @Override
    public IFUpdateBuild<T> not(Consumer<IFUpdateBuild<T>> subBuilder) {
        return optionDo(true, () -> {
            List<IWhere> subBuilders = update.getWhere().orElseThrow().getSubBuilders();
            IFUpdateBuild<T> tifWhereBuild = get(aclass);
            tifWhereBuild.withLogicOperator(LogicOperator.NOT);
            tifWhereBuild.setSubSql(true);
            subBuilder.accept(tifWhereBuild);
            subBuilders.add(tifWhereBuild);
        });
    }

    @Override
    public IFUpdateBuild<T> not(boolean option, Consumer<IFUpdateBuild<T>> subBuilder) {
        return optionDo(option, () -> {
            this.not(subBuilder);
        });
    }

    @Override
    public void clear() {
        update.clear();
    }

    @Override
    public String buildQuery(List<Object> updateArgs, RuntimeContext<?> runtimeContext, boolean skipTail) {
        return update.buildQuery(updateArgs, runtimeContext, skipTail);
    }

    @Override
    public List<String> buildUpdate(List<Object> argList, RuntimeContext<?> context) {
        return update.buildUpdate(argList, context);
    }

    public static <T> FUpdateBuild<T> get(Class<T> aclass) {
        FUpdateBuild<T> fWhereBuild = new FUpdateBuild<>();
        fWhereBuild.setAclass(aclass);
        return fWhereBuild;
    }


}
