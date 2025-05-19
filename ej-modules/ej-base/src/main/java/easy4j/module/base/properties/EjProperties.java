package easy4j.module.base.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "ej"
)
@Data
public class EjProperties {

    private String env;

    /**
     * 签名密钥串(字典等敏感接口)
     */
    private String signatureSecret = "4002479ff39deef18b8c7eaf560d6c04";
    /**
     * 需要加强校验的接口清单
     */
    private String signUrls;

}
