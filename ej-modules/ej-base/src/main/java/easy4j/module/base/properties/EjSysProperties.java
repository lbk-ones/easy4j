package easy4j.module.base.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
// 驼峰转短横线
@ConfigurationProperties(
        prefix = "easy4j"
)
@Data
public class EjSysProperties {
    /**
     * 数据源简写
     */
    private String dataSourceUrl;

    /**
     * 签名密钥串(字典等敏感接口)
     */
    private String signatureSecret = "33a9gkqjqnxiqnaoqnsdj187sa56h5a856adf5a8";

    /**
     * 需要加强校验的接口清单
     */
    private String signUrls;


    /**
     * seed 模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10. 设置了ip前缀之后会按照ip来分配工作ID 分布式系统则不会主键重复
     */
    private String seedIpSegment;


    /**
     * 是否开启全局跨域 默认true 但是可以关闭
     */
    private String corsRejectEnable;

    private boolean h2Enable;

    private String h2Url;

    /**
     * 系统负责人作者
     */
    private String author;

    /**
     * 服务描述
     */
    private String serviceDesc;
    /**
     * h2控制台用户名 默认 easy4j
     */
    private String h2ConsoleUsername;
    /**
     * H2 控制台密码 默认 easy4j
     */
    private String h2ConsolePassword;

}
