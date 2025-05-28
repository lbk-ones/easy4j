package easy4j.module.sauth.domain;

import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
public class SecurityUserInfo {


    /**
     * 权限
     */
    private Set<SecurityAuthority> authorities;

    /**
     * 是否免密登录
     */
    private boolean isSkipAuthentication;


    long userId;


    /**
     * 用户名 短号 唯一索引 IDX_SYS_SECURITY_USER_USERNAME
     */
    private String username;

    /**
     * 密码（加密之后的）
     */
    private String password;

    /**
     * 是否跳过密码
     */
    private boolean isSkipPassword = false;


    /**
     * 中文姓名
     */
    private String usernameCn;

    /**
     * 外国名
     */
    private String usernameEn;

    /**
     * 昵称
     */
    private String nickName;


    /**
     * 用户是否过期 true代表没过期
     */
    private boolean accountNonExpired;

    /**
     * 用户是否被锁 true代表没被锁
     */
    private boolean accountNonLocked;

    /**
     * 密码是否过期 true代表没过期
     */
    private boolean credentialsNonExpired;

    /**
     * 账户是否可用 true代表可用
     */
    private boolean enabled;


    /**
     * 加密随机数
     */
    private String shalt;


    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 额外信息 不会存入数据库
     */
    private Map<String, Object> extMap;

    /**
     * 错误代码
     */
    private String errorCode;
    /**
     * 错误信息
     */
    private String errorMsg;

}
