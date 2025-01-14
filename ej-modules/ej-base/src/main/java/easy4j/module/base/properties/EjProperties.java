package easy4j.module.base.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(
        prefix = "ej.boot"
)
@Data
public class EjProperties {

    private String env;

}
