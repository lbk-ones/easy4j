package easy4j.module.sauth.domain;

import easy4j.infra.common.annotations.Desc;
import easy4j.module.sauth.authentication.AuthenticationScopeType;
import easy4j.module.sauth.authentication.AuthenticationType;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 做一个用户适配器
 *
 * @author bokun.li
 * @date 2025-07-26
 */
public interface ISecurityEasy4jUser extends Serializable {

    /**
     * 用户唯一ID
     *
     * @return
     */
    @Desc("用户唯一ID")
    long getUserId();

    /**
     * 用户名（用户CODE）
     *
     * @return
     */
    @Desc("用户名（用户CODE）")
    String getUsername();

    /**
     * 设置用户名
     *
     * @param username
     */
    @Desc("设置用户名")
    void setUsername(String username);


    /**
     * 获取出生年月日
     *
     * @return
     */
    @Desc("获取出生年月日")
    Date getBirthDate();

    /**
     * 设置出生年月日
     *
     * @return
     */
    @Desc("设置出生年月日")
    void setBirthDate(Date birthDate);

    /**
     * 获取性别
     *
     * @return
     */
    @Desc("获取性别")
    int getSex();

    /**
     * 设置性别
     *
     * @return
     */
    @Desc("设置性别")
    void setSex(int sex);

    /**
     * 获取电话号码
     *
     * @return
     */
    @Desc("获取电话号码")
    String getPhone();

    /**
     * 设置电话号码
     *
     * @return
     */
    @Desc("设置电话号码")
    void setPhone(String phone);

    /**
     * 获取身份证号
     *
     * @return
     */
    @Desc("获取身份证号")
    String getIdCard();

    /**
     * 设置身份证号
     *
     * @return
     */
    @Desc("设置身份证号")
    void setIdCard(String idCard);

    /**
     * 获取地址
     *
     * @return
     */
    @Desc("获取地址")
    String getAddress();

    /**
     * 设置地址
     *
     * @return
     */
    @Desc("设置地址")
    void setAddress(String address);

    /**
     * 密码（加密/不加密都可以具体取决加密逻辑）
     *
     * @return
     */
    @Desc("密码（加密/不加密都可以具体取决加密逻辑）")
    String getPassword();

    /**
     * 中文姓名
     *
     * @return
     */
    @Desc("中文姓名")
    String getUsernameCn();


    /**
     * 设置用户名
     *
     * @param username
     */
    @Desc("设置用户名")
    void setUsernameCn(String usernameCn);

    /**
     * 英文姓名
     *
     * @return
     */
    @Desc("英文姓名")
    String getUsernameEn();

    /**
     * 昵称
     *
     * @return
     */
    @Desc("昵称")
    String getNickName();

    /**
     * 部门代码
     *
     * @return
     */
    @Desc("部门代码")
    String getDeptCode();

    /**
     * 部门名称
     *
     * @return
     */
    @Desc("部门名称")
    String getDeptName();

    /**
     * 机构代码
     *
     * @return
     */
    @Desc("机构代码")
    String getOrgCode();

    /**
     * 机构名称
     *
     * @return
     */
    @Desc("机构名称")
    String getOrgName();


    /**
     * 租户ID
     *
     * @return
     */
    @Desc("租户ID")
    String getTenantId();

    /**
     * 租户名称
     *
     * @return
     */
    @Desc("租户名称")
    String getTenantName();

    /**
     * 账户是否过期（true：没过期，false：过期）
     *
     * @return
     */
    @Desc("账户是否过期（true：没过期，false：过期）")
    boolean isAccountNonExpired();

    /**
     * 账户是否被锁定（true：没锁定，false：锁定了）
     *
     * @return
     */
    @Desc("账户是否被锁定（true：没锁定，false：锁定了）")
    boolean isAccountNonLocked();

