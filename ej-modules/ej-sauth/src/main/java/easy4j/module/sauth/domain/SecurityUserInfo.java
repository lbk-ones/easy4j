package easy4j.module.sauth.domain;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Set;

@Data
public class SecurityUserInfo {


    /**
     * 权限
     */
    @JdbcIgnore
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


}
