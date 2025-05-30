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

import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.lang.func.LambdaUtil;
import easy4j.module.base.utils.ListTs;

import java.sql.Connection;
import java.util.List;

/**
 * Condition
 * Tuple:
 * 0 是否有符号  true代表有 false代表没有
 * 1  or 或者 and
 * 2 参数名称
 * 3 符号
 * 4 值
 * 如果第0位是false那么第三位是没值的第二位是值
 * 如果第1位是or那么第2位会整个变成 Condition[] 数组
 *
 * @author bokun.li
 * @date 2025-05-29 23:16:46
 */
public class FCondition extends Condition {

    public static FCondition get() {
        return new FCondition();
    }

    public static FCondition get(Connection connection) {
        FCondition fCondition = new FCondition();
        fCondition.bind(connection);
        return fCondition;
    }

    public String getName(Func0<?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public FCondition set(Func0<?> func, String symbol, Object value) {
        return (FCondition) super.set(getName(func), symbol, value);
    }

    public FCondition equal(Func0<?> func, Object value) {
        return (FCondition) super.equal(getName(func), value);
    }

    public FCondition gt(Func0<?> func, Object value) {
        return (FCondition) super.gt(getName(func), value);
    }

    public FCondition gte(Func0<?> func, Object value) {
        return (FCondition) super.gte(getName(func), value);
    }

    public FCondition lt(Func0<?> func, Object value) {
        return (FCondition) super.lt(getName(func), value);
    }

    public FCondition lte(Func0<?> func, Object value) {
        return (FCondition) super.lte(getName(func), value);
    }

    public FCondition ne(Func0<?> func, Object value) {
        return (FCondition) super.ne(getName(func), value);
    }

    public FCondition ne2(Func0<?> func, Object value) {
        return (FCondition) super.ne2(getName(func), value);
    }

    public FCondition isNotNull(Func0<?> func) {
        return (FCondition) super.isNotNull(getName(func));
    }

    public FCondition isNull(Func0<?> func) {
        return (FCondition) super.isNull(getName(func));
    }

    public FCondition like(Func0<?> func, String value) {
        return (FCondition) super.like(getName(func), value);
    }

    public FCondition likeLeft(Func0<?> func, String value) {
        return (FCondition) super.likeLeft(getName(func), value);
    }

    public FCondition likeRight(Func0<?> func, String value) {
        return (FCondition) super.likeRight(getName(func), value);
    }

    /**
     * Condition.
     * or的话 传进来的都将会
     *
     * @return
     */
    public FCondition or(FCondition... condition) {
        return (FCondition) super.or(condition);
    }

    public FCondition groupBy(Func0<?>... field) {
        List<String> objects = ListTs.newArrayList();
        for (Func0<?> func0 : field) {
            String name = getName(func0);
            objects.add(name);
        }
        return (FCondition) super.groupBy(objects.toArray(new String[]{}));
    }

    public FCondition asc(Func0<?> func) {
        return (FCondition) super.asc(getName(func));
    }

    public FCondition desc(Func0<?> func) {
        return (FCondition) super.desc(getName(func));
    }

}