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
package easy4j.module.base.plugin.dbaccess.condition;

import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;

import java.sql.Connection;
import java.util.List;

/**
 * FSqlBuilder
 *
 * @author bokun.li
 * @date 2025-05-31 17:41:28
 */
public class FSqlBuild<T> extends SqlBuild {

    public String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public SqlBuild equal(Func1<T, ?> column, Object value) {
        return super.equal(getName(column), value);
    }

    public SqlBuild ne(Func1<T, ?> column, Object value) {
        return super.ne(getName(column), value);
    }

    public SqlBuild gt(Func1<T, ?> column, Object value) {
        return super.gt(getName(column), value);
    }

    public SqlBuild lt(Func1<T, ?> column, Object value) {
        return super.lt(getName(column), value);
    }


    public SqlBuild gte(Func1<T, ?> column, Object value) {
        return super.gte(getName(column), value);
    }


    public SqlBuild lte(Func1<T, ?> column, Object value) {
        return super.lte(getName(column), value);
    }


    public SqlBuild like(Func1<T, ?> column, String value) {
        return super.like(getName(column), value);
    }


    public SqlBuild notLike(Func1<T, ?> column, String value) {
        return super.notLike(getName(column), value);
    }


    public SqlBuild in(Func1<T, ?> column, List<T> values) {
        return super.in(getName(column), values);
    }


    public SqlBuild in(Func1<T, ?> column, Object... values) {
        return super.in(getName(column), values);
    }


    public SqlBuild notIn(Func1<T, ?> column, List<T> values) {
        return super.notIn(getName(column), values);
    }


    public SqlBuild notIn(Func1<T, ?> column, Object... values) {
        return super.notIn(getName(column), values);
    }


    public SqlBuild between(Func1<T, ?> column, Object value1, Object value2) {
        return super.between(getName(column), value1, value2);
    }


    public SqlBuild isNull(Func1<T, ?> column) {
        return super.isNull(getName(column));
    }


    public SqlBuild isNotNull(Func1<T, ?> column) {
        return super.isNotNull(getName(column));
    }


    public static <T> FSqlBuild<T> get(Class<T> aclass) {
        return new FSqlBuild<>();
    }

    public static <T> FSqlBuild<T> get(Class<T> aclass, Connection connection, Dialect dialect) {
        FSqlBuild<T> fSqlBuilder = new FSqlBuild<>();
        fSqlBuilder.bind(connection);
        fSqlBuilder.bind(dialect);
        return fSqlBuilder;
    }

    public static <T> FSqlBuild<T> get(Class<T> aclass, Connection connection) {
        FSqlBuild<T> fSqlBuilder = new FSqlBuild<>();
        fSqlBuilder.bind(connection);
        return fSqlBuilder;
    }
}