    /**
     * 密码是否过期（true：没过期，false：过期了）
     *
     * @return
     */
    @Desc("密码是否过期（true：没过期，false：过期了）")
    boolean isCredentialsNonExpired();

    /**
     * 是否启用 true启用 false未启用
     *
     * @return
     */
    @Desc("是否启用 true启用 false未启用")
    boolean isEnabled();

    /**
     * 密码加密随机数
     *
     * @return
     */
    @Desc("密码加密随机数")
    String getPwdSalt();

    /**
     * 设置密码加密随机数
     *
     * @return
     */
    @Desc("设置密码加密随机数")
    void setPwdSalt(String pwdSalt);


    /**
     * 创建时间
     *
     * @return
     */
    @Desc("创建时间")
    Date getCreateDate();

    /**
     * 创建时间
     *
     * @return
     */
    @Desc("创建人代码")
    String getCreateBy();

    /**
     * 获取创建人姓名
     *
     * @return
     */
    @Desc("获取创建人姓名")
    String getCreateName();

    /**
     * 更新时间
     *
     * @return
     */
    @Desc("更新时间")
    Date getUpdateDate();

    /**
     * 更新人代码
     *
     * @return
     */
    @Desc("更新人代码")
    String getUpdateBy();

    /**
     * 更新人姓名
     *
     * @return
     */
    @Desc("更新人姓名")
    String getUpdateName();


    /**
     * 设置错误代码通常为i18n代码
     *
     * @return
     */
    @Desc("设置错误代码通常为i18n代码")
    void setErrorCode(String errorCode);

    /**
     * 获取错误代码通常为i18n代码
     *
     * @return
     */
    @Desc("获取错误代码通常为i18n代码")
    String getErrorCode();

    /**
     * 设置密码（不要传加了密的密码）
     *
     * @return
     */
    @Desc("设置密码（不要传加了密的密码）")
    void setPassword(String pwd);

    /**
     * 设置前端使用的token
     *
     * @return
     */
    @Desc("设置前端使用的token")
    void setShaToken(String shatToken);

    /**
     * 获取前端使用的token
     *
     * @return
     */
    @Desc("获取前端使用的token")
    String getShaToken();


    /**
     * 获取用户扩展信息
     *
     * @return
     */
    @Desc("获取用户扩展信息")
    Map<String, Object> getExtMap();

    /**
     * 设置扩展信息
     *
     * @return
     */
    @Desc("设置扩展信息")
    void setExtMap(Map<String, Object> extMap);

    /**
     * 获取更新人姓名
     *
     * @return
     */
    @Desc("获取更新人姓名")
    String getDeviceInfo();

    /**
     * 设置设备信息
     *
     * @return
     */
    @Desc("设置设备信息")
    void setDeviceInfo(String deviceInfo);

    /**
     * 获取IP
     *
     * @return
     */
    @Desc("获取IP")
    String getIp();

    /**
     * 设置IP
     *
     * @return
     */
    @Desc("设置IP")
    void setIp(String ip);

    /**
     * 是否是超级管理员
     *
     * @return
     */
    @Desc("是否是超级管理员")
    boolean isSuperAdmin();


    /**
     * 获取认证器类型，默认为用户名密码认证
     *
     * @return
     */
    @Desc("获取认证器类型，默认为用户名密码认证")
    AuthenticationType getAuthenticationType();

    /**
     * 设置认证器类型，默认为用户名密码认证
     *
     * @return
     */
    @Desc("设置认证器类型，默认为用户名密码认证")
    void setAuthenticationType(AuthenticationType authenticationType);

    /**
     * 作用于哪里 拦截器或者认证
     *
     * @return
     */
    @Desc("作用于哪里 拦截器或者认证 默认是认证")
    AuthenticationScopeType getScope();

    /**
     * 设置作用于哪里 拦截器或者认证
     *
     * @param scope
     */
    @Desc("作用于哪里 拦截器或者认证")
    void setScope(AuthenticationScopeType scope);


}
