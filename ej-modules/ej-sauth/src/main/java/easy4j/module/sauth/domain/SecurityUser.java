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
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * SecurityUser
 *
 * @author bokun.li
 * @date 2025-05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JdbcTable(name = "sys_security_user")
@Schema(description = "系统通用用户信息")
public class SecurityUser extends AbstractSecurityEasy4jUser {


    /**
     * userId 长号 (主键)
     */
    @JdbcColumn(isPrimaryKey = true)
    @Schema(description = "用户主键 长号 (主键)")
    private long userId;

    /**
     * 用户名 短号 唯一索引 IDX_SYS_SECURITY_USER_USERNAME
     */
    @Schema(description = "用户名/登录账号")
    private String username;


    /**
     * 出生年月日
     */
    @Schema(description = "出生年月日")
    private Date birthDate;

    /**
     * 性别
     */
    @Schema(description = "性别 1男2女0未知")
    private int sex;
    /**
     * 联系电话号码
     */
    @Schema(description = "联系电话号码")
    private String phone;
    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCard;

    /**
     * 地址
     */
    @Schema(description = "地址")
    private String address;


    /**
     * 密码（加密之后的）
     */
    @Schema(description = "密码（加密之后的）")
    private String password;


    /**
     * 中文姓名
     */
    @Schema(description = "中文姓名")
    private String usernameCn;

    /**
     * 外国名
     */
    @Schema(description = "英文姓名")
    private String usernameEn;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickName;

    /**
     * 当前部门代码
     */
    @Schema(description = "当前部门代码")
    private String deptCode;

    /**
     * 当前部门名称
     */
    @Schema(description = "当前部门名称")
    private String deptName;


    /**
     * 用户是否过期 true代表没过期
     */
    @Schema(description = "用户是否没过期 true代表没过期")
    private boolean accountNonExpired;

    /**
     * 用户是否被锁 true代表没被锁
     */
    @Schema(description = "用户是否没被锁 true代表没被锁")
    private boolean accountNonLocked;

    /**
     * 密码是否过期 true代表没过期
     */
    @Schema(description = "密码是否没过期 true代表没过期")
    private boolean credentialsNonExpired;

    /**
     * 账户是否可用 true代表可用
     */
    @Schema(description = "账户是否可用 true代表可用")
    private boolean enabled;


    /**
     * 加密随机数
     */
    @Schema(description = "加密随机数")
    private String pwdSalt;


    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createDate;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateDate;

    /**
     * 机构代码
     */
    @Schema(description = "机构代码")
    private String orgCode;

    /**
     * 机构名称
     */
    @Schema(description = "机构名称")
    private String orgName;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    private String tenantId;

    /**
     * 租户名称
     */
    @Schema(description = "租户名称")
    private String tenantName;

    /**
     * 创建人代码
     */
    @Schema(description = "创建人代码")
    private String createBy;


    /**
     * 创建人姓名
     */
    @Schema(description = "创建人姓名")
    private String createName;

    /**
     * 更新人
     */
    @Schema(description = "更新人代码")
    private String updateBy;

    /**
     * 更新人姓名
     */
    @Schema(description = "更新人姓名")
    private String updateName;


}
