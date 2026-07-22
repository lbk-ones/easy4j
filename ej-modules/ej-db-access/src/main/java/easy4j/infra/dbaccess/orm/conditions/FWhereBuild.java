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
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.utils.ListTs;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * FWhereBuilder
 * Lambda条件构造器
 *
 * @author bokun.li
 * @date 2025-05-31 17:41:28
 */
public class FWhereBuild<T> extends WhereBuild {

    private String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public FWhereBuild<T> eq(Func1<T, ?> column, Object value) {
        super.eq(getName(column), value);
        return this;
    }

    public FWhereBuild<T> ne(Func1<T, ?> column, Object value) {
        super.ne(getName(column), value);
        return this;
    }

    public FWhereBuild<T> gt(Func1<T, ?> column, Object value) {
        super.gt(getName(column), value);
        return this;
    }

    public FWhereBuild<T> lt(Func1<T, ?> column, Object value) {
        super.lt(getName(column), value);
        return this;
    }


    public FWhereBuild<T> gte(Func1<T, ?> column, Object value) {
        super.gte(getName(column), value);
        return this;
    }


    public FWhereBuild<T> lte(Func1<T, ?> column, Object value) {
        super.lte(getName(column), value);
        return this;
    }


    public FWhereBuild<T> like(Func1<T, ?> column, String value) {
        super.like(getName(column), value);
        return this;
    }

    public FWhereBuild<T> likeLeft(Func1<T, ?> column, String value) {
        super.likeLeft(getName(column),value);
        return this;
    }
    public FWhereBuild<T> likeRight(Func1<T, ?> column, String value) {
        super.likeRight(getName(column),value);
        return this;
    }
    public FWhereBuild<T> notLike(Func1<T, ?> column, String value) {
        super.notLike(getName(column), value);
        return this;
    }


    public <R> FWhereBuild<T> in(Func1<T, R> column, Collection<R> values) {
        super.in(getName(column), values);
        return this;
    }


    public FWhereBuild<T> sql(boolean option,String sql,Object ...args){
        if(option) super.sql(option,sql,args);
        return this;
    }

    public FWhereBuild<T> sql(String sql,Object ...args){
        super.sql(sql,args);
        return this;
    }


    @SafeVarargs
    public final <R> FWhereBuild<T> inArray(Func1<T, R> column, R... values) {
        super.inArray(getName(column), (Object[]) values);
        return this;
    }


    public FWhereBuild<T> notIn(Func1<T, ?> column, Collection<T> values) {
        super.notIn(getName(column), values);
        return this;
    }


    public FWhereBuild<T> notIn(Func1<T, ?> column, Object... values) {
        super.notIn(getName(column), values);
        return this;
    }


    public FWhereBuild<T> between(Func1<T, ?> column, Object value1, Object value2) {
        super.between(getName(column), value1, value2);
        return this;
    }


    @JsonIgnore
    public FWhereBuild<T> isNull(Func1<T, ?> column) {
        super.isNull(getName(column));
        return this;
    }

    @JsonIgnore
    public FWhereBuild<T> isNotNull(Func1<T, ?> column) {
        super.isNotNull(getName(column));
        return this;
    }


    public FWhereBuild<T> eq(boolean option,Func1<T, ?> column, Object value) {
        super.eq(option,getName(column), value);
        return this;
    }

    public FWhereBuild<T> ne(boolean option,Func1<T, ?> column, Object value) {
        super.ne(option,getName(column), value);
        return this;
    }

    public FWhereBuild<T> gt(boolean option,Func1<T, ?> column, Object value) {
        super.gt(option,getName(column), value);
        return this;
    }

    public FWhereBuild<T> lt(boolean option,Func1<T, ?> column, Object value) {
        super.lt(option,getName(column), value);
        return this;
    }


    public FWhereBuild<T> gte(boolean option,Func1<T, ?> column, Object value) {
        super.gte(option,getName(column), value);
        return this;
    }


    public FWhereBuild<T> lte(boolean option,Func1<T, ?> column, Object value) {
        super.lte(option,getName(column), value);
        return this;
    }


    public FWhereBuild<T> like(boolean option,Func1<T, ?> column, String value) {
        super.like(option,getName(column), value);
        return this;
    }

    public FWhereBuild<T> likeLeft(boolean option,Func1<T, ?> column, String value) {
        super.likeLeft(option,getName(column),value);
        return this;
    }
    public FWhereBuild<T> likeRight(boolean option,Func1<T, ?> column, String value) {
        super.likeRight(option,getName(column),value);
        return this;
    }
    public FWhereBuild<T> notLike(boolean option,Func1<T, ?> column, String value) {
        super.notLike(option,getName(column), value);
        return this;
    }


    public <R> FWhereBuild<T> in(boolean option,Func1<T, R> column, Collection<R> values) {
        super.in(option,getName(column), values);
        return this;
    }


    @SafeVarargs
    public final <R> FWhereBuild<T> inArray(boolean option,Func1<T, R> column, R... values) {
        super.inArray(option,getName(column), (Object[]) values);
        return this;
    }


    public FWhereBuild<T> notIn(boolean option,Func1<T, ?> column, Collection<T> values) {
        super.notIn(option,getName(column), values);
        return this;
    }


    public FWhereBuild<T> notIn(boolean option,Func1<T, ?> column, Object... values) {
        super.notIn(option,getName(column), values);
        return this;
    }


    public FWhereBuild<T> between(boolean option,Func1<T, ?> column, Object value1, Object value2) {
        super.between(option,getName(column), value1, value2);
        return this;
    }


    @JsonIgnore
    public FWhereBuild<T> isNull(boolean option,Func1<T, ?> column) {
        super.isNull(option,getName(column));
        return this;
    }

    @JsonIgnore
    public FWhereBuild<T> isNotNull(boolean option,Func1<T, ?> column) {
        super.isNotNull(option,getName(column));
        return this;
    }


    public FWhereBuild<T> last(String last) {
        super.last(last);
        return this;
    }


    @SafeVarargs
    public final FWhereBuild<T> select(Func1<T, ?>... columns) {
        List<String> objects = ListTs.newArrayList();
        for (Func1<T, ?> column : columns) {
            String name = this.getName(column);
            objects.add(name);
        }
        super.select(objects.toArray(new String[]{}));
        return this;
    }


    @SafeVarargs
    public final FWhereBuild<T> groupBy(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        super.groupBy(array);
        return this;
    }

    @SafeVarargs
    public final FWhereBuild<T> asc(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        super.asc(array);
        return this;
    }

    @SafeVarargs
    public final FWhereBuild<T> desc(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        super.desc(array);
        return this;
    }


    public FWhereBuild<T> having(String name, String value) {
        super.having(name,value);
        return this;
    }

    // 构建子条件
    public FWhereBuild<T> and(FWhereBuild<T> subBuilder) {
        super.and(subBuilder);
        return this;
    }


    public FWhereBuild<T> or(FWhereBuild<T> subBuilder) {
        super.or(subBuilder);
        return this;
    }


    public FWhereBuild<T> not(FWhereBuild<T> subBuilder) {
        super.not(subBuilder);
        return this;
    }


    public static <T> FWhereBuild<T> get(Class<T> aclass) {
        return new FWhereBuild<>();
    }
}