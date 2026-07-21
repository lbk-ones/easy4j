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

import jodd.util.StringPool;

/**
 * 逻辑运算符
 * LogicOperator
 *
 * @author bokun.li
 * @deprecated 自版本 2.1.4 起过期，请使用 {@link easy4j.infra.dbaccess.orm.conditions.LogicOperator} 替代
 */
@Deprecated
public enum LogicOperator {
    AND, OR, NOT;

    public static String get(LogicOperator logicOperator) {
        return StringPool.SPACE + logicOperator.name() + StringPool.SPACE;
    }
}