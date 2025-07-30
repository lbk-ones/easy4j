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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AuditDemo
 * 继承这个类实现自动审计
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
public class AutoAudit implements Serializable {

    // 创建人
    @TableField(value="create_by",fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value="create_name",fill = FieldFill.INSERT)
    private String createName;

    // 创建时间
    @TableField(value="create_date",fill = FieldFill.INSERT)
    private Date createTime;

    // 更新人
    @TableField(value="update_by",fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value="update_name",fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    // 更新时间
    @TableField(value="last_update_date",fill = FieldFill.INSERT_UPDATE)
    private Date lastUpdateTime;


}
