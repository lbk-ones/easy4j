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
package template.service.api.dto;

import lombok.Data;

/**
 * AccountDto
 *
 * @author bokun.li
 * @date 2025-06-15
 */
@Data
public class AccountDto {

    private String patId;    // 患者ID
    private Integer balance; // 余额

    private Integer frozeAmount;// 冻结金额

    private Integer unFrozeAmount;// 解冻金额


    private Integer reduceAmount;// 划扣金额
}