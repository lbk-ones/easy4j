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

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.orm.AccessUtils;
import easy4j.infra.dbaccess.orm.RuntimeContext;
import easy4j.infra.dbaccess.orm.conditions.wd.Wd;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LogicOperator
 * 条件
 *
 * @author bokun.li
 */
@Schema(description = "条件")
@Data
public class Condition {

    @Schema(description = "字段名称，驼峰和下划线都支持")
    @JsonProperty("column")
    private String column;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CompareOperator operator;


    @Schema(description = "字段值，如果是 IN,NOT IN,BETWEEN 查询那么就是集合")
    @JsonProperty("value")
    private Object value;


    @Schema(description = "自定义条件的参数值放这里")
    @JsonProperty("values")
    private Object[] values;

    public Condition() {
    }

    public Condition(String column, CompareOperator operator, Object... value) {
        this.column = column;
        this.operator = operator;
        if (operator == CompareOperator.UNKNOW) {
            this.values = value;
        } else {
            this.value = ListTs.get(value, 0);
        }
    }


    public String getSqlSegment(List<Object> argsList, RuntimeContext<?> runtimeContext) {
        AccessUtils accessUtils = runtimeContext.getAccessUtils();
        String column = accessUtils.escapeCn(accessUtils.fn(getColumn()), runtimeContext.getDialectV2(), false);
        // 给参数加前缀
        String argNamePrefix = runtimeContext.getArgNamePrefix();
        if (StrUtil.isNotBlank(argNamePrefix)) {
            if (column.indexOf(".") <= 0) {
                if (StrUtil.endWith(argNamePrefix, SP.DOT)) {
                    column = argNamePrefix + column;
                } else {
                    column = argNamePrefix + SP.DOT + column;
                }
            }
        }
        if (operator == CompareOperator.IS_NULL || operator == CompareOperator.IS_NOT_NULL) {
            return String.format("%s %s", column, operator.getSymbol());
        } else if (operator == CompareOperator.IN || operator == CompareOperator.NOT_IN) {
            if (value instanceof Collection<?> list) {
                String values = list.stream()
                        .map(v -> {
                            argsList.add(Wd.value(v));
                            return Wd.place(v);
                        })
                        .collect(Collectors.joining(SP.COMMA+SP.SPACE));
                return String.format("%s %s (%s)", column, operator.getSymbol(), values);
            } else {
                if (value instanceof CharSequence) {
                    String values = ListTs.asList(value).stream().map(v -> {
                                argsList.add(Wd.value(v));
                                return Wd.place(v);
                            })
                            .collect(Collectors.joining(", "));
                    return String.format("%s %s (%s)", column, operator.getSymbol(), values);
                }
            }
        } else if (operator == CompareOperator.BETWEEN) {
            if (value instanceof Collection<?> list && list.size() == 2) {
                Object v1 = ListTs.get(list, 0);
                Object v2 = ListTs.get(list, 1);
                argsList.add(Wd.value(v1));
                argsList.add(Wd.value(v2));
                return String.format("%s %s %s and %s", column, operator.getSymbol(), Wd.place(v1), Wd.place(v2));
            }
        } else if (operator == CompareOperator.LIKE_LEFT || operator == CompareOperator.LIKE_RIGHT) {
            operator = CompareOperator.LIKE;
        } else if (operator == CompareOperator.UNKNOW) {
            if (values != null && values.length > 0) {
                argsList.addAll(Arrays.asList(values));
            }
            // 不转义 直接返回
            return this.column;
        }else if (operator == CompareOperator.DECR_BY) {
            // 没有参数
            return String.format(operator.getSymbol(), column, column,Wd.value(value));
        }else if (operator == CompareOperator.INCR_BY) {
            // 没有参数
            return String.format(operator.getSymbol(), column, column,Wd.value(value));
        }
        argsList.add(Wd.value(value));
        return String.format("%s %s %s", column, operator.getSymbol(), Wd.place(value));
    }
}