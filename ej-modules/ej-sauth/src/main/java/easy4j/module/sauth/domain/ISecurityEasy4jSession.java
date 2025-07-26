package easy4j.module.sauth.domain;

import java.util.Date;
import java.util.Map;

/**
 * 用户会话
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public interface ISecurityEasy4jSession {

    /**
     * 会话ID
     */
    long getSessionId();

    /**
     * jwt加密之后的短token 索引 IDX_SYS_SECURITY_SESSION_SHA_TOKEN
     */
    String getShaToken();

    /**
     * jwtToken
     */
    String getJwtToken();

    /**
     * 将SessionJwtToken加密成SessionShaToken的盐值
     */
    String getJwtSalt();

    /**
     * ip
     */
    String getIp();

    /**
     * 设备信息 （浏览器、手机型号等）
     */
    String getDeviceInfo();

    /**
     * 登录时间
     */
    Date getLoginDateTime();

    /**
     * 登出时间
     */
    Date getLogoutDateTime();

    /**
     * 会话是否有效  1无效 0有效
     */
    int getIsInvalid();

    /**
     * 过期时间（秒为单位）
     */
    long getExpireTimeSeconds();

    /**
     * 用户唯一ID
     *
     * @return
     */
    long getUserId();

    /**
     * 用户CODE
     *
     * @return
     */
    String getUserName();


    /**
     * 额外信息 存入 长文本 json 字符串
     */
    Map<String, Object> getExtMap();

    /**
     * 会话是否有效 返回true代表生效
     *
     * @return
     */
    boolean isValid();


}
