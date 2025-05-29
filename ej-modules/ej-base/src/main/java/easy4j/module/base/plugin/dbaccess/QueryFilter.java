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
package easy4j.module.base.plugin.dbaccess;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用查询过滤器
 */
@Getter
public class QueryFilter implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1838760918303682186L;
    public static final String ASC = "asc";
    public static final String DESC = "desc";

    //排序字段
    @Setter
    private String orderBy;
    //排序类型ASC/DESC
    private String order;


    public QueryFilter orderBy(String theOrderBy) {
        setOrderBy(theOrderBy);
        return this;
    }

    /**
     * 设置排序类型.
     *
     * @param order 可选值为desc或asc,多个排序字段时用','分隔.
     */
    public void setOrder(String order) {
        String lowcaseOrder = order.toLowerCase();
        //检查order字符串的合法值
        String[] orders = lowcaseOrder.split(",");
        for (String orderStr : orders) {
            if (!StrUtil.equals(DESC, orderStr) && !StrUtil.equals(ASC, orderStr)) {
                throw new IllegalArgumentException("排序类型[" + orderStr + "]不是合法值");
            }
        }
        this.order = lowcaseOrder;
    }

    public QueryFilter order(String theOrder) {
        setOrder(theOrder);
        return this;
    }

    /**
     * 是否已设置排序字段,无默认值.
     */
    public boolean isOrderBySetted() {
        return (StrUtil.isNotBlank(orderBy) && StrUtil.isNotBlank(order));
    }
}
