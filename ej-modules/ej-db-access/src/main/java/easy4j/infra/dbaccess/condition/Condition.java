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

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.*;
import easy4j.infra.dbaccess.dialect.Dialect;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LogicOperator
 * 条件
 *
 * @author bokun.li
 */

public class Condition {
    private String column;
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private final CompareOperator operator;
    @Getter
    private final Object value;

    public boolean toUnderLine = true;

    public void setToUnderLine(boolean toUnderLine) {
        this.toUnderLine = toUnderLine;
    }

    public String getColumn() {
        if(toUnderLine){
            return StrUtil.isNotBlank(this.column) ? StrUtil.toUnderlineCase(this.column) : this.column;
        }else{
            return this.column;
        }
    }

    @JsonCreator
    public Condition(@JsonProperty("column") String column, @JsonProperty("operator")  CompareOperator operator, @JsonProperty("value")  Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }


    public String getSqlSegment(List<Object> argsList, Dialect dialect) {
       String column2 = dialect.getWrapper().wrap(getColumn());
        if (operator == CompareOperator.IS_NULL || operator == CompareOperator.IS_NOT_NULL) {
            return String.format("%s %s", column2, operator.getSymbol());
        } else if (operator == CompareOperator.IN || operator == CompareOperator.NOT_IN) {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                String values = list.stream()
                        .map(v -> {
                            argsList.add(v);
                            return "?";
                        })
                        .collect(Collectors.joining(", "));
                return String.format("%s %s (%s)", column2, operator.getSymbol(), values);
            }
        } else if (operator == CompareOperator.BETWEEN) {
            if (value instanceof List && ((List<?>) value).size() == 2) {
                List<?> list = (List<?>) value;
                Object v1 = list.get(0);
                Object v2 = list.get(1);
                argsList.add(v1);
                argsList.add(v2);
                return String.format("%s %s %s AND %s", column2, operator.getSymbol(), "?", "?");
            }
        }
        argsList.add(value);
        return String.format("%s %s %s", column2, operator.getSymbol(), "?");
    }
}