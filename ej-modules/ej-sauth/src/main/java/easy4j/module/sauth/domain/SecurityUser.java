package easy4j.module.sauth.domain;

import easy4j.module.base.plugin.dbaccess.annotations.JdbcColumn;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcIgnore;
import easy4j.module.base.plugin.dbaccess.annotations.JdbcTable;
import lombok.Data;

import java.util.Date;

@Data
@JdbcTable(name = "sys_security_user")
public class SecurityUser {

    /**
     * userId 长号 (主键)
     */
    @JdbcColumn(isPrimaryKey = true)
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
