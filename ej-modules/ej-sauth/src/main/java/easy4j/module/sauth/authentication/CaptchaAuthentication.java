package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.BusCode;
import easy4j.infra.webmvc.WebContextUtil;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 验证码，其实验证码和其他类似，考虑到太常用了，单独拉一个出来
 *
 * @author bokun.li
 */
public class CaptchaAuthentication extends UserNamePasswordAuthentication {


    @Override
    public String getName() {
        return AuthenticationType.Captcha.name();
    }

    @Override
    public void verify(AuthenticationContext context) {
        ISecurityEasy4jUser reqUser = context.getReqUser();
        CaptchaVerify captchaVerify = reqUser.getCaptchaVerify();
        if (captchaVerify == null) {
            context.setErrorCode(BusCode.A00033);
        } else {
            if (!captchaVerify.verify(reqUser)) {
                context.setErrorCode(BusCode.A00033);
            }
        }

    }

    @Override
    public void verifyPre(ISecurityEasy4jUser user) {
        if (null == user) {
            user = new SecurityUser();
            user.setErrorCode(BusCode.A00004 + ",user");
            return;
        }
        HttpServletRequest servletRequest = WebContextUtil.getRequest();
        String method = servletRequest.getMethod();
        if (!"post".equalsIgnoreCase(method)) {
            user.setErrorCode(BusCode.A00030);
            return;
        }
        String username = user.getUsername();
        String captcha = user.getCaptcha();
        if (StrUtil.isBlank(username)) {
            user.setErrorCode(BusCode.A00031);
            return;
        }
        if (StrUtil.isBlank(captcha)) {
            user.setErrorCode(BusCode.A00066);
        }
    }
}
