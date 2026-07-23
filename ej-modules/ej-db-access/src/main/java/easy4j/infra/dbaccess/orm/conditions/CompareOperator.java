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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * CompareOperator
 * 比较运算法
 *
 * @author bokun.li
 */
public enum CompareOperator {
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_OR_EQUAL(">="),
    LESS_OR_EQUAL("<="),
    LIKE("like"),
    LIKE_LEFT("like left"),
    LIKE_RIGHT("like right"),
    NOT_LIKE("not like"),
    IN("in"),
    NOT_IN("not in"),
    BETWEEN("between"),
    IS_NULL("is null"),
    IS_NOT_NULL("is not null"),
    EMPTY("empty"),
    DECR_BY("%s = %s - %s"),
    INCR_BY("%s = %s + %s"),
    // 自定义sql
    UNKNOW("unknow");


    private final String symbol;

    CompareOperator(String symbol) {
        this.symbol = symbol;
    }

    @JsonValue
    public String getSymbol() {
        return symbol;
    }
}