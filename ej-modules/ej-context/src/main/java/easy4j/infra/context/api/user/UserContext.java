package easy4j.infra.context.api.user;

import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 给其他模块使用的
 * 存储账号 姓名 租户ID 角色代码
 */
@Data
public class UserContext {

    public static final String USER_CONTEXT_NAME = SysConstant.PARAM_PREFIX+ SP.DOT+"user-context-name";


    private String userName;

    private String userNameCn;

    private Long tenantId;

    private List<String> roleCodeList = new ArrayList<>();


    public boolean hasRole(String roleCode){
        if(roleCode == null) return true;
        return roleCodeList.contains(roleCode);
    }

}
