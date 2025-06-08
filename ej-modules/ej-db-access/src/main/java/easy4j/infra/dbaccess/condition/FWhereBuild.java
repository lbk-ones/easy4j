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
package easy4j.infra.dbaccess.condition;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import easy4j.infra.dbaccess.dialect.Dialect;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

/**
 * FWhereBuilder
 * Lambda条件构造器
 *
 * @author bokun.li
 * @date 2025-05-31 17:41:28
 */
public class FWhereBuild<T> extends WhereBuild {

    public String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public WhereBuild equal(Func1<T, ?> column, Object value) {
        return super.equal(getName(column), value);
    }

    public WhereBuild ne(Func1<T, ?> column, Object value) {
        return super.ne(getName(column), value);
    }

    public WhereBuild gt(Func1<T, ?> column, Object value) {
        return super.gt(getName(column), value);
    }

    public WhereBuild lt(Func1<T, ?> column, Object value) {
        return super.lt(getName(column), value);
    }


    public WhereBuild gte(Func1<T, ?> column, Object value) {
        return super.gte(getName(column), value);
    }


    public WhereBuild lte(Func1<T, ?> column, Object value) {
        return super.lte(getName(column), value);
    }


    public WhereBuild like(Func1<T, ?> column, String value) {
        return super.like(getName(column), value);
    }


    public WhereBuild notLike(Func1<T, ?> column, String value) {
        return super.notLike(getName(column), value);
    }


    public <R> WhereBuild in(Func1<T, R> column, List<R> values) {
        return super.in(getName(column), values);
    }


    @SafeVarargs
    public final <R> WhereBuild inArray(Func1<T, R> column, R... values) {
        return super.inArray(getName(column), values);
    }


    public WhereBuild notIn(Func1<T, ?> column, List<T> values) {
        return super.notIn(getName(column), values);
    }


    public WhereBuild notIn(Func1<T, ?> column, Object... values) {
        return super.notIn(getName(column), values);
    }


    public WhereBuild between(Func1<T, ?> column, Object value1, Object value2) {
        return super.between(getName(column), value1, value2);
    }


    public WhereBuild isNull(Func1<T, ?> column) {
        return super.isNull(getName(column));
    }


    public WhereBuild isNotNull(Func1<T, ?> column) {
        return super.isNotNull(getName(column));
    }


    @SafeVarargs
    public final WhereBuild select(Func1<T, ?>... columns) {
        String[] array = (String[]) Arrays.stream(columns).map(this::getName).toArray();
        return super.select(array);
    }


    @SafeVarargs
    public final WhereBuild groupBy(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        return super.groupBy(array);
    }

    @SafeVarargs
    public final WhereBuild asc(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        return super.asc(array);
    }

    @SafeVarargs
    public final WhereBuild desc(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        return super.desc(array);
    }


    public static <T> FWhereBuild<T> get(Class<T> aclass) {
        return new FWhereBuild<>();
    }

    public static <T> FWhereBuild<T> get(Class<T> aclass, Connection connection, Dialect dialect) {
        FWhereBuild<T> fSqlBuilder = new FWhereBuild<>();
        fSqlBuilder.bind(connection);
        fSqlBuilder.bind(dialect);
        return fSqlBuilder;
    }

    public static <T> FWhereBuild<T> get(Class<T> aclass, Connection connection) {
        FWhereBuild<T> fSqlBuilder = new FWhereBuild<>();
        fSqlBuilder.bind(connection);
        return fSqlBuilder;
    }
}