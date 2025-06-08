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
package easy4j.module.jpa.page;

import easy4j.infra.common.exception.EasyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * PageableTools
 *
 * @author bokun.li
 * @date 2025-05
 */
public class PageableTools {
    /**
     * 获取基础分页对象
     *
     * @param page 获取第几页
     * @param size 每页条数
     * @param dtos 排序对象数组
     * @return
     */
    public static Pageable basicPage(Integer page, Integer size, SortDto... dtos) throws EasyException {
        Sort sort = SortTools.basicSort(dtos);
        page = (page == null || page < 0) ? 0 : page;
        size = (size == null || size <= 0) ? 15 : size;
        return PageRequest.of(page, size, sort);
    }

    /**
     * 获取基础分页对象，每页条数默认15条
     * - 默认以id降序排序
     *
     * @param page 获取第几页
     * @return
     */
    public static Pageable basicPage(Integer page) throws EasyException {
        return basicPage(page, 0, new SortDto("desc", "id"));
    }

    /**
     * 获取基础分页对象，每页条数默认15条
     *
     * @param page 获取第几页
     * @param dtos 排序对象数组
     * @return
     */
    public static Pageable basicPage(Integer page, SortDto... dtos) throws EasyException {
        return basicPage(page, 0, dtos);
    }

    /**
     * 获取基础分页对象，排序方式默认降序
     *
     * @param page       获取第几页
     * @param size       每页条数
     * @param orderField 排序字段
     * @return
     */
    public static Pageable basicPage(Integer page, Integer size, String orderField) throws EasyException {
        return basicPage(page, size, new SortDto("desc", orderField));
    }

    /**
     * 获取基础分页对象
     * - 每页条数默认15条
     * - 排序方式默认降序
     *
     * @param page       获取第几页
     * @param orderField 排序字段
     * @return
     */
    public static Pageable basicPage(Integer page, String orderField) throws EasyException {
        return basicPage(page, 0, new SortDto("desc", orderField));
    }

}
