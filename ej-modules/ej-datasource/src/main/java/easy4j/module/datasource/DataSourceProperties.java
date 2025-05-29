package easy4j.module.datasource;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DataSourceProperties
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
@ConfigurationProperties(prefix = "ej.datasource")
public class DataSourceProperties {

    private String url;

}