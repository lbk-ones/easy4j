package easy4j.module.base.properties;


import easy4j.module.base.annotations.Desc;
import easy4j.module.base.utils.SysConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 驼峰转短横线
 * 这个类的所有前缀都是 easy4j开头的
 * <p>
 * 这些参数有两种配置形式
 * 1、使用注解 注解只能配置一部分并不能完全配置
 * 2、使用配置文件
 * 3、远程配置的参数
 * <p>
 * 优先级如下：
 * 远程配置的参数 > 配置文件 > 注解
 * <p>
 * 如果两个意义一样的参数都在配置文件中存在那么 easy4j开头的参数优先级最高 如果只是配置了但是又没设置值 那还是以原来的配置为准
 */
@ConfigurationProperties(
        prefix = SysConstant.PARAM_PREFIX
)
@Data
public class EjSysProperties {
    /**
     * 数据源简写
     */
    @Desc("数据源简写，例如：“jdbc:postgresql://localhost:5432/postgres@root:123456”")
    private String dataSourceUrl;

    /**
     * 签名密钥串(字典等敏感接口)
     */
    @Desc("签名密钥串(字典等敏感接口)")
    private String signatureSecret = "33a9gkqjqnxiqnaoqnsdj187sa56h5a856adf5a8";

    /**
     * 需要加强校验的接口清单
     */
    @Desc("需要加强校验的接口清单")
    private String signUrls;


    /**
     * seed 模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10. 设置了ip前缀之后会按照ip来分配工作ID 分布式系统则不会主键重复
     */
    @Desc("seed模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10.“设置了ip前缀之后会按照ip来分配工作ID 分布式系统则不会主键重复”")
    private String seedIpSegment;


    /**
     * 是否开启全局允许跨域 默认true 但是可以关闭
     */
    @Desc("是否开启全局允许跨域 默认true 但是可以关闭")
    private String corsRejectEnable;

    /**
     * 是否启用h2数据库
     */
    @Desc("是否启用h2数据库")
    private boolean h2Enable;

    /**
     * h2 数据库地址
     */
    @Desc("h2 数据库地址")
    private String h2Url;

    /**
     * 系统负责人作者
     */
    @Desc("业务模块负责人")
    private String author;

    /**
     * 服务描述
     */
    @Desc("服务描述")
    private String serverDesc;

    /**
     * h2控制台用户名 默认 easy4j
     */
    @Desc("h2控制台用户名 默认 easy4j")
    private String h2ConsoleUsername;
    /**
     * H2 控制台密码 默认 easy4j
     */
    @Desc("H2 控制台密码 默认 easy4j")
    private String h2ConsolePassword;
    /**
     * 服务端口 默认8080 等同于server.port
     */
    @Desc("服务端口 默认8080 等同于server.port")
    private int serverPort;
    /**
     * 服务名称 等同于 spring.application.name
     */
    @Desc("服务名称 等同于 spring.application.name")
    private String serverName;

    /**
     * spring-application-name
     */
    @Desc("和spring-application-name效果一致")
    private int springApplicationName;


    /**
     * 是否启用sca 如果引用了 sca模块的starter那么这个默认是开启的
     */
    @Desc("是否启用sca 如果引用了 sca模块的starter那么这个默认是开启的")
    private boolean enableSca;

    /**
     * env 类似于 spring.profiles.active 用于在nacos远程配置文件名称加后缀
     */
    @Desc("env 类似于 spring.profiles.active 用于在nacos远程配置文件名称加后缀")
    private String env;

    /**
     * nacos 地址 如果有密码 可以 地址@username:password简写
     */
    @Desc("nacos 地址 如果有密码 可以 地址@username:password简写")
    private String nacosUrl;
    /**
     * nacos用户名
     */
    @Desc("nacos用户名")
    private String nacosUsername;
    /**
     * nacos用户密码
     */
    @Desc("nacos用户密码")
    private String nacosPassword;

    /**
     * nacos配置中心地址
     */
    @Desc("nacos配置中心地址")
    private String nacosConfigUrl;
    /**
     * nacos配置中心用户名
     */
    @Desc("nacos配置中心用户名")
    private String nacosConfigUsername;
    /**
     * nacos配置中心密码
     */
    @Desc("nacos配置中心密码")
    private String nacosConfigPassword;
    /**
     * nacos配置中心group
     */
    @Desc("nacos配置中心group")
    private String nacosConfigGroup;
    /**
     * nacos配置中心命名空间
     */
    @Desc("nacos配置中心命名空间")
    private String nacosConfigNamespace;
    /**
     * nacos 远程配置文件后缀默认为 properties
     */
    @Desc("nacos 远程配置文件后缀默认为：properties")
    private String nacosConfigFileExtension;
    /**
     * nacos 注册中心地址
     */
    @Desc("nacos 注册中心地址")
    private String nacosDiscoveryUrl;
    /**
     * nacos 注册中心用户名
     */
    @Desc("nacos 注册中心用户名")
    private String nacosDiscoveryUsername;
    /**
     * nacos 注册中心密码
     */
    @Desc("nacos 注册中心密码")
    private String nacosDiscoveryPassword;
    /**
     * nacos 注册中心group
     */
    @Desc("nacos 注册中心group")
    private String nacosDiscoveryGroup;
    /**
     * nacos 注册中心命名空间
     */
    @Desc("nacos 注册中心命名空间")
    private String nacosDiscoveryNamespace;

    /**
     * nacos配置中心data-ids
     */
    @Desc("nacos配置中心data-ids")
    private String dataIds;

    /**
     * nacos配置中心group
     */
    @Desc("nacos配置中心group，如果设置了这个 则配置中心和注册中心可以不用填group")
    private String nacosGroup;

    /**
     * nacos配置中心namespace
     */
    @Desc("nacos配置中心namespace，如果设置了这个 则配置中心和注册中心可以不用填namespace")
    private String nacosNameSpace;

}
