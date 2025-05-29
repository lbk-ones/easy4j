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

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.lang.func.Func0;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.properties.EjSysProperties;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public static final String OR = "or";
    public static final String AND = "and";

    public static FCondition get() {
        return new FCondition();
    }

    public String getName(Func0<?> func) {
        return LambdaUtil.getFieldName(func);
    }

    public FCondition set(Func0<?> func, String symbol, Object value) {

        super.set(getName(func), symbol, value);
        return this;
    }

    public FCondition equal(Func0<?> func, Object value) {
        super.equal(getName(func), value);
        return this;
    }

    public FCondition gt(Func0<?> func, Object value) {
        super.gt(getName(func), value);
        return this;
    }

    public FCondition gte(Func0<?> func, Object value) {
        super.gte(getName(func), value);
        return this;
    }

    public FCondition lt(Func0<?> func, Object value) {
        super.lt(getName(func), value);
        return this;
    }

    public FCondition lte(Func0<?> func, Object value) {
        super.lte(getName(func), value);
        return this;
    }

    public FCondition ne(Func0<?> func, Object value) {
        super.ne(getName(func), value);
        return this;
    }

    public FCondition ne2(Func0<?> func, Object value) {
        super.ne2(getName(func), value);
        return this;
    }

    public FCondition isNotNull(Func0<?> func) {
        super.isNotNull(getName(func));
        return this;
    }

    public FCondition isNull(Func0<?> func) {
        super.isNull(getName(func));
        return this;
    }

    public FCondition like(Func0<?> func, String value) {
        super.like(getName(func), value);
        return this;
    }

    public FCondition likeLeft(Func0<?> func, String value) {
        super.likeLeft(getName(func), value);
        return this;
    }

    public FCondition likeRight(Func0<?> func, String value) {
        super.likeRight(getName(func), value);
        return this;
    }

    /**
     * Condition.
     * or的话 传进来的都将会
     *
     * @return
     */
    public FCondition or(FCondition... condition) {
        super.or(condition);
        return this;
    }

    public FCondition groupBy(Func0<?>... field) {
        List<String> objects = ListTs.newArrayList();
        for (Func0<?> func0 : field) {
            String name = getName(func0);
            objects.add(name);
        }
        super.groupBy(objects.toArray(new String[]{}));
        return this;
    }

    public FCondition asc(Func0<?> func) {
        super.asc(getName(func));
        return this;
    }

    public FCondition desc(Func0<?> func) {
        super.desc(getName(func));
        return this;
    }

}