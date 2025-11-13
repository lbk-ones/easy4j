package easy4j.module.sauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import easy4j.infra.common.annotations.Desc;
import easy4j.infra.dbaccess.annotations.JdbcIgnore;
import easy4j.module.sauth.authentication.AuthenticationScopeType;
import easy4j.module.sauth.authentication.AuthenticationType;
import easy4j.module.sauth.authentication.LoadAuthentication;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "错误代码")
    private String errorCode;

    @Desc("最终请求token")
    @JdbcIgnore
    @Schema(description = "返回给客户端（前端）保存之后后续使用的token")
    private String shaToken;


    @Desc("验证码")
    @JdbcIgnore
    @Schema(description = "验证码")
    private String verifyCode;

    @Desc("did身份验证")
    @JdbcIgnore
    @Schema(description = "did身份验证")
    private String did;


    @Desc("请求IP")
    @JdbcIgnore
    @Schema(description = "请求IP")
    private String ip;

    @Desc("附加信息")
    @JdbcIgnore
    @Schema(description = "附加信息")
    private Map<String, Object> extMap;

    @Desc("设备信息")
    @JdbcIgnore
    @Schema(description = "客户端设备信息")
    private String deviceInfo;

    @Desc("是否是超级管理员")
    @JdbcIgnore
    @Schema(description = "是否是超级管理员")
    private boolean superAdmin = false;

    @Desc("认证类型")
    @JdbcIgnore
    @Schema(description = "认证类型，username，jwt，basic，simple")
    private String authenticationType = AuthenticationType.UserNamePassword.name();


    @Desc("作用域")
    @JdbcIgnore
    @JsonIgnore
    @Schema(description = "作用域，（认证、拦截）")
    private AuthenticationScopeType scope = AuthenticationScopeType.Authentication;

    @Desc("其他鉴权方式")
    @JdbcIgnore
    @Schema(description = "其他鉴权方式")
    private LoadAuthentication loadAuthentication;
}
