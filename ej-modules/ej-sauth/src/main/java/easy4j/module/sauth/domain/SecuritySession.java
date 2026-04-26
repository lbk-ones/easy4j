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

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.infra.dbaccess.dynamic.dll.DDLField;
import easy4j.module.sauth.authentication.JWTUtils;
import easy4j.module.seed.CommonKey;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * SecuritySession
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
@JdbcTable(name = "sys_security_session")
public class SecuritySession implements ISecurityEasy4jSession {

    /**
     * sessionId
     */
    @Schema(description = "主键ID")
    @TableId(value = "session_id", type = IdType.AUTO)
    private Long sessionId;


    /**
     * 用户名（账号）
     */
    @Schema(description = "用户名（账号）")
    @TableField("username")
    private String username;

    /**
     * 租户ID
     */
    @Schema(description = "租户ID")
    @TableField("tenant_id")
    private Long tenantId;


    /**
     * 访问token
     */
    @Schema(description = "访问token")
    @TableField("sha_token")
    private String shaToken;


    /**
     * 原始TOKEN(通过原始token生成访问token)
     */
    @Schema(description = "原始TOKEN(通过原始token生成访问token)")
    @TableField("real_token")
    private String realToken;


    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    @TableField("expire_time_seconds")
    private Long expireTimeSeconds;


    /**
     * 用户姓名（中文）
     */
    @Schema(description = "用户姓名（中文）")
    @TableField("username_cn")
    private String usernameCn;


    /**
     * 访问token随机值
     */
    @Schema(description = "访问token随机值")
    @TableField("sha_token_salt")
    private String shaTokenSalt;


    /**
     * 用户姓名（英文）
     */
    @Schema(description = "用户姓名（英文）")
    @TableField("username_en")
    private String usernameEn;


    /**
     * UA
     */
    @Schema(description = "UA")
    @TableField("user_agent")
    private String userAgent;


    /**
     * 是否失效 1在线 0被踢
     */
    @Schema(description = "是否失效 1在线 0被踢")
    @TableField("is_invalid")
    private Integer isInvalid;


    /**
     * 访问token生成方式
     */
    @Schema(description = "访问token生成方式")
    @TableField("sha_token_type")
    private Integer shaTokenType;


    /**
     * 设备唯一ID
     */
    @Schema(description = "设备唯一ID")
    @TableField("device_id")
    private String deviceId;


    /**
     * 会话IP
     */
    @Schema(description = "会话IP")
    @TableField("ip")
    private String ip;


    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 昵称
     */
    @Schema(description = "昵称")
    @TableField("nick_name")
    private String nickName;


    // 创建人
    @TableField(value="create_by",fill = FieldFill.INSERT)
    @Schema(description = "创建人用户代码")
    private String createBy;

    @TableField(value="create_name",fill = FieldFill.INSERT)
    @Schema(description = "创建人姓名")
    private String createName;

    // 创建时间
    @TableField(value="create_time",fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private Date createTime;

    // 更新人
    @TableField(value="update_by",fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人代码")
    private String updateBy;

    @TableField(value="update_name",fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人姓名")
    private String updateName;

    // 更新时间
    @TableField(value="last_update_time",fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private Date lastUpdateTime;


    /**
     * 校验是否被篡改
     *
     * @return
     */
    public boolean isNotTampered() {
        return StrUtil.equals(shaToken, DigestUtil.sha1Hex(realToken + this.shaTokenSalt));
    }

    public String genShaToken() {
        if (StrUtil.hasBlank(this.realToken, this.shaTokenSalt)) return "";
        return DigestUtil.sha1Hex(realToken + this.shaTokenSalt);
    }

    public boolean isNotExpired() {
        //if (this.loginDateTime == null) return true;
        //long time = this.loginDateTime.getTime();
        Date date = new Date(expireTimeSeconds);
        return new Date().before(date);
    }

    public String genSalt() {
        return RandomUtil.randomString(5);
    }

    @JsonIgnore
    public boolean isValid() {
        return this.isNotExpired() && this.isNotTampered() && this.isInvalid == 0;
    }

    /**
     * 初始化token
     *
     * @param reqUser 请求传参|区别于数据库中查出来的用户信息
     */
    public SecuritySession init(ISecurityEasy4jUser reqUser) {
        String username = reqUser.getUsername();
        String usernameCn = reqUser.getUsernameCn();
        long l = Easy4j.getProperty(SysConstant.EASY4J_AUTH_SESSION_EXPIRE_TIME, int.class) * 1000L;
        long expireTimeMilli = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + l;
        this.realToken = JWTUtils.genJwtToken(username, usernameCn, expireTimeMilli, null);
        this.shaTokenSalt = genSalt();
        this.shaToken = genShaToken();
        //this.loginDateTime = new Date();
        this.isInvalid = 0;
        this.username = reqUser.getUsername();
        this.userId = reqUser.getUserId();
        this.sessionId = CommonKey.gennerLong();
        this.expireTimeSeconds = expireTimeMilli;
        //this.extMap = reqUser.getExtMap() == null ? new HashMap<>() : reqUser.getExtMap();
        //this.deptCode = reqUser.getDeptCode();
        //this.deptName = reqUser.getDeptName();
        this.deviceId = reqUser.getDeviceInfo();
        this.ip = reqUser.getIp();

        //this.orgCode = reqUser.getOrgCode();
        //this.orgCode = reqUser.getOrgName();
        this.tenantId = reqUser.getTenantId();
        //this.tenantName = reqUser.getTenantName();
        return this;
    }

}
