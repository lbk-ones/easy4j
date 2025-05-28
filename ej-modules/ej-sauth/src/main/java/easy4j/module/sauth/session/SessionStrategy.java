package easy4j.module.sauth.session;

import easy4j.module.sauth.domain.SecuritySession;

public interface SessionStrategy {
    /**
     * 根据会话token获取会话信息
     *
     * @param token
     * @return
     */
    SecuritySession getSession(String token);

    /**
     * 根据用户名获取会话信息
     *
     * @param userName
     * @return
     */
    SecuritySession getSessionByUserName(String userName);

    /**
     * 保存会话信息
     *
     * @param securitySession
     * @return
     */
    SecuritySession saveSession(SecuritySession securitySession);


    /**
     * 删除会话信息
     *
     * @param token
     */
    void deleteSession(String token);

    /**
     * 刷新会话信息
     *
     * @param token
     * @return
     */
    SecuritySession refreshSession(String token);


    void clearInValidSession();
}
