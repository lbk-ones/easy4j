package easy4j.module.mybatisplus;

import easy4j.infra.common.utils.SysConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(
        prefix = SysConstant.PARAM_PREFIX
)
@Data
public class MybatisPlusProperties {


    /**
     * 是否开启多租户模式
     */
    private boolean multiTenantEnabled = false;

    /**
     * 多租户的字段名称
     */
    private String tenantIdFieldName = "tenant_id";

    /**
     * 单租户的时候，默认租户ID的值
     */
    private Long defaultTenantId = 1L;

    private List<String> tenantIgnoreTables = new ArrayList<>();

}
