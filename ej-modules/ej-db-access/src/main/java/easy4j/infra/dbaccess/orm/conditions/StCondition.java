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

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 标准查询条件抽象
 *
 * @param <T>
 * @param <S>
 * @author bokun.li
 * @since 2.1.5
 */
public interface StCondition<T, S> extends St {

    T eq(S column, Object value);

    T eq(boolean option, S column, Object value);

    T ne(S column, Object value);

    T ne(boolean option, S column, Object value);

    T gt(S column, Object value);

    T gt(boolean option, S column, Object value);

    T lt(S column, Object value);

    T lt(boolean option, S column, Object value);

    T gte(S column, Object value);

    T gte(boolean option, S column, Object value);

    T lte(S column, Object value);

    T lte(boolean option, S column, Object value);

    // LIKE 条件
    T like(S column, String value);

    T like(boolean option, S column, String value);

    T likeLeft(S column, String value);

    T likeLeft(boolean option, S column, String value);

    T likeRight(S column, String value);

    T likeRight(boolean option, S column, String value);

    T notLike(S column, String value);

    T notLike(boolean option, S column, String value);

    // IN 条件
    T in(S column, Collection<?> values);

    T in(boolean option, S column, Collection<?> values);

    T inArray(S column, Object... values);

    T inArray(boolean option, S column, Object... values);

    T notIn(S column, Collection<?> values);

    T notIn(boolean option, S column, Collection<?> values);

    T notIn(S column, Object... values);

    T notIn(boolean option, S column, Object... values);

    // BETWEEN 条件
    T between(S column, Object value1, Object value2);

    T between(boolean option, S column, Object value1, Object value2);

    // NULL 条件
    T isNull(S column);

    T isNull(boolean option, S column);

    T isNotNull(S column);

    T isNotNull(boolean option, S column);

    T sql(boolean option, String sql, Object... args_);

    T sql(String sql, Object... args_);

    T last(String last);

    T select(S... columns);

    T groupBy(S... column);

    T asc(S... column);

    T desc(S... column);

    T having(S column, String value);

    // 构建子条件
    T and(T subBuilder);

    T and(Consumer<T> subBuilder);

    T and(boolean option, T subBuilder);

    T and(boolean option, Consumer<T> subBuilder);

    T or(T subBuilder);

    T or(boolean option, T subBuilder);

    T or(Consumer<T> subBuilder);

    T or(boolean option, Consumer<T> subBuilder);

    T not(T subBuilder);

    T not(boolean option, T subBuilder);

    T not(Consumer<T> subBuilder);

    T not(boolean option, Consumer<T> subBuilder);

}
