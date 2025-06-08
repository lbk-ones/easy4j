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
package easy4j.infra.dbaccess.domain;

import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 一个简单的系统锁
 *
 * @author bokun.li
 * @date 2025/5/29
 */
@Data
@JdbcTable(name = "sys_lock")
public class SysLock implements Serializable {

    @JdbcColumn(isPrimaryKey = true)
    private String id;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 过期时间
     */
    private Date expireDate;

    /**
     * 备注 这一次锁的详细信息
     * 比如说是谁成功抢走的
     * 或者说加锁具体内容
     */
    private String remark;
}
