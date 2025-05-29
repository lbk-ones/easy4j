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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * SortDto
 *
 * @author bokun.li
 * @date 2025-05
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(name = "SortDto",description = "排序对象")
public class SortDto implements Serializable {
    public static final String DESC = "desc";
    public static final String ASC = "asc";
	//排序方式
    @Schema(description = "排序方式 desc | asc")
    private String orderType;

    //排序字段
    @Schema(description = "排序字段")
    private String orderField;

    public SortDto(String orderType, String orderField) {
        this.orderType = orderType;
        this.orderField = orderField;
    }

    //默认为DESC排序
    public SortDto(String orderField) {
        this.orderField = orderField;
        this.orderType = "desc";
    }
}
