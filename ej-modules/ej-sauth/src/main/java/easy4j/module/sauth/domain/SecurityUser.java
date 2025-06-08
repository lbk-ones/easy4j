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
package easy4j.module.sauth.domain;

import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.util.Date;

/**
 * SecurityUser
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
@JdbcTable(name = "sys_security_user")
public class SecurityUser {

    /**
     * userId 长号 (主键)
     */
    @JdbcColumn(isPrimaryKey = true)
    private long userId;

    /**
     * 用户名 短号 唯一索引 IDX_SYS_SECURITY_USER_USERNAME
     */
    private String username;

    /**
     * 密码（加密之后的）
     */
    private String password;


    /**
     * 中文姓名
     */
    private String usernameCn;

    /**
     * 外国名
     */
    private String usernameEn;

    /**
     * 昵称
     */
    private String nickName;


    /**
     * 用户是否过期 true代表没过期
     */
    private boolean accountNonExpired;

    /**
     * 用户是否被锁 true代表没被锁
     */
    private boolean accountNonLocked;

    /**
     * 密码是否过期 true代表没过期
     */
    private boolean credentialsNonExpired;

    /**
     * 账户是否可用 true代表可用
     */
    private boolean enabled;


    /**
     * 加密随机数
     */
    private String pwdSalt;


    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date updateDate;


}
