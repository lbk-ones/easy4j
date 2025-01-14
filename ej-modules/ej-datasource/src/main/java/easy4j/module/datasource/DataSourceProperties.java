package easy4j.module.datasource;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ej.datasource")
public class DataSourceProperties {

    private String url;

}
