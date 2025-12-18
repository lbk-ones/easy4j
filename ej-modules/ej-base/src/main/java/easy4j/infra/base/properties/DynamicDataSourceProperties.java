package easy4j.infra.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 动态数据源总配置
 */
@Data
public class DynamicDataSourceProperties {

    /**
     * 是否启用，如果不启用则禁用
     */
    private boolean enabled = false;


    /**
     * 所有数据源配置（Map 结构：key=数据源标识，value=数据源参数）
     */
    private Map<String, DataSourceProperties> datasource;
}