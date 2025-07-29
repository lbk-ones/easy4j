package easy4j.module.sauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.dbaccess.annotations.JdbcIgnore;
import easy4j.module.sauth.authentication.AuthenticationScopeType;
import easy4j.module.sauth.authentication.AuthenticationType;
import lombok.Data;

import java.util.Map;

/**
 * 将一些不存入库的字段放在这里 且标注 @JdbcIgnore 不要写入数据库去 数据库没这些字段
 *
 * @author bokun.li
 * @date 2025-07-26
 */
@Data
public abstract class AbstractSecurityEasy4jUser implements ISecurityEasy4jUser {


    @Desc("错误代码")
    @JdbcIgnore
    @JsonIgnore
    private String errorCode;

    @Desc("最终请求token")
    @JdbcIgnore
    private String shaToken;

    @Desc("请求IP")
    @JdbcIgnore
    private String ip;

    @Desc("附加信息")
    @JdbcIgnore
    private Map<String, Object> extMap;

    @Desc("设备信息")
    @JdbcIgnore
    private String deviceInfo;

    @Desc("是否是超级管理员")
    @JdbcIgnore
    private boolean superAdmin = false;

    @Desc("认证类型")
    @JdbcIgnore
    private String authenticationType = AuthenticationType.UserNamePassword.name();


    @Desc("作用域")
    @JdbcIgnore
    @JsonIgnore
    private AuthenticationScopeType scope = AuthenticationScopeType.Authentication;


}
