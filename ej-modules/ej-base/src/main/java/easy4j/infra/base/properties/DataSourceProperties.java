package easy4j.infra.base.properties;

import lombok.Data;

/**
 * 单个数据源的配置参数
 */
@Data
public class DataSourceProperties {
    private String url;
    private String username;
    private String password;

    /**
     * 为空会自动推断
     */
    private String driverClassName;
}