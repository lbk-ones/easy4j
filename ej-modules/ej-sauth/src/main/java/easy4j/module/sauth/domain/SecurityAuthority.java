package easy4j.module.sauth.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 将角色和权限展开
 * 角色：字符串
 * 权限：字符串
 */
@Data
public class SecurityAuthority implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 角色代码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 权限代码
     */
    private String authorityCode;


    /**
     * 权限名称
     */
    private String authorityName;


    /**
     * 请求地址 ant风格 /api/**
     */
    private String requestUri;

    /**
     * 额外信息
     */
    private Map<String, Object> extMap;

}
