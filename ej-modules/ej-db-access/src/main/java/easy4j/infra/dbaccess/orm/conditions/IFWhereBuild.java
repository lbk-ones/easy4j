package easy4j.infra.dbaccess.orm.conditions;

import cn.hutool.core.lang.func.Func1;

import java.util.Collection;
import java.util.function.Consumer;

public interface IFWhereBuild<T> extends IWhere {

    IFWhereBuild<T> eq(Func1<T, ?> column, Object value);

    IFWhereBuild<T> eq(boolean option, Func1<T, ?> column, Object value);

    IFWhereBuild<T> ne(Func1<T, ?> column, Object value);

    IFWhereBuild<T> ne(boolean option, Func1<T, ?> column, Object value);

    IFWhereBuild<T> gt(Func1<T, ?> column, Object value);

    IFWhereBuild<T> gt(boolean option, Func1<T, ?> column, Object value);

    IFWhereBuild<T> lt(Func1<T, ?> column, Object value);

    IFWhereBuild<T> lt(boolean option, Func1<T, ?> column, Object value);

    IFWhereBuild<T> gte(Func1<T, ?> column, Object value);

    IFWhereBuild<T> gte(boolean option, Func1<T, ?> column, Object value);

    IFWhereBuild<T> lte(Func1<T, ?> column, Object value);

    IFWhereBuild<T> lte(boolean option, Func1<T, ?> column, Object value);

    // LIKE 条件
    IFWhereBuild<T> like(Func1<T, ?> column, String value);

    IFWhereBuild<T> like(boolean option, Func1<T, ?> column, String value);

    IFWhereBuild<T> likeLeft(Func1<T, ?> column, String value);

    IFWhereBuild<T> likeLeft(boolean option, Func1<T, ?> column, String value);

    IFWhereBuild<T> likeRight(Func1<T, ?> column, String value);

    IFWhereBuild<T> likeRight(boolean option, Func1<T, ?> column, String value);

    IFWhereBuild<T> notLike(Func1<T, ?> column, String value);

    IFWhereBuild<T> notLike(boolean option, Func1<T, ?> column, String value);

    // IN 条件
    IFWhereBuild<T> in(Func1<T, ?> column, Collection<?> values);

    IFWhereBuild<T> in(boolean option, Func1<T, ?> column, Collection<?> values);

    IFWhereBuild<T> inArray(Func1<T, ?> column, Object... values);

    IFWhereBuild<T> inArray(boolean option, Func1<T, ?> column, Object... values);

    IFWhereBuild<T> notIn(Func1<T, ?> column, Collection<?> values);

    IFWhereBuild<T> notIn(boolean option, Func1<T, ?> column, Collection<?> values);

    IFWhereBuild<T> notIn(Func1<T, ?> column, Object... values);

    IFWhereBuild<T> notIn(boolean option, Func1<T, ?> column, Object... values);

    // BETWEEN 条件
    IFWhereBuild<T> between(Func1<T, ?> column, Object value1, Object value2);

    IFWhereBuild<T> between(boolean option, Func1<T, ?> column, Object value1, Object value2);

    // NULL 条件
    IFWhereBuild<T> isNull(Func1<T, ?> column);

    IFWhereBuild<T> isNull(boolean option, Func1<T, ?> column);

    IFWhereBuild<T> isNotNull(Func1<T, ?> column);

    IFWhereBuild<T> isNotNull(boolean option, Func1<T, ?> column);

    IFWhereBuild<T> sql(boolean option, String sql, Object... args_);

    IFWhereBuild<T> sql(String sql, Object... args_);

    IFWhereBuild<T> last(String last);

    IFWhereBuild<T> select(Func1<T, ?>... columns);

    IFWhereBuild<T> groupBy(Func1<T, ?>... column);

    IFWhereBuild<T> asc(Func1<T, ?>... column);

    IFWhereBuild<T> desc(Func1<T, ?>... column);

    IFWhereBuild<T> having(Func1<T, ?> column, String value);

    // 构建子条件
    IFWhereBuild<T> and(IFWhereBuild<T> subBuilder);

    IFWhereBuild<T> and(Consumer<IFWhereBuild<T>> subBuilder);

    IFWhereBuild<T> and(boolean option, IFWhereBuild<T> subBuilder);

    IFWhereBuild<T> and(boolean option, Consumer<IFWhereBuild<T>> subBuilder);

    IFWhereBuild<T> or(IFWhereBuild<T> subBuilder);

    IFWhereBuild<T> or(boolean option, IFWhereBuild<T> subBuilder);

    IFWhereBuild<T> or(Consumer<IFWhereBuild<T>> subBuilder);

    IFWhereBuild<T> or(boolean option, Consumer<IFWhereBuild<T>> subBuilder);

    IFWhereBuild<T> not(IFWhereBuild<T> subBuilder);

    IFWhereBuild<T> not(boolean option, IFWhereBuild<T> subBuilder);

    IFWhereBuild<T> not(Consumer<IFWhereBuild<T>> subBuilder);

    IFWhereBuild<T> not(boolean option, Consumer<IFWhereBuild<T>> subBuilder);


}
