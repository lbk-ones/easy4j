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
    Long getSessionId();

    /**
     * jwt加密之后的短token 索引 IDX_SYS_SECURITY_SESSION_SHA_TOKEN
     */
    String getShaToken();

    /**
     * jwtToken
     */
    String getRealToken();

    /**
     * 将SessionJwtToken加密成SessionShaToken的盐值
     */
    String getShaTokenSalt();

    /**
     * ip
     */
    String getIp();

    /**
     * 设备信息 （浏览器、手机型号等）
     */
    String getDeviceId();

    /**
     * 登录时间
     */
    //Date getLoginDateTime();



    /**
     * 会话是否有效  1无效 0有效
     */
    Integer getIsInvalid();

    /**
     * 过期时间（秒为单位）
     */
    Long getExpireTimeSeconds();

    /**
     * 用户唯一ID
     *
     * @return
     */
    Long getUserId();

    /**
     * 用户CODE
     *
     * @return
     */
    String getUsername();


    /**
     * 会话是否有效 返回true代表生效
     *
     * @return
     */
    boolean isValid();


}
