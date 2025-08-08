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

import easy4j.module.mybatisplus.audit.BaseAudit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
/**
 * AuditPageDto
 * with audit fields page dto
 *
 * @author bokun.li
 * @date 2025/7/24
 */
@Getter
@Setter
public class AuditPageDto extends BaseAudit implements APage {
    // 从1开始
    @Schema(description = "页码 从1开始")
    private int pageNo = 1;
    // 默认20条
    @Schema(description = "每页多少条，默认20")
    private int pageSize = 20;

    @Schema(description = "如果一个输入的值可以查很多字段那么就用这个，比如就一个框然后啥都查")
    private String searchKey;

    @Schema(description = "过滤的条件，格式为二维数组的集合 [[\"status\",\"eq\",\"xxx\"]] 支持eq in like gt gte lt lte tgt(t开头的是时间) tgte tlt tlte between betweene(包含) 为in的话第三个参数为json类型的字符串数组其他不用转成json")
    private List<List<Object>> keys;


}
