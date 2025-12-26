package easy4j.infra.base.properties;

import lombok.Data;

@Data
public class CodeGenProperties {
    /**
     * 是否启用代码生成页面
     */
    @SpringVs(desc = "是否启用代码生成页面")
    public boolean enable = true;
    /**
     * 是否启用代码生成页面鉴权
     */
    @SpringVs(desc = "是否启用代码生成页面鉴权")
    private boolean enableBasicAuth;
    /**
     * 用户名
     */
    @SpringVs(desc = "用户名")
    private String username;
    /**
     * 密码
     */
    @SpringVs(desc = "密码")
    private String password;
    /**
     * 是否跨域
     */
    @SpringVs(desc = "是否跨域")
    private boolean enableCrossOrigin;

}
