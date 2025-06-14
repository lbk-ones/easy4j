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
import cn.hutool.jwt.JWT;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.module.seed.CommonKey;
import lombok.Data;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * SecuritySession
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
@JdbcTable(name = "sys_security_session")
public class SecuritySession {

    /**
     * 主键ID
     */
    @JdbcColumn(isPrimaryKey = true)
    private long id;

    /**
     * 用户名 索引 IDX_SYS_SECURITY_SESSION_USER_NAME
     */
    private String userName;

    /**
     * 用户ID(长) 索引 IDX_SYS_SECURITY_SESSION_USER_ID
     */
    private long userId;

    private String userNameCn;
    private String userNameEn;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * jwt加密之后的短token 索引 IDX_SYS_SECURITY_SESSION_SHA_TOKEN
     */
    private String shaToken;


    /**
     * jwtToken
     */
    private String jwtToken;

    /**
     * 将jwtToken加密成token的盐值
     */
    private String salt;

    /**
     * ip
     */
    private String ip;

    /**
     * 设备信息 （浏览器、手机型号等）
     */
    private String deviceInfo;


    /**
     * 登录时间
     */
    private Date loginDateTime;

    /**
     * 登出时间
     */
    private Date logoutDateTime;

    /**
     * 会话是否有效  0无效 1有效
     */
    private int isInvalid;

    /**
     * 过期时间（秒为单位）
     */
    private long expireTimeSeconds;


    /**
     * 当前部门代码
     */
    private String deptCode;

    /**
     * 当前部门名称
     */
    private String deptName;


    /**
     * 额外信息 存入 长文本 json 字符串
     */
    @JdbcColumn(toJson = true)
    private Map<String, Object> extMap;


    /**
     * 校验是否被篡改
     *
     * @return
     */
    public boolean isNotTampered() {
        return StrUtil.equals(shaToken, DigestUtil.sha1Hex(jwtToken + this.salt));
    }

    public String getShaToken() {
        if (StrUtil.hasBlank(this.jwtToken, this.salt)) return "";
        return DigestUtil.sha1Hex(jwtToken + this.salt);
    }

    public boolean isNotExpired() {
        if (this.loginDateTime == null) return true;
        long time = this.loginDateTime.getTime();
        Date date = new Date(time + (expireTimeSeconds * 1000L));
        return new Date().before(date);
    }

    public String genSalt() {
        return RandomUtil.randomString(5);
    }

    /**
     * 初始化token
     *
     * @param securityUser
     */
    public SecuritySession init(SecurityUserInfo securityUser) {
        String username = securityUser.getUsername();
        String usernameCn = securityUser.getUsernameCn();
        String ejSysPropertyName = Easy4j.getEjSysPropertyName(EjSysProperties::getJwtSecret);
        String signatureSecret = Easy4j.getProperty(ejSysPropertyName);
        this.jwtToken = JWT.create()
                .setPayload("un", username)
                .setPayload("cn", usernameCn)
                .setKey(signatureSecret.getBytes(StandardCharsets.UTF_8)).sign();

        this.salt = genSalt();

        this.shaToken = getShaToken();

        this.loginDateTime = new Date();
        this.isInvalid = 1;
        this.userName = securityUser.getUsername();
        this.userId = securityUser.getUserId();
        this.id = CommonKey.gennerLong();

        return this;
    }


}
