package easy4j.module.sauth.authentication;

import easy4j.module.sauth.domain.ISecurityEasy4jUser;

public interface CaptchaVerify {

    /**
     * 将传进来的用户信息回传回去
     * @param iSecurityEasy4jUser 用户信息
     * @return 是否通过
     */
    boolean verify(ISecurityEasy4jUser iSecurityEasy4jUser);

}
