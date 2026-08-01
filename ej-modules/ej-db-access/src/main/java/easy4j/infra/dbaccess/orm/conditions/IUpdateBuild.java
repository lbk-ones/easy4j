package easy4j.infra.dbaccess.orm.conditions;


import java.util.Collection;
import java.util.function.Consumer;

public interface IUpdateBuild extends IWhere {

    IUpdateBuild set(boolean condition, String column, Object val);

    // col1 = ? + ? | arg1,arg2
    IUpdateBuild setSql(boolean condition, String setSql, Object... params);
    
    IUpdateBuild setIncrBy(boolean condition, String column, Number val);
    
    IUpdateBuild setDecrBy(boolean condition, String column, Number val);


    // ---------------------------------------------
    IUpdateBuild eq(String column, Object value);

    IUpdateBuild eq(boolean option, String column, Object value);

    IUpdateBuild ne(String column, Object value);

    IUpdateBuild ne(boolean option, String column, Object value);

    IUpdateBuild gt(String column, Object value);

    IUpdateBuild gt(boolean option, String column, Object value);

    IUpdateBuild lt(String column, Object value);

    IUpdateBuild lt(boolean option, String column, Object value);

    IUpdateBuild gte(String column, Object value);

    IUpdateBuild gte(boolean option, String column, Object value);

    IUpdateBuild lte(String column, Object value);

    IUpdateBuild lte(boolean option, String column, Object value);

    // LIKE 条件
    IUpdateBuild like(String column, String value);

    IUpdateBuild like(boolean option, String column, String value);

    IUpdateBuild likeLeft(String column, String value);

    IUpdateBuild likeLeft(boolean option, String column, String value);

    IUpdateBuild likeRight(String column, String value);

    IUpdateBuild likeRight(boolean option, String column, String value);

    IUpdateBuild notLike(String column, String value);

    IUpdateBuild notLike(boolean option, String column, String value);

    // IN 条件
    IUpdateBuild in(String column, Collection<?> values);

    IUpdateBuild in(boolean option, String column, Collection<?> values);

    IUpdateBuild inArray(String column, Object... values);

    IUpdateBuild inArray(boolean option, String column, Object... values);

    IUpdateBuild notIn(String column, Collection<?> values);

    IUpdateBuild notIn(boolean option, String column, Collection<?> values);

    IUpdateBuild notIn(String column, Object... values);

    IUpdateBuild notIn(boolean option, String column, Object... values);

    // BETWEEN 条件
    IUpdateBuild between(String column, Object value1, Object value2);

    IUpdateBuild between(boolean option, String column, Object value1, Object value2);

    // NULL 条件
    IUpdateBuild isNull(String column);

    IUpdateBuild isNull(boolean option, String column);

    IUpdateBuild isNotNull(String column);

    IUpdateBuild isNotNull(boolean option, String column);

    IUpdateBuild sql(boolean option, String sql, Object... args_);

    IUpdateBuild sql(String sql, Object... args_);

    IUpdateBuild last(String last);

    IUpdateBuild select(String... columns);

    IUpdateBuild groupBy(String... column);

    IUpdateBuild asc(String... column);

    IUpdateBuild desc(String... column);

    IUpdateBuild having(String name, String value);

    // 构建子条件
    IUpdateBuild and(IUpdateBuild subBuilder);

    IUpdateBuild and(boolean option, IUpdateBuild subBuilder);

    IUpdateBuild and(Consumer<IUpdateBuild> subBuilder);

    IUpdateBuild and(boolean option, Consumer<IUpdateBuild> subBuilder);

    IUpdateBuild or(IUpdateBuild subBuilder);

    IUpdateBuild or(boolean option, IUpdateBuild subBuilder);

    IUpdateBuild or(Consumer<IUpdateBuild> subBuilder);

    IUpdateBuild or(boolean option, Consumer<IUpdateBuild> subBuilder);

    IUpdateBuild not(IUpdateBuild subBuilder);

    IUpdateBuild not(boolean option, IUpdateBuild subBuilder);

    IUpdateBuild not(Consumer<IUpdateBuild> subBuilder);

    IUpdateBuild not(boolean option, Consumer<IUpdateBuild> subBuilder);
}
