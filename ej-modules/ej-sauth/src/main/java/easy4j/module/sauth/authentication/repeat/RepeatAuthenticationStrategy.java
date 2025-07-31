package easy4j.module.sauth.authentication.repeat;

import easy4j.module.sauth.authentication.AuthenticationContext;

/**
 * 重复登录的策略
 * 1、颁发新的会话
 * 2、公用之前的会话
 * 3、不允许重复登录
 * 4、踢掉已经登录的会话，建立新的会话
 */
public interface RepeatAuthenticationStrategy {


    boolean checkRepeat(AuthenticationContext authenticationContext);


}
