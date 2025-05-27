package easy4j.module.sauth.core;

import cn.hutool.core.util.StrUtil;
import easy4j.module.base.exception.EasyException;
import easy4j.module.sauth.domain.SecurityUser;
import easy4j.module.sauth.domain.SecurityUserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractSecurityAuthentication implements SecurityAuthentication {

    @Autowired
    EncryptionService encryptionService;

    public HttpServletRequest getServletRequest() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        assert sra != null;
        return (HttpServletRequest) sra.getRequest();
    }

    // 校验密码是否准确
    // 动态查询患者信息
    // 动态选择加密方式
    @Override
    public boolean verifyAuthentication(SecurityUserInfo user) {
        HttpServletRequest servletRequest = getServletRequest();
        String method = servletRequest.getMethod();
        if ("POST".equalsIgnoreCase(method)) {
            throw new EasyException("请使用POST方式提交");
        }
        String username = user.getUsername();
        String password = user.getPassword();
        if (StrUtil.isBlank(username)) {
            throw new EasyException("用户名不能为空");
        }
        if (StrUtil.isBlank(password)) {
            throw new EasyException("密码不能为空");
        }
        SecurityUserInfo userByUserName = getUserByUserName(username);
        if (userByUserName == null) {
            throw new EasyException("用户不存在");
        }
        String encryptPwd = encryptionService.encrypt(password, user);
        return StrUtil.equals(encryptPwd, userByUserName.getPassword());
    }


    /**
     * 默认检查通过 如果想更改可以覆盖
     *
     * @param user
     * @return
     * @throws EasyException
     */
    @Override
    public boolean checkUser(SecurityUserInfo user) throws EasyException {
        return true;
    }
}
