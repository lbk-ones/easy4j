package easy4j.module.nacosdubbo3;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "config")
public class ConfigProperties {


    private String configFile;
    private String appConfigFile;
    private String group;
    private String namespace;

    private String url;

}
