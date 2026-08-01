package easy4j.infra.dbaccess.orm.conditions;

import cn.hutool.core.lang.func.Func1;

import java.util.Collection;
import java.util.function.Consumer;

public interface IFUpdateBuild<T> extends IWhere {

    IFUpdateBuild<T> set(boolean condition, Func1<T, ?> column, Object val);

    // col1 = ? + ? | arg1,arg2
    IFUpdateBuild<T> setSql(boolean condition, String setSql, Object... params);
    
    IFUpdateBuild<T> setIncrBy(boolean condition, Func1<T, ?> column, Number val);
    
    IFUpdateBuild<T> setDecrBy(boolean condition, Func1<T, ?> column, Number val);

    
    // ---------------------------------------------
    IFUpdateBuild<T> eq(Func1<T, ?> column, Object value);

    IFUpdateBuild<T> eq(boolean option, Func1<T, ?> column, Object value);

    IFUpdateBuild<T> ne(Func1<T, ?> column, Object value);

    IFUpdateBuild<T> ne(boolean option, Func1<T, ?> column, Object value);

    IFUpdateBuild<T> gt(Func1<T, ?> column, Object value);

    IFUpdateBuild<T> gt(boolean option, Func1<T, ?> column, Object value);

    IFUpdateBuild<T> lt(Func1<T, ?> column, Object value);

    IFUpdateBuild<T> lt(boolean option, Func1<T, ?> column, Object value);

    IFUpdateBuild<T> gte(Func1<T, ?> column, Object value);

    IFUpdateBuild<T> gte(boolean option, Func1<T, ?> column, Object value);

    IFUpdateBuild<T> lte(Func1<T, ?> column, Object value);

    IFUpdateBuild<T> lte(boolean option, Func1<T, ?> column, Object value);

    // LIKE 条件
    IFUpdateBuild<T> like(Func1<T, ?> column, String value);

    IFUpdateBuild<T> like(boolean option, Func1<T, ?> column, String value);

    IFUpdateBuild<T> likeLeft(Func1<T, ?> column, String value);

    IFUpdateBuild<T> likeLeft(boolean option, Func1<T, ?> column, String value);

    IFUpdateBuild<T> likeRight(Func1<T, ?> column, String value);

    IFUpdateBuild<T> likeRight(boolean option, Func1<T, ?> column, String value);

    IFUpdateBuild<T> notLike(Func1<T, ?> column, String value);

    IFUpdateBuild<T> notLike(boolean option, Func1<T, ?> column, String value);

    // IN 条件
    IFUpdateBuild<T> in(Func1<T, ?> column, Collection<?> values);

    IFUpdateBuild<T> in(boolean option, Func1<T, ?> column, Collection<?> values);

    IFUpdateBuild<T> inArray(Func1<T, ?> column, Object... values);

    IFUpdateBuild<T> inArray(boolean option, Func1<T, ?> column, Object... values);

    IFUpdateBuild<T> notIn(Func1<T, ?> column, Collection<?> values);

    IFUpdateBuild<T> notIn(boolean option, Func1<T, ?> column, Collection<?> values);

    IFUpdateBuild<T> notIn(Func1<T, ?> column, Object... values);

    IFUpdateBuild<T> notIn(boolean option, Func1<T, ?> column, Object... values);

    // BETWEEN 条件
    IFUpdateBuild<T> between(Func1<T, ?> column, Object value1, Object value2);

    IFUpdateBuild<T> between(boolean option, Func1<T, ?> column, Object value1, Object value2);

    // NULL 条件
    IFUpdateBuild<T> isNull(Func1<T, ?> column);

    IFUpdateBuild<T> isNull(boolean option, Func1<T, ?> column);

    IFUpdateBuild<T> isNotNull(Func1<T, ?> column);

    IFUpdateBuild<T> isNotNull(boolean option, Func1<T, ?> column);

    IFUpdateBuild<T> sql(boolean option, String sql, Object... args_);

    IFUpdateBuild<T> sql(String sql, Object... args_);

    IFUpdateBuild<T> last(String last);

    IFUpdateBuild<T> select(Func1<T, ?>... columns);

    IFUpdateBuild<T> groupBy(Func1<T, ?>... column);

    IFUpdateBuild<T> asc(Func1<T, ?>... column);

    IFUpdateBuild<T> desc(Func1<T, ?>... column);

    IFUpdateBuild<T> having(Func1<T, ?> column, String value);

    // 构建子条件
    IFUpdateBuild<T> and(IFUpdateBuild<T> subBuilder);

    IFUpdateBuild<T> and(boolean option, IFUpdateBuild<T> subBuilder);

    IFUpdateBuild<T> and(Consumer<IFUpdateBuild<T>>  subBuilder);

    IFUpdateBuild<T> and(boolean option, Consumer<IFUpdateBuild<T>>  subBuilder);

    IFUpdateBuild<T> or(IFUpdateBuild<T> subBuilder);

    IFUpdateBuild<T> or(boolean option, IFUpdateBuild<T> subBuilder);

    IFUpdateBuild<T> or(Consumer<IFUpdateBuild<T>>  subBuilder);

    IFUpdateBuild<T> or(boolean option, Consumer<IFUpdateBuild<T>>  subBuilder);

    IFUpdateBuild<T> not(IFUpdateBuild<T> subBuilder);

    IFUpdateBuild<T> not(boolean option, IFUpdateBuild<T> subBuilder);

    IFUpdateBuild<T> not(Consumer<IFUpdateBuild<T>>  subBuilder);

    IFUpdateBuild<T> not(boolean option, Consumer<IFUpdateBuild<T>>  subBuilder);
}
