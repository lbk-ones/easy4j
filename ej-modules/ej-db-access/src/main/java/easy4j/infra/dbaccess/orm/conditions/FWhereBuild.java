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
import lombok.Setter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * FWhereBuilder
 * Lambda条件构造器
 *
 * @author bokun.li
 * @date 2025-05-31 17:41:28
 */
public class FWhereBuild<T> extends WhereBuild {
    @Setter
    private Class<T> aclass = null;
    
    public FWhereBuild<T> instance = this;
    
    private String getName(Func1<T, ?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public FWhereBuild<T> eq(Func1<T, ?> column, Object value) {
        super.eq(getName(column), value);
        return instance;
    }

    public FWhereBuild<T> ne(Func1<T, ?> column, Object value) {
        super.ne(getName(column), value);
        return instance;
    }

    public FWhereBuild<T> gt(Func1<T, ?> column, Object value) {
        super.gt(getName(column), value);
        return instance;
    }

    public FWhereBuild<T> lt(Func1<T, ?> column, Object value) {
        super.lt(getName(column), value);
        return instance;
    }


    public FWhereBuild<T> gte(Func1<T, ?> column, Object value) {
        super.gte(getName(column), value);
        return instance;
    }


    public FWhereBuild<T> lte(Func1<T, ?> column, Object value) {
        super.lte(getName(column), value);
        return instance;
    }


    public FWhereBuild<T> like(Func1<T, ?> column, String value) {
        super.like(getName(column), value);
        return instance;
    }

    public FWhereBuild<T> likeLeft(Func1<T, ?> column, String value) {
        super.likeLeft(getName(column),value);
        return instance;
    }
    public FWhereBuild<T> likeRight(Func1<T, ?> column, String value) {
        super.likeRight(getName(column),value);
        return instance;
    }
    public FWhereBuild<T> notLike(Func1<T, ?> column, String value) {
        super.notLike(getName(column), value);
        return instance;
    }


    public <R> FWhereBuild<T> in(Func1<T, R> column, Collection<R> values) {
        super.in(getName(column), values);
        return instance;
    }


    public FWhereBuild<T> sql(boolean option,String sql,Object ...args){
        if(option) super.sql(option,sql,args);
        return instance;
    }

    public FWhereBuild<T> sql(String sql,Object ...args){
        super.sql(sql,args);
        return instance;
    }


    @SafeVarargs
    public final <R> FWhereBuild<T> inArray(Func1<T, R> column, R... values) {
        super.inArray(getName(column), (Object[]) values);
        return instance;
    }


    public FWhereBuild<T> notIn(Func1<T, ?> column, Collection<T> values) {
        super.notIn(getName(column), values);
        return instance;
    }


    public FWhereBuild<T> notIn(Func1<T, ?> column, Object... values) {
        super.notIn(getName(column), values);
        return instance;
    }


    public FWhereBuild<T> between(Func1<T, ?> column, Object value1, Object value2) {
        super.between(getName(column), value1, value2);
        return instance;
    }


    @JsonIgnore
    public FWhereBuild<T> isNull(Func1<T, ?> column) {
        super.isNull(getName(column));
        return instance;
    }

    @JsonIgnore
    public FWhereBuild<T> isNotNull(Func1<T, ?> column) {
        super.isNotNull(getName(column));
        return instance;
    }


    public FWhereBuild<T> eq(boolean option,Func1<T, ?> column, Object value) {
        super.eq(option,getName(column), value);
        return instance;
    }

    public FWhereBuild<T> ne(boolean option,Func1<T, ?> column, Object value) {
        super.ne(option,getName(column), value);
        return instance;
    }

    public FWhereBuild<T> gt(boolean option,Func1<T, ?> column, Object value) {
        super.gt(option,getName(column), value);
        return instance;
    }

    public FWhereBuild<T> lt(boolean option,Func1<T, ?> column, Object value) {
        super.lt(option,getName(column), value);
        return instance;
    }


    public FWhereBuild<T> gte(boolean option,Func1<T, ?> column, Object value) {
        super.gte(option,getName(column), value);
        return instance;
    }


    public FWhereBuild<T> lte(boolean option,Func1<T, ?> column, Object value) {
        super.lte(option,getName(column), value);
        return instance;
    }


    public FWhereBuild<T> like(boolean option,Func1<T, ?> column, String value) {
        super.like(option,getName(column), value);
        return instance;
    }

    public FWhereBuild<T> likeLeft(boolean option,Func1<T, ?> column, String value) {
        super.likeLeft(option,getName(column),value);
        return instance;
    }
    public FWhereBuild<T> likeRight(boolean option,Func1<T, ?> column, String value) {
        super.likeRight(option,getName(column),value);
        return instance;
    }
    public FWhereBuild<T> notLike(boolean option,Func1<T, ?> column, String value) {
        super.notLike(option,getName(column), value);
        return instance;
    }


    public <R> FWhereBuild<T> in(boolean option,Func1<T, R> column, Collection<R> values) {
        super.in(option,getName(column), values);
        return instance;
    }


    @SafeVarargs
    public final <R> FWhereBuild<T> inArray(boolean option,Func1<T, R> column, R... values) {
        super.inArray(option,getName(column), (Object[]) values);
        return instance;
    }


    public FWhereBuild<T> notIn(boolean option,Func1<T, ?> column, Collection<T> values) {
        super.notIn(option,getName(column), values);
        return instance;
    }


    public FWhereBuild<T> notIn(boolean option,Func1<T, ?> column, Object... values) {
        super.notIn(option,getName(column), values);
        return instance;
    }


    public FWhereBuild<T> between(boolean option,Func1<T, ?> column, Object value1, Object value2) {
        super.between(option,getName(column), value1, value2);
        return instance;
    }


    @JsonIgnore
    public FWhereBuild<T> isNull(boolean option,Func1<T, ?> column) {
        super.isNull(option,getName(column));
        return instance;
    }

    @JsonIgnore
    public FWhereBuild<T> isNotNull(boolean option,Func1<T, ?> column) {
        super.isNotNull(option,getName(column));
        return instance;
    }


    public FWhereBuild<T> last(String last) {
        super.last(last);
        return instance;
    }


    @SafeVarargs
    public final FWhereBuild<T> select(Func1<T, ?>... columns) {
        List<String> objects = ListTs.newArrayList();
        for (Func1<T, ?> column : columns) {
            String name = this.getName(column);
            objects.add(name);
        }
        super.select(objects.toArray(new String[]{}));
        return instance;
    }


    @SafeVarargs
    public final FWhereBuild<T> groupBy(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        super.groupBy(array);
        return instance;
    }

    @SafeVarargs
    public final FWhereBuild<T> asc(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        super.asc(array);
        return instance;
    }

    @SafeVarargs
    public final FWhereBuild<T> desc(Func1<T, ?>... column) {
        String[] array = (String[]) Arrays.stream(column).map(this::getName).toArray();
        super.desc(array);
        return instance;
    }


    public FWhereBuild<T> having(String name, String value) {
        super.having(name,value);
        return instance;
    }

    // 构建子条件
    public FWhereBuild<T> and(FWhereBuild<T> subBuilder) {
        super.and(subBuilder);
        return instance;
    }

    // 构建子条件
    public FWhereBuild<T> andConsumer(Consumer<FWhereBuild<T>> subBuilder) {
        FWhereBuild<T> whereBuild = get(aclass);
        subBuilder.accept(whereBuild);
        whereBuild.withLogicOperator(LogicOperator.AND);
        whereBuild.setSubSql(true);
        super.getSubBuilders().add(whereBuild);
        return this;
    }


    public FWhereBuild<T> or(FWhereBuild<T> subBuilder) {
        super.or(subBuilder);
        return instance;
    }

    @Override
    public FWhereBuild<T> withLogicOperator(LogicOperator operator) {
         super.withLogicOperator(operator);
         return instance;
    }

    public FWhereBuild<T> orConsumer(Consumer<FWhereBuild<T>> sub) {
        FWhereBuild<T> whereBuild = get(aclass);
        sub.accept(whereBuild);
        whereBuild.withLogicOperator(LogicOperator.OR);
        super.getSubBuilders().add(whereBuild);
        whereBuild.setSubSql(true);
        return instance;
    }


    public FWhereBuild<T> not(FWhereBuild<T> subBuilder) {
        super.not(subBuilder);
        return instance;
    }

    public FWhereBuild<T> notConsumer(Consumer<FWhereBuild<T>> subBuilder) {
        FWhereBuild<T> whereBuild = get(aclass);
        subBuilder.accept(whereBuild);
        whereBuild.setSubSql(true);
        whereBuild.withLogicOperator(LogicOperator.NOT);
        super.getSubBuilders().add(whereBuild);
        return this;
    }


    public static <T> FWhereBuild<T> get(Class<T> aclass) {
        FWhereBuild<T> fWhereBuild = new FWhereBuild<>();
        fWhereBuild.setAclass(aclass);
        return fWhereBuild;
    }
}