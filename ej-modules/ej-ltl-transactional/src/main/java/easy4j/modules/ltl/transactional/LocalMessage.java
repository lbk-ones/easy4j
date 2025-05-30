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
package easy4j.modules.ltl.transactional;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * LocalMessage
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
@JdbcTable(name = "SYS_LOCAL_MESSAGE")
public class LocalMessage implements Serializable {

    // 正在发送
    public static final Integer PENDING = 0;
    // 已发送
    public static final Integer SENT = 1;

    // 失败
    public static final Integer FAILED = 2;

    @JdbcColumn(name = "MSG_ID", isPrimaryKey = true)
    private String msgId;
    private String businessKey;
    private String businessName;
    private String content;
    private String beanName;
    private String beanMethod;
    private Integer retryCount;
    private Integer status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    private String errorMessage;

    private String isFreeze;
}
