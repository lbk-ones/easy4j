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
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.dbaccess.dialect.Dialect;
import easy4j.infra.dbaccess.dialect.PostgresqlDialect;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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

    public static final String PG_TYPE = "@pgconvert::pgconvert@";

    @Schema(description = "字段名称，驼峰和下划线都支持")
    private String column;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CompareOperator operator;


    @Schema(description = "字段值，如果是 IN,NOT IN,BETWEEN 查询那么就是集合")
    private Object value;

    @Schema(description = "是否转下划线，默认要转")
    public boolean toUnderLine = true;

    public void setToUnderLine(boolean toUnderLine) {
        this.toUnderLine = toUnderLine;
    }

    public String getColumn() {
        if (toUnderLine) {
            return StrUtil.isNotBlank(this.column) ? StrUtil.toUnderlineCase(this.column) : this.column;
        } else {
            return this.column;
        }
    }

    @JsonCreator
    public Condition(@JsonProperty("column") String column, @JsonProperty("operator") CompareOperator operator, @JsonProperty("value") Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }


    public String getSqlSegment(List<Object> argsList, Dialect dialect) {
        String column2 = dialect.getWrapper().wrap(getColumn());
        // only str and equal and pg
        if (value != null && value instanceof CharSequence && ((operator != null && operator != CompareOperator.EQUAL) || !(dialect instanceof PostgresqlDialect))) {
            value = StrUtil.replace(String.valueOf(value), PG_TYPE, SP.EMPTY);
        }
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
            } else {
                if (value instanceof CharSequence) {
                    String values = ListTs.asList(value).stream().map(v -> {
                                argsList.add(v);
                                return "?";
                            })
                            .collect(Collectors.joining(", "));
                    return String.format("%s %s (%s)", column2, operator.getSymbol(), values);
                }
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
        } else if (operator == CompareOperator.EQUAL) {
            try {
                if (value instanceof CharSequence && dialect instanceof PostgresqlDialect) {
                    String value1 = (String) value;
                    // PostGreSql 类型转换
                    if (StrUtil.contains(value1, PG_TYPE)) {
                        String[] split = value1.split(PG_TYPE);
                        String tempValue = split[0];
                        String s2 = split[1];
                        argsList.add(tempValue);
                        return String.format("%s %s %s", column2, operator.getSymbol(), "?::" + s2);
                    }
                }
            } catch (Exception e) {
                value = StrUtil.replace(String.valueOf(value), PG_TYPE, SP.EMPTY);
            }
        } else if (operator == CompareOperator.LIKE_LEFT || operator == CompareOperator.LIKE_RIGHT) {
            operator = CompareOperator.LIKE;
        }
        argsList.add(value);
        return String.format("%s %s %s", column2, operator.getSymbol(), "?");
    }
}