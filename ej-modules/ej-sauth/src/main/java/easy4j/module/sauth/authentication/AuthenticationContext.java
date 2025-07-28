package easy4j.module.sauth.authentication;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.exception.EasyException;
import easy4j.infra.common.utils.BusCode;
import easy4j.module.sauth.domain.ISecurityEasy4jSession;
import easy4j.module.sauth.domain.ISecurityEasy4jUser;
import easy4j.module.sauth.domain.OnlineUserInfo;
import lombok.Data;

/**
 * 认证上下文
 *
 * @author bokun.li
 * @date 2025-07-27
 */
@Data
public class AuthenticationContext {

    /**
     * 传过来的用户信息
     */
    @Desc("传过来的用户信息")
    private ISecurityEasy4jUser reqUser;

    /**
     * 查询出来的用户信息
     */
    @Desc("查询出来的用户信息")
    private ISecurityEasy4jUser dbUser;


    /**
     * 查询出来的会话信息
     */
    @Desc("查询出来的会话信息")
    private ISecurityEasy4jSession dbSession;


    /**
     * 在线用户信息 这个的生成会比较靠后
     */
    @Desc("在线用户信息 这个的生成会比较靠后")
    private OnlineUserInfo onlineUserInfo;

    /**
     * 认证器名称
     */
    private String name;


    /**
     * 预校验结果
     */
    private boolean preVerifyResult;

    /**
     * 正校验结果
     */
    private boolean verifyResult;

    /**
     * 错误代码
     */
    private String errorCode;

    public ISecurityEasy4jUser getReqUser() {
        if (reqUser == null) {
            throw EasyException.wrap(BusCode.A00004,"【AuthenticationContext of reqUser】");
        }
        return reqUser;
    }

    public void checkError() {
        if (StrUtil.isNotBlank(this.errorCode)) {
            if (this.reqUser != null) {
                this.reqUser.setErrorCode(errorCode);
            }
            throw new EasyException(this.errorCode);
        }
    }

    public void checkError(String code) {
        if (StrUtil.isNotBlank(this.errorCode)) {
            if (this.reqUser != null) {
                this.reqUser.setErrorCode(EasyException.getCodeFromMessage(this.errorCode));
            }
            throw new EasyException(code);
        }
    }
}
