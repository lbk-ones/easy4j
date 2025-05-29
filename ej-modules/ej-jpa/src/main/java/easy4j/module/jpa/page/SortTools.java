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

import org.springframework.data.domain.Sort;

/**
 * SortTools
 *
 * @author bokun.li
 * @date 2025-05
 */
public class SortTools {
	public static Sort basicSort() {
        return basicSort("desc", "id");
    }

    public static Sort basicSort(String orderType, String orderField) {
        return Sort.by(Sort.Direction.fromString(orderType), orderField);
    }

    public static Sort basicSort(SortDto... dtos) {
        Sort result = Sort.unsorted();
        for (SortDto dto : dtos) {
            result = result.and(Sort.by(Sort.Direction.fromString(dto.getOrderType()), dto.getOrderField()));
        }
        return result;
    }
}
