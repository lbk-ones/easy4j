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
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.sql.Wrapper;
import easy4j.module.base.exception.EasyException;
import easy4j.module.base.plugin.dbaccess.dialect.Dialect;
import easy4j.module.base.plugin.dbaccess.helper.JdbcHelper;
import easy4j.module.base.utils.ListTs;
import easy4j.module.base.utils.json.JacksonUtil;

import java.sql.Connection;
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
public class Condition {
    public static final String OR = "or";
    public static final String AND = "and";
    public static final String GROUP = "group by";
    public static final String ORDER_BY = "order by";
    public static final String ASC = "asc";
    public static final String DESC = "desc";
    private Connection connection;

    public static Condition get() {
        return new Condition();
    }

    public void bind(Connection connection) {
        this.connection = connection;
    }

    public List<Tuple> tuple = ListTs.newLinkedList();
    public List<Tuple> groupTuple = ListTs.newLinkedList();
    public List<Tuple> orderBy = ListTs.newLinkedList();

    public Condition set(String key, String symbol, Object value) {
        tuple.add(new Tuple(true, AND, key, symbol, value));
        return this;
    }

    public Condition equal(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, "=", value));
        return this;
    }

    public Condition gt(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, ">", value));
        return this;
    }

    public Condition gte(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, ">=", value));
        return this;
    }

    public Condition lt(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, "<", value));
        return this;
    }

    public Condition lte(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, "<=", value));
        return this;
    }

    public Condition ne(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, "<>", value));
        return this;
    }

    public Condition ne2(String key, Object value) {
        tuple.add(new Tuple(true, AND, key, "!=", value));
        return this;
    }

    public Condition isNotNull(String key) {
        tuple.add(new Tuple(false, AND, key, "is not null"));
        return this;
    }

    public Condition isNull(String key) {
        tuple.add(new Tuple(false, AND, key, "is null"));
        return this;
    }

    public Condition like(String key, String value) {
        tuple.add(new Tuple(true, AND, key, "like", "%" + value + "%"));
        return this;
    }

    public Condition likeLeft(String key, String value) {
        tuple.add(new Tuple(true, AND, key, "like", value + "%"));
        return this;
    }

    public Condition likeRight(String key, String value) {
        tuple.add(new Tuple(true, AND, key, "like", "%" + value));
        return this;
    }

    /**
     * Condition.
     * or的话 传进来的都将会
     *
     * @return
     */
    public Condition or(Condition... condition) {
        tuple.add(new Tuple(false, OR, condition));
        return this;
    }

    public Condition groupBy(String... field) {
        groupTuple.add(new Tuple(false, GROUP, field));
        return this;
    }

    public Condition asc(String name) {
        orderBy.add(new Tuple(false, ORDER_BY, name + " " + ASC));
        return this;
    }

    public Condition desc(String name) {
        orderBy.add(new Tuple(false, ORDER_BY, name + " " + DESC));
        return this;
    }

    private String getName(Tuple tuple) {
        String o = StrUtil.blankToDefault(tuple.get(2), "");
        return StrUtil.toUnderlineCase(o);
    }

    // 获取占位符
    private String getSymbol(Tuple tuple) {
        return tuple.get(3);
    }

    private String getZwf(Tuple tuple) {

        // 无符号
        if (!Convert.toBool(tuple.get(0))) {
            return null;
        } else {
            return "?";
        }
    }

    private Object getValue(Tuple tuple) {

        // 无符号 没有值
        if (!Convert.toBool(tuple.get(0))) {
            return null;
        } else {
            return tuple.get(4);
        }
    }

    public String getSqlSegment(Tuple tuple, List<Object> argsList, Wrapper wrapper) {
        String name = wrapper.wrap(StrUtil.blankToDefault(getName(tuple), ""));
        String symbol = StrUtil.blankToDefault(getSymbol(tuple), "");
        String value = StrUtil.blankToDefault(getZwf(tuple), "");
        Object value1 = getValue(tuple);
        if (Objects.nonNull(value1)) {
            argsList.add(value1);
        }
        return StrUtil.trim(name + " " + symbol + " " + value);

    }

    // 从条件中获取sql
    public String getSql(List<Object> argsList) {
        if (this.connection == null) {
            throw new EasyException("condition is not bind connection please bind a connection");
        }
        Dialect dialect = JdbcHelper.getDialect(this.connection);
        assert dialect != null;
        Wrapper wrapper = dialect.getWrapper();
        StringBuilder sb = new StringBuilder();
        for (Tuple tuple : tuple) {
            String orAnd = Convert.toStr(tuple.get(1));
            // ((x = 2 and b = 2) or (x = 2 and b = 2)) and
            if (orAnd.equals(OR)) {
                StringBuilder sbOr = new StringBuilder();
                Condition[] condition = tuple.get(2);
                for (Condition conditionItem : condition) {
                    String sql = conditionItem.getSql(argsList);
                    if (StrUtil.isNotBlank(sql)) {
                        sbOr.append("(").append(" ").append(sql).append(" ").append(")").append(" ").append(OR).append(" ");
                    }
                }
                if (StrUtil.isNotBlank(sbOr)) {
                    sbOr.delete(sbOr.length() - 3, sbOr.length());
                    sb.append("(").append(" ").append(sbOr).append(" ").append(")").append(" ").append(AND).append(" ");
                }
            } else if (orAnd.equals(AND)) {
                sb.append(getSqlSegment(tuple, argsList, wrapper)).append(" ").append(AND).append(" ");
            }
        }
        if (StrUtil.isBlank(sb)) {
            return "";
        }
        String sqlPre = sb.delete(sb.length() - 5, sb.length()).toString();
        StringBuilder builder = new StringBuilder(sqlPre);
        StringBuilder groupByStr = new StringBuilder(GROUP);
        StringBuilder orderByStr = new StringBuilder(ORDER_BY);
        if (StrUtil.isNotBlank(sqlPre)) {
            if (!groupTuple.isEmpty()) {
                for (Tuple objects : groupTuple) {
                    String[] w = objects.get(2);
                    for (String s : w) {
                        groupByStr.append(" ").append(s).append(" ").append(",");
                    }
                }
            }
            if (!orderBy.isEmpty()) {
                for (Tuple objects : orderBy) {
                    String str = Convert.toStr(objects.get(2));
                    orderByStr.append(" ").append(str).append(" ").append(",");
                }
            }
            if (groupByStr.length() > GROUP.length()) {
                groupByStr.delete(groupByStr.length() - 2, groupByStr.length());
                builder.append(" ").append(groupByStr).append(" ");
            }
            if (orderByStr.length() > ORDER_BY.length()) {
                orderByStr.delete(orderByStr.length() - 2, orderByStr.length());
                builder.append(" ").append(orderByStr).append(" ");
            }
        }
        return StrUtil.trim(builder.toString());
    }


}