package easy4j.infra.context.api.user;

import cn.hutool.core.util.StrUtil;
import easy4j.infra.common.utils.ListTs;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 给其他模块内部使用的
 * 存储账号 姓名 租户ID 角色代码
 */
@Data
public class UserContext {

    public static final String USER_CONTEXT_NAME = SysConstant.PARAM_PREFIX_DOT + "user-context-name";

    public static final String USER_NAME = SysConstant.PARAM_PREFIX_DASH + "user-name";
    public static final String USER_ID = SysConstant.PARAM_PREFIX_DASH + "user-id";
    public static final String USER_NAME_CN = SysConstant.PARAM_PREFIX_DASH + "user-name-cn";
    public static final String USER_NAME_EN = SysConstant.PARAM_PREFIX_DASH + "user-name-en";
    public static final String USER_NAME_NICK = SysConstant.PARAM_PREFIX_DASH + "user-name-nick";
    public static final String USER_TENANT_ID = SysConstant.PARAM_PREFIX_DASH + "user-tenant-id";
    public static final String USER_ORGAN_CODE = SysConstant.PARAM_PREFIX_DASH + "user-organ-code";
    public static final String USER_APP_CODE = SysConstant.PARAM_PREFIX_DASH + "user-app-code";
    public static final String ROLE_CODE_LIST = SysConstant.PARAM_PREFIX_DASH + "role-code-list";
    public static final String RESOURCE_CODE_LIST = SysConstant.PARAM_PREFIX_DASH + "resource-code-list";

    private static final List<String> KEYS = ListTs.asList(
            USER_NAME, USER_ID,
            USER_NAME_CN, USER_TENANT_ID,
            ROLE_CODE_LIST, RESOURCE_CODE_LIST,
            USER_ORGAN_CODE, USER_APP_CODE,
            USER_NAME_EN, USER_NAME_NICK
    );

    // 内部使用 是否为空
    private boolean isEmpty = false;

    // 是否来自网关
    private boolean isGateWay = false;

    // 账号
    private String userName;

    // 用户唯一且固定的ID
    private Long userId;

    // 用户中文
    private String userNameCn;

    // 用户英文
    private String userNameEn;

    // 用户昵称
    private String userNameNick;

    // 用户租户ID
    private Long tenantId;

    // 用户所属机构代码
    private String organCode;

    // 用户所属应用代码
    private String appCode;

    // 用户角色代码集合
    private List<String> roleCodeList = new ArrayList<>();

    // 用户权限代码
    private List<String> resourceCodeList = new ArrayList<>();


    public void set(String headerKey, String value) {
        if (value == null || headerKey == null) return;

        if (StrUtil.equals(headerKey, USER_NAME)) {
            this.setUserName(value);
        } else if (StrUtil.equals(headerKey, USER_ID)) {
            try {
                this.setUserId(Long.parseLong(value));
            } catch (Exception ignored) {
            }
        } else if (StrUtil.equals(headerKey, USER_NAME_CN)) {
            this.setUserNameCn(value);
        } else if (StrUtil.equals(headerKey, USER_TENANT_ID)) {
            try {
                this.setTenantId(Long.parseLong(value));
            } catch (Exception ignored) {
            }
        } else if (StrUtil.equals(headerKey, ROLE_CODE_LIST)) {
            try {
                this.setRoleCodeList(ListTs.splitToList(value, SP.COMMA));
            } catch (Exception ignored) {
            }
        } else if (StrUtil.equals(headerKey, RESOURCE_CODE_LIST)) {
            try {
                this.setResourceCodeList(ListTs.splitToList(value, SP.COMMA));
            } catch (Exception ignored) {
            }
        } else if (StrUtil.equals(headerKey, USER_ORGAN_CODE)) {
            this.setOrganCode(value);
        } else if (StrUtil.equals(headerKey, USER_APP_CODE)) {
            this.setAppCode(value);

        } else if (StrUtil.equals(headerKey, USER_NAME_EN)) {
            this.setUserNameEn(value);

        } else if (StrUtil.equals(headerKey, USER_NAME_NICK)) {
            this.setUserNameNick(value);

        }
    }

    public boolean hasRole(String roleCode) {
        if (roleCode == null) return true;
        return roleCodeList.contains(roleCode);
    }

    // 将网关带过来的消息往下传递
    public void keysVisitor(BiConsumer<String, String> consumer) {
        for (String id : KEYS) {
            if (StrUtil.equals(id, USER_NAME)) {
                String userName1 = this.getUserName();
                if (StrUtil.isNotBlank(userName1)) {
                    consumer.accept(id, userName1);
                }
            } else if (StrUtil.equals(id, USER_ID)) {
                Long userId1 = this.getUserId();
                if (userId1 != null) {
                    consumer.accept(id, String.valueOf(userId1));
                }
            } else if (StrUtil.equals(id, USER_NAME_CN)) {
                if (this.getUserNameCn() != null) {
                    consumer.accept(id, this.getUserNameCn());
                }
            } else if (StrUtil.equals(id, USER_TENANT_ID)) {
                if (this.getTenantId() != null) {
                    consumer.accept(id, String.valueOf(this.getTenantId()));
                }
            } else if (StrUtil.equals(id, ROLE_CODE_LIST)) {
                if (!this.getRoleCodeList().isEmpty()) {
                    consumer.accept(id, ListTs.join(SP.COMMA, this.getRoleCodeList()));
                }
            } else if (StrUtil.equals(id, RESOURCE_CODE_LIST)) {
                if (!this.getResourceCodeList().isEmpty()) {
                    consumer.accept(id, ListTs.join(SP.COMMA, this.getResourceCodeList()));
                }
            } else if (StrUtil.equals(id, USER_ORGAN_CODE)) {
                if (StrUtil.isNotBlank(this.getOrganCode())) {
                    consumer.accept(id, this.getOrganCode());
                }
            } else if (StrUtil.equals(id, USER_APP_CODE)) {
                if (StrUtil.isNotBlank(this.getAppCode())) {
                    consumer.accept(id, this.getAppCode());
                }
            } else if (StrUtil.equals(id, USER_NAME_EN)) {
                if (StrUtil.isNotBlank(this.getUserNameEn())) {
                    consumer.accept(id, this.getUserNameEn());
                }
            } else if (StrUtil.equals(id, USER_NAME_NICK)) {
                if (StrUtil.isNotBlank(this.getUserNameNick())) {
                    consumer.accept(id, this.getUserNameNick());
                }
            }
        }
    }

}
