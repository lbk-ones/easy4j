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
package easy4j.module.mybatisplus.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;
import lombok.experimental.Accessors;
import java.util.List;

/**
 * EasyPageResult
 * 分页返回
 *
 * @author bokun.li
 * @date 2025/8/8
 */
@Accessors(chain = true)
public class EasyPageResult extends EasyMap<String, Object> {

    public static final String PAGE_NO = "pageNo";
    public static final String PAGE_SIZE = "pageSize";
    public static final String TOTAL = "total";
    public static final String RECORDS = "records";

    public static EasyPageResult get() {
        return new EasyPageResult();
    }

    public EasyPageResult() {
        init(this);
    }

    public static void init(EasyPageResult that) {
        that.put(PAGE_NO, 0);
        that.put(PAGE_SIZE, 0);
        that.put(TOTAL, 0);
        that.put(RECORDS, ListTs.newList());
    }

    @Desc("从mybatis-plus转换")
    public static <T> EasyPageResult from(Page<T> page) {
        EasyPageResult tEasyPageResult = new EasyPageResult();
        init(tEasyPageResult);
        List<T> records1 = page.getRecords();
        tEasyPageResult.put(PAGE_NO, page.getCurrent());
        tEasyPageResult.put(PAGE_SIZE, page.getSize());
        tEasyPageResult.put(TOTAL, page.getTotal());
        tEasyPageResult.put(RECORDS, records1);
        return tEasyPageResult;
    }

    public EasyPageResult setPageNo(long pageNo){
        put(PAGE_NO,pageNo);
        return this;
    }
    public EasyPageResult setPageSize(long pageSize){
        put(PAGE_SIZE,pageSize);
        return this;
    }

    public EasyPageResult setRecords(Iterable<?> records){
        put(RECORDS,records);
        return this;
    }

}
