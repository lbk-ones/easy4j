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
package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.context.SecurityContext;
import easy4j.module.sauth.core.EncryptionService;
import easy4j.module.sauth.core.StandardResolve;
import easy4j.module.sauth.core.loaduser.LoadUserApi;
import easy4j.module.sauth.domain.*;
import easy4j.module.sauth.session.SessionStrategy;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * AbstractSecurityAuthentication
 *
 * @author bokun.li
 * @date 2025-05
 */
public abstract class AbstractSecurityAuthentication extends StandardResolve implements SecurityAuthentication {

    public abstract EncryptionService getEncryptionService();

    public abstract SessionStrategy getSessionStrategy();


    public abstract SecurityContext getSecurityContext();


    // 校验密码是否准确
    // 动态查询患者信息
    // 动态选择加密方式
    @Override
    public ISecurityEasy4jUser verifyLoginAuthentication(ISecurityEasy4jUser user, ISecurityEasy4jUser userByUserName) {
        if (userByUserName == null) {
            user.setErrorCode(BusCode.A00037);
            return user;
        }
        SessionStrategy sessionStrategy = getSessionStrategy();
        // 2、second query session info
        SecuritySession sessionByUserName = sessionStrategy.getSessionByUserName(user.getUsername());
        if (null != sessionByUserName) {
            if (sessionByUserName.isValid()) {
                user.setErrorCode(BusCode.A00044);
                return user;
            } else {
                String shaToken = sessionByUserName.getShaToken();
                sessionStrategy.deleteSession(shaToken);
            }
        }
        // 3、confirm is been skiped and refresh session？
        if (!user.isSkipPassword()) {
            String encryptPwd = getEncryptionService().encrypt(user.getPassword(), userByUserName);
            if (StrUtil.equals(encryptPwd, userByUserName.getPassword())) {
                return user;
            } else {
                user.setErrorCode(BusCode.A00033);
            }
        }

        return user;

    }


    /**
     * 默认检查通过 如果想更改可以覆盖
     *
     * @param user
     * @return
     * @throws EasyException
     */
    @Override
    public boolean checkUser(ISecurityEasy4jUser user) throws EasyException {
        return true;
    }


    /**
     * 根据token重新鉴权
     * 查一次会话信息
     * 查一次用户信息
     * 查一次权限信息
     * 刷新会话（还剩十分钟的时候刷新，具体多少分钟是配置决定的）
     *
     * @param token
     * @return
     * @throws EasyException
     */
    @Override
    public OnlineUserInfo tokenAuthentication(String token) throws EasyException {
        OnlineUserInfo onlineUserInfo = new OnlineUserInfo();
        SecurityUser securityUserInfo = new SecurityUser();
        SessionStrategy securitySession = getSessionStrategy();
        // query session and verify session
        SecuritySession session = securitySession.getSession(token);
        if (session == null) {
            securityUserInfo.setErrorCode(BusCode.A00034);
            onlineUserInfo.setUser(securityUserInfo);
            return onlineUserInfo;
        }
        if (!session.isValid()) {
            securityUserInfo.setErrorCode(BusCode.A00035);
            onlineUserInfo.setUser(securityUserInfo);
            return onlineUserInfo;
        }
        onlineUserInfo = sessionToSecurityUserInfo(session);
        //ISecurityEasy4jUser userByUserName = LoadUserApi.getByUserName(session.getUserName());
        if (getAuthorizationStrategy().checkByUserInfo(onlineUserInfo.getUser())) {
            // refresh session
            session = securitySession.refreshSession(token, null, null);
            getSecurityContext().setSession(session);
        }
        return onlineUserInfo;
    }
}
