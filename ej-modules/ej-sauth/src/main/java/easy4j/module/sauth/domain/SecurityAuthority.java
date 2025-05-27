package easy4j.module.sauth.domain;

import lombok.Data;

import java.io.Serializable;

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

}
