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
package easy4j.module.mybatisplus.audit;

import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * 几个基本的审计字段
 * 给dto使用
 *
 * @author bokun.li
 * @date 2025/7/23
 */
@Data
public class BaseAudit implements Serializable {
    /**
     * 创建人编码
     */
    @DDLField(dataLength = 20)
    @Schema(description = "创建人编码")
    private String createBy;

    /**
     * 创建人名称
     */
    @DDLField(dataLength = 150)
    @Schema(description = "创建人名称")
    private String createName;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新人编码
     */
    @DDLField(dataLength = 20)
    @Schema(description = "更新人编码")
    private String updateBy;

    /**
     * 更新人姓名
     */
    @DDLField(dataLength = 150)
    @Schema(description = "更新人姓名")
    private String updateName;

    /**
     * 最新更新时间
     */
    @Schema(description = "最新更新时间")
    private Date lastUpdateTime;
}
