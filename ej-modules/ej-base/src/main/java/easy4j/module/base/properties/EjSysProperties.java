package easy4j.module.base.properties;


import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import easy4j.module.base.utils.SysConstant;
import jodd.util.StringPool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

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
     * 系统负责人作者
     */
    @SpringVs(desc = "业务模块负责人")
    private String author;

    /**
     * 服务端口 默认8080 等同于server.port
     */
    @SpringVs(
            desc = "服务端口 默认8080 等同于server.port",
            vs = SysConstant.SPRING_SERVER_PORT
    )
    private int serverPort;
    /**
     * 服务名称 等同于 spring.application.name
     */
    @SpringVs(
            desc = "服务名称 等同于 spring.application.name",
            vs = SysConstant.SPRING_SERVER_NAME
    )
    private String serverName;

    /**
     * 服务描述
     */
    @SpringVs(desc = "服务描述"
    )
    private String serverDesc;

    /**
     * 数据源简写
     */
    @SpringVs(
            desc = "数据源简写，例如：“jdbc:postgresql://localhost:5432/postgres@root:123456”",
            vs = SysConstant.DB_URL_STR
    )
    private String dataSourceUrl;


    /**
     * seed 模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10. 设置了ip前缀之后会按照ip来分配工作ID 分布式系统则不会主键重复
     */
    @SpringVs(desc = "seed模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10.“设置了ip前缀之后会按照ip来分配工作ID 分布式系统则不会主键重复”")
    private String seedIpSegment;


    /**
     * 是否开启全局允许跨域 默认true 但是可以关闭
     */
    @SpringVs(desc = "是否开启全局允许跨域 默认true 但是可以关闭")
    private String corsRejectEnable;

    /**
     * 是否启用h2数据库
     */
    @SpringVs(desc = "是否启用h2数据库"
    )
    private boolean h2Enable = false;

    /**
     * h2 数据库地址
     */
    @SpringVs(
            desc = "h2 数据库地址",
            vs = SysConstant.DB_URL_STR
    )
    private String h2Url = "jdbc:h2:mem:testdb@easy4j:easy4j";


    /**
     * h2控制台用户名 默认 easy4j
     */
    @SpringVs(
            desc = "h2控制台用户名 默认 easy4j",
            vs = SysConstant.DB_USER_NAME
    )
    private String h2ConsoleUsername = "easy4j";
    /**
     * H2 控制台密码 默认 easy4j
     */
    @SpringVs(
            desc = "H2 控制台密码 默认 easy4j",
            vs = SysConstant.DB_USER_PASSWORD
    )
    private String h2ConsolePassword = "easy4j";


    /**
     * 是否启用sca 如果引用了 sca模块的starter那么这个默认是开启的
     */
    @SpringVs(desc = "是否启用sca 如果引用了 sca模块的starter那么这个默认是开启的")
    private boolean enableSca;

    /**
     * env 类似于 spring.profiles.active 用于在nacos远程配置文件名称加后缀
     */
    @SpringVs(
            desc = "env 类似于 spring.profiles.active 用于在nacos远程配置文件名称加后缀",
            vs = ""
    )
    private String env;

    /**
     * nacos 地址 如果有密码 可以 地址@username:password简写
     */
    @SpringVs(
            desc = "nacos 地址 如果有密码 可以 地址@username:password简写",
            vs = SysConstant.SPRING_CLOUD_NACOS_URL
    )
    private String nacosUrl;
    /**
     * nacos用户名
     */
    @SpringVs(
            desc = "nacos用户名",
            vs = SysConstant.SPRING_CLOUD_NACOS_USERNAME
    )
    private String nacosUsername;
    /**
     * nacos用户密码
     */
    @SpringVs(
            desc = "nacos用户密码",
            vs = SysConstant.SPRING_CLOUD_NACOS_PASSWORD
    )
    private String nacosPassword;

    /**
     * nacos配置中心地址
     */
    @SpringVs(
            desc = "nacos配置中心地址",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_URL
    )
    private String nacosConfigUrl;
    /**
     * nacos配置中心用户名
     */
    @SpringVs(
            desc = "nacos配置中心用户名",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_USERNAME
    )
    private String nacosConfigUsername;
    /**
     * nacos配置中心密码
     */
    @SpringVs(
            desc = "nacos配置中心密码",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_PASSWORD
    )
    private String nacosConfigPassword;
    /**
     * nacos配置中心group
     */
    @SpringVs(
            desc = "nacos配置中心group",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_GROUP
    )
    private String nacosConfigGroup;
    /**
     * nacos配置中心命名空间
     */
    @SpringVs(
            desc = "nacos配置中心命名空间",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_NAMESPACE
    )
    private String nacosConfigNamespace;


    /**
     * nacos配置中心严格模式 默认为false 如果设置为 true 那么配置中心没有该data-id配置会报错
     */
    @SpringVs(
            desc = "nacos配置中心严格模式",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_NAMESPACE
    )
    private boolean nacosConfigStrict = false;

    /**
     * nacos 远程配置文件后缀默认为 properties
     */
    @SpringVs(
            desc = "nacos 远程配置文件后缀默认为：properties",
            vs = SysConstant.SPRING_CLOUD_NACOS_CONFIG_FILE_EXTENSION
    )
    private String nacosConfigFileExtension = "properties";
    /**
     * nacos 注册中心地址
     */
    @SpringVs(
            desc = "nacos 注册中心地址",
            vs = SysConstant.SPRING_CLOUD_NACOS_DISCOVERY_URL
    )
    private String nacosDiscoveryUrl;
    /**
     * nacos 注册中心用户名
     */
    @SpringVs(
            desc = "nacos 注册中心用户名",
            vs = SysConstant.SPRING_CLOUD_NACOS_DISCOVERY_USERNAME
    )
    private String nacosDiscoveryUsername;
    /**
     * nacos 注册中心密码
     */
    @SpringVs(
            desc = "nacos 注册中心密码",
            vs = SysConstant.SPRING_CLOUD_NACOS_DISCOVERY_PASSWORD
    )
    private String nacosDiscoveryPassword;
    /**
     * nacos 注册中心group
     */
    @SpringVs(
            desc = "nacos 注册中心group",
            vs = SysConstant.SPRING_CLOUD_NACOS_DISCOVERY_GROUP
    )
    private String nacosDiscoveryGroup;
    /**
     * nacos 注册中心命名空间
     */
    @SpringVs(
            desc = "nacos 注册中心命名空间",
            vs = SysConstant.SPRING_CLOUD_NACOS_DISCOVERY_NAMESPACE
    )
    private String nacosDiscoveryNamespace;


    /**
     * nacos配置中心data-ids 多个,逗号分割
     */
    @SpringVs(
            desc = "nacos配置中心data-ids 多个,逗号分割如果属于不同组那么就 data-id?group=XXX_GROUP"
    )
    private String dataIds;

    /**
     * nacos配置中心group
     */
    @SpringVs(
            desc = "nacos配置中心group，如果设置了这个 则配置中心和注册中心可以不用填group"
    )
    private String nacosGroup;

    /**
     * nacos配置中心namespace
     */
    @SpringVs(
            desc = "nacos配置中心namespace，如果设置了这个 则配置中心和注册中心可以不用填namespace"
    )
    private String nacosNameSpace;

    /**
     * 签名密钥串(字典等敏感接口)
     */
    @SpringVs(desc = "签名密钥串(字典等敏感接口)")
    private String signatureSecret = "33a9gkqjqnxiqnaoqnsdj187sa56h5a856adf5a8";

    /**
     * jwt签名密钥串
     */
    @SpringVs(desc = "jwt签名密钥串")
    private String jwtSecret = "31a9gquinkaywjlxuhtlailg7sa56h5a856adf7i38";

    /**
     * 需要加强校验的接口清单
     */
    @SpringVs(desc = "需要加强校验的接口清单")
    private String signUrls;

    /**
     * 简单权限认证
     */
    @SpringVs(desc = "简单权限认证 默认没开启 true为开启")
    private boolean simpleAuthEnable = false;

    /**
     * 会话过期时间
     */
    @SpringVs(desc = "会话过期时间 默认3个小时")
    private int sessionExpireTimeSeconds = 60 * 60 * 3;


    /**
     * 根据常量获取 对应的springboot变量
     *
     * @param constant
     * @return
     */
    public String[] getVs(String constant) {
        Field[] fields = ReflectUtil.getFields(this.getClass());
        for (Field field : fields) {
            String name = field.getName();
            String lowerCase = SysConstant.PARAM_PREFIX + StringPool.DOT + StrUtil.replace(
                    StrUtil.toUnderlineCase(name), StringPool.UNDERSCORE, StringPool.DASH
            ).toLowerCase();
            if (StrUtil.equals(constant, lowerCase)) {
                if (field.isAnnotationPresent(SpringVs.class)) {
                    SpringVs annotation = field.getAnnotation(SpringVs.class);
                    if (Objects.nonNull(annotation)) {
                        return annotation.vs();
                    }
                }
            }
        }
        return null;
    }

}
