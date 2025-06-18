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
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.base.properties.EjSysProperties;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.dbaccess.annotations.JdbcColumn;
import easy4j.infra.dbaccess.annotations.JdbcTable;
import easy4j.module.seed.CommonKey;
import lombok.Data;

import java.nio.charset.StandardCharsets;
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
     * 会话是否有效  1无效 0有效
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
     * @param securityUser
     */
    public SecuritySession init(SecurityUserInfo securityUser) {
        String username = securityUser.getUsername();
        String usernameCn = securityUser.getUsernameCn();
        String ejSysPropertyName = Easy4j.getEjSysPropertyName(EjSysProperties::getJwtSecret);
        String signatureSecret = Easy4j.getProperty(ejSysPropertyName);
        // unique
        String s = CommonKey.gennerString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("un", username);
        claims.put("cn", usernameCn);
        claims.put("jti", s);

        this.jwtToken = JWT.create()
                .addPayloads(claims)
                .setKey(signatureSecret.getBytes(StandardCharsets.UTF_8))
                .sign();
//        this.jwtToken = JWT.create()
//                .setPayload("un", username)
//                .setJWTId(s)
//                .setPayload("cn", usernameCn)
//                .setKey(signatureSecret.getBytes(StandardCharsets.UTF_8)).sign();
        this.salt = genSalt();
        this.shaToken = getShaToken();
        this.loginDateTime = new Date();
        this.isInvalid = 0;
        this.userName = securityUser.getUsername();
        this.userId = securityUser.getUserId();
        this.id = CommonKey.gennerLong();
        long l = Easy4j.getProperty(SysConstant.EASY4J_AUTH_SESSION_EXPIRE_TIME, int.class) * 1000L;
        this.expireTimeSeconds = new Date().getTime() + l;
        this.extMap = securityUser.getExtMap();
        this.deptCode = securityUser.getDeptCode();
        this.deptName = securityUser.getDeptName();
        this.nickName = securityUser.getNickName();
        this.deviceInfo = securityUser.getDeviceInfo();
        this.ip = securityUser.getIp();
        this.userNameCn = securityUser.getUsernameCn();
        this.userNameEn = securityUser.getUsernameEn();
        return this;
    }

}
