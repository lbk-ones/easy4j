package easy4j.module.sauth.authentication;

import easy4j.module.sauth.domain.ISecurityEasy4jUser;
/**
 * 自定义Bearer 鉴权接口
 * @author bokun.li
 * @date 2025/11/14
 */
public interface IBearerAuthentication {


    /**
     * 返回 Bearer 后面的字符串
     * @param bearerToken
     * @return
     */
    boolean verify(String bearerToken);

    /**
     * 返回 Bearer 后面的字符串
     * @param bearerToken
     * @return
     */
    ISecurityEasy4jUser obtainUserInfo(String bearerToken);


}
