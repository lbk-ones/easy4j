package easy4j.infra.dbaccess.orm.conditions;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface IWhereBuild extends IWhere {

    List<IWhere> getSubBuilders();

    List<Condition> getConditions();

    List<Condition> getUpdateConditions();

    WhereBuild eq(String column, Object value);

    WhereBuild eq(boolean option, String column, Object value);

    WhereBuild ne(String column, Object value);

    WhereBuild ne(boolean option, String column, Object value);

    WhereBuild gt(String column, Object value);

    WhereBuild gt(boolean option, String column, Object value);

    WhereBuild lt(String column, Object value);

    WhereBuild lt(boolean option, String column, Object value);

    WhereBuild gte(String column, Object value);

    WhereBuild gte(boolean option, String column, Object value);

    WhereBuild lte(String column, Object value);

    WhereBuild lte(boolean option, String column, Object value);

    // LIKE 条件
    WhereBuild like(String column, String value);

    WhereBuild like(boolean option, String column, String value);

    WhereBuild likeLeft(String column, String value);

    WhereBuild likeLeft(boolean option, String column, String value);

    WhereBuild likeRight(String column, String value);

    WhereBuild likeRight(boolean option, String column, String value);

    WhereBuild notLike(String column, String value);

    WhereBuild notLike(boolean option, String column, String value);

    // IN 条件
    WhereBuild in(String column, Collection<?> values);

    WhereBuild in(boolean option, String column, Collection<?> values);

    WhereBuild inArray(String column, Object... values);

    WhereBuild inArray(boolean option, String column, Object... values);

    WhereBuild notIn(String column, Collection<?> values);

    WhereBuild notIn(boolean option, String column, Collection<?> values);

    WhereBuild notIn(String column, Object... values);

    WhereBuild notIn(boolean option, String column, Object... values);

    // BETWEEN 条件
    WhereBuild between(String column, Object value1, Object value2);

    WhereBuild between(boolean option, String column, Object value1, Object value2);

    // NULL 条件
    WhereBuild isNull(String column);

    WhereBuild isNull(boolean option, String column);

    WhereBuild isNotNull(String column);

    WhereBuild isNotNull(boolean option, String column);

    WhereBuild sql(boolean option, String sql, Object... args_);

    WhereBuild sql(String sql, Object... args_);

    WhereBuild last(String last);

    WhereBuild select(String... columns);

    WhereBuild groupBy(String... column);

    WhereBuild asc(String... column);

    WhereBuild desc(String... column);

    WhereBuild having(String name, String value);

    // 构建子条件
    WhereBuild and(WhereBuild subBuilder);

    WhereBuild and(boolean option, WhereBuild subBuilder);

    WhereBuild and(Consumer<WhereBuild> subBuilder);

    WhereBuild and(boolean option, Consumer<WhereBuild> subBuilder);

    WhereBuild or(WhereBuild subBuilder);

    WhereBuild or(boolean option, WhereBuild subBuilder);

    WhereBuild or(Consumer<WhereBuild> subBuilder);

    WhereBuild or(boolean option, Consumer<WhereBuild> subBuilder);

    WhereBuild not(WhereBuild subBuilder);

    WhereBuild not(boolean option, WhereBuild subBuilder);

    WhereBuild not(Consumer<WhereBuild> subBuilder);

    WhereBuild not(boolean option, Consumer<WhereBuild> subBuilder);

}
