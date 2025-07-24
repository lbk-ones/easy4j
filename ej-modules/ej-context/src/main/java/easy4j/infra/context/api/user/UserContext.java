package easy4j.infra.context.api.user;

import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import lombok.Data;

@Data
public class UserContext {

    public static final String USER_CONTEXT_NAME = SysConstant.PARAM_PREFIX+ SP.DOT+"user-context-name";


    private String userCode;

    private String userName;

}
