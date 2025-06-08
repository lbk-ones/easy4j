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
package easy4j.infra.dbaccess;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * 与具体DBAccess实现无关的分页参数及查询结果封装.
 *
 * @param <T> Page中对象的类型.
 * @author yuqs
 * @since 1.0
 */
@ToString
public class Page<T> implements Serializable {
    public static final int NON_PAGE = -1;
    public static final int PAGE_SIZE = 15;

    /**
     * -- GETTER --
     * 获得当前页的页号,默认为1.
     */
    //当前页
    @Getter
    private int pageNo = 1;
    /**
     * -- SETTER --
     * 设置每页的记录数.
     * -- GETTER --
     * 获得每页记录数.
     */
    //每页记录数
    @Getter
    @Setter
    private int pageSize = -1;
    /**
     * -- SETTER --
     * 设置总记录数.
     */
    //总记录数
    @Setter
    private long totalCount = 0;
    /**
     * -- GETTER --
     * 获得页内的记录列表.
     * -- SETTER --
     * 设置页内的记录列表.
     */
    //查询结果集
    @Setter
    @Getter
    private List<T> result;

    public Page() {
        pageSize = PAGE_SIZE;
    }

    public Page(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 设置当前页的页号,小于1时自动设置为1.
     */
    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
        if (pageNo < 1) {
            this.pageNo = 1;
        }
    }

    /**
     * 返回Page对象自身的setPageNo函数,可用于连续设置。
     */
    public Page<T> pageNo(int thePageNo) {
        setPageNo(thePageNo);
        return this;
    }

    /**
     * 返回Page对象自身的setPageSize函数,用于连续设置。
     */
    public Page<T> pageSize(int thePageSize) {
        setPageSize(thePageSize);
        return this;
    }

    /**
     * 获得总记录数, 默认值为0.
     */
    public long getTotalCount() {
        return totalCount < 0 ? 0 : totalCount;
    }

    /**
     * 根据pageSize与totalCount计算总页数, 默认值为-1.
     */
    public long getTotalPages() {
        if (totalCount < 0) {
            return 0;
        }

        long count = totalCount / pageSize;
        if (totalCount % pageSize > 0) {
            count++;
        }
        return count;
    }

    /**
     * 是否还有下一页.
     */
    public boolean isHasNext() {
        return (pageNo + 1 <= getTotalPages());
    }

    /**
     * 取得下页的页号, 序号从1开始.
     * 当前页为尾页时仍返回尾页序号.
     */
    public int getNextPage() {
        if (isHasNext()) {
            return pageNo + 1;
        } else {
            return pageNo;
        }
    }

    /**
     * 是否还有上一页.
     */
    public boolean isHasPre() {
        return (pageNo - 1 >= 1);
    }

    /**
     * 取得上页的页号, 序号从1开始.
     * 当前页为首页时返回首页序号.
     */
    public int getPrePage() {
        if (isHasPre()) {
            return pageNo - 1;
        } else {
            return pageNo;
        }
    }

}
