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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.EasyMap;
import easy4j.infra.common.utils.ListTs;

import java.util.List;
import java.util.function.Consumer;

/**
 * EasyPageResult
 * 分页返回
 * 使用方法
 * 1、EasyPageRes.from(Page<T> mybatisPlus);
 * 2、new EasyPageRes(pageNo,pageSize,records);
 * 3、new EasyPageRes(records);
 * 4、easyPageRes.setPageNo(long);
 * 5、easyPageRes.getPageNo();
 * 6、easyPageRes.loopRecords(Class<T> aclass,e->{});
 *
 * @author bokun.li
 * @date 2025/8/8
 */
public class EasyPageRes extends EasyMap<String, Object> {

    public static final String PAGE_NO = "pageNo";
    public static final String PAGE_SIZE = "pageSize";
    public static final String TOTAL = "total";
    public static final String RECORDS = "records";

    public static EasyPageRes get() {
        return new EasyPageRes();
    }

    public EasyPageRes() {
        init(this);
    }

    public EasyPageRes(long pageNo, long pageSize, List<?> objects) {
        init(this);
        setPageNo(pageNo).
                setPageSize(pageSize).
                setRecords(objects);

    }

    public EasyPageRes(List<?> objects) {
        init(this);
        setRecords(objects);
    }


    public static void init(EasyPageRes that) {
        that.put(PAGE_NO, 0);
        that.put(PAGE_SIZE, 0);
        that.put(TOTAL, 0);
        that.put(RECORDS, ListTs.newList());
    }

    @Desc("从mybatis-plus转换")
    public static <T> EasyPageRes from(Page<T> page) {
        EasyPageRes tEasyPageRes = new EasyPageRes();
        init(tEasyPageRes);
        List<T> records1 = page.getRecords();
        tEasyPageRes.put(PAGE_NO, page.getCurrent());
        tEasyPageRes.put(PAGE_SIZE, page.getSize());
        tEasyPageRes.put(TOTAL, page.getTotal());
        tEasyPageRes.put(RECORDS, records1);
        return tEasyPageRes;
    }

    public EasyPageRes setPageNo(long pageNo) {
        put(PAGE_NO, pageNo);
        return this;
    }

    public EasyPageRes setPageSize(long pageSize) {
        put(PAGE_SIZE, pageSize);
        return this;
    }

    public EasyPageRes setRecords(Iterable<?> records) {
        put(RECORDS, records);
        return this;
    }

    public EasyPageRes setTotal(long total) {
        put(TOTAL, total);
        return this;
    }

    public long getPageNo() {
        Object o = get(PAGE_NO);
        return Convert.toLong(o);
    }

    public long getPageSize() {
        return Convert.toLong(get(PAGE_SIZE));
    }

    @Desc("需要传入集合对象的Class类型")
    public <T> List<T> getRecords(Class<T> aclass) {
        return Convert.toList(aclass, get(RECORDS));
    }

    public long getTotal() {
        return Convert.toLong(get(TOTAL));
    }

    @Desc("遍历数据集合中的每一项")
    public <T> void loopRecords(Class<T> aclass, Consumer<T> consumer) {
        List<T> records = getRecords(aclass);
        if (CollUtil.isEmpty(records)) return;
        for (T record : records) {
            consumer.accept(record);
        }
    }
}
