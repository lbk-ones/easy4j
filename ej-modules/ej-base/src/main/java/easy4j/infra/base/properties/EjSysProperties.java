/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.base.properties;


import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import easy4j.infra.base.starter.env.Easy4j;
import easy4j.infra.common.utils.SP;
import easy4j.infra.common.utils.SysConstant;
import jodd.util.StringPool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

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
/**
 * EjSysProperties
 *
 * @author bokun.li
 * @date 2025-05
 */
@Data
public class EjSysProperties {

    /**
     * 是否是开发环境，如果是开发环境那么有些参数会降低提升应用启动速度
     */
    @SpringVs(desc = "是否是开发环境，如果是开发环境那么有些参数会降低提升应用启动速度")
    private boolean dev = false;

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
    private String signUrls = "";


    /**
     * 会话过期时间
     */
    @SpringVs(desc = "会话过期时间 默认3个小时")
    private int sessionExpireTimeSeconds = 60 * 60 * 3;

    /**
     * 会话刷新剩余时间，秒为单位
     */
    @SpringVs(desc = "会话刷新剩余时间，秒为单位，默认十分钟")
    private int sessionRefreshTimeRemaining = 60 * 10;

    /**
     * 单服务简单链路追踪
     */
    @SpringVs(desc = "单服务简单链路追踪，默认未开启，true为开启")
    private boolean simpleLinkTracking = false;

    /**
     * 是否打印简单的请求日志，默认不打印，true为打印
     */
    @SpringVs(desc = "是否打印简单的请求日志，默认不打印，true为打印")
    private boolean printRequestLog = false;

    /**
     * 简单权限认证
     */
    @SpringVs(desc = "简单权限认证 默认没开启 true为开启")
    private boolean simpleAuthEnable = false;

    /**
     * "简单权限认证服务端，默认不是，如果是服务端那么会自动建表，自动注册服务暴露"
     */
    @SpringVs(desc = "简单权限认证服务端，默认不是，如果是服务端那么会自动建表，自动注册服务暴露")
    private boolean simpleAuthIsServer = false;

    /**
     * 权限session存储类型：db代表数据库，redis代表redis
     */
    @SpringVs(desc = "权限session存储类型：db代表数据库，redis代表redis")
    private String simpleAuthSessionStorageType = "db";


    /**
     * 简单权限认证的用户名
     */
    @SpringVs(desc = "简单权限认证的用户名")
    private String simpleAuthUsername;

    /**
     * 简单权限认证的用户名中文
     */
    @SpringVs(desc = "简单权限认证的用户名中文")
    private String simpleAuthUsernameCn;

    /**
     * 简单权限认证的密码
     */
    @SpringVs(desc = "简单权限认证的密码")
    private String simpleAuthPassword;
    /**
     * 用户信息的实现类型（default、extra）default代表默认实现（默认实现会自动建表），extra代表是外部业务实现，如果是extra则不建默认用户表：该字段无默认值如果开启了EASY4J_SAUTH_IS_SERVER那么必须设置
     */
    @SpringVs(
            valueEnums = {"default", "extra"},
            desc = "用户信息的实现类型（default、extra）default代表默认实现（默认实现会自动建表），extra代表是外部业务实现，如果是extra则不建默认用户表：该字段无默认值如果开启了EASY4J_SAUTH_IS_SERVER那么必须设置"
    )
    private String simpleAuthUserImplType;

    /**
     * 简单权限是否缓存权限列表
     */
    @SpringVs(
            valueEnums = {"true", "false"},
            desc = "简单权限是否缓存权限列表"
    )
    private boolean simpleAuthIsCacheAuthority = false;

    /**
     * 是否将权限注册到nacos去远程调用
     */
    @SpringVs(
            valueEnums = {"true", "false"},
            desc = "服务端是否将权限注册到nacos去远程调用"
    )
    private boolean simpleAuthRegisterToNacos = true;

    /**
     * 权限扫描包名，比如org.springframework这种前缀
     */
    @SpringVs(
            desc = "权限扫描包名，比如org.springframework这种前缀,只有处于这个包前缀的类才会被权限验证，默认是启动类所在包路径"
    )
    private String simpleAuthScanPackagePrefix;

    /**
     * 认证时会话重复策略,默认default也就是共用会话,new新建会话,reject不允许重复，public共用会话，kick把已存在的会话踢下线
     */
    @SpringVs(
            valueEnums = {"default","new","reject","public","kick"},
            desc = "认证时会话重复策略,默认default也就是共用会话,new新建会话,reject不允许重复，public共用会话，kick把已存在的会话踢下线"
    )
    private String simpleAuthSessionRepeatStrategy = "default";

    /**
     * 是否启用RequestLog注解进行请求日志收集 true代表开启
     */
    @SpringVs(desc = "是否启用RequestLog注解进行请求日志收集 默认启用false关闭")
    private boolean dbRequestLogEnable = true;

    /**
     * 是否开启系统sql日志记录 true 代表开启，默认开启
     */
    @SpringVs(desc = "是否开启系统sql日志记录 true 代表开启，默认开启")
    private boolean enablePrintSysDbSql = true;


    /**
     * 请求体缓存字节流最大大小，默认5M
     */
    @SpringVs(desc = "请求体缓存字节流最大大小，默认5M")
    private int cacheHttpContentLength = 5 * 1024 * 1024;

    /**
     * BootAdminServer地址
     */
    @SpringVs(desc = "BootAdmin监控地址,配置了代表自动开启admin-client", vs = {
            "spring.boot.admin.client.url"
    })
    private String adminServerUrl;


    /**
     * BootAdminServer地址
     */
    @SpringVs(desc = "redis server 地址 127.0.0.1:6379@user:123456 用户名如果省略第一位就是密码")
    private String redisServerUrl = "";

    /**
     * Redis连接方式: Single、Sentinel、Cluster
     */
    @SpringVs(desc = "Redis连接方式: Single、Sentinel、Cluster 默认单点")
    private String redisConnectionType = "Single";

    /**
     * Redis连接方式: Single、Sentinel、Cluster
     */
//    @SpringVs(desc = "Redis连接池类型: LETTUCE、JEDIS", vs = {
//            "spring.redis.client-type"
//    })
//    private String redisPoolType = "LETTUCE";

    /**
     * 是否启用redis 如果配置了 redis-server-url 那么这个自动变成true
     */
    @SpringVs(desc = "是否启用redis 如果配置了 redis-server-url 那么这个自动变成true")
    private boolean redisEnable = false;

    /**
     * redis最小空闲连接数 默认30
     */
    @SpringVs(desc = "redis最小空闲连接数 默认30")
    private int redisMinIdeSize = 30;

    /**
     * redis连接池最大连接数量 默认500
     */
    @SpringVs(desc = "redis连接池最大连接数量 默认500")
    private int redisConnectionPoolSize = 500;

    /**
     * 是否启用flyway
     */
    @SpringVs(desc = "是否启用flyway默认没启动，但是如果在linux服务器上默认是启用了的，开发环境需要置为true才会生效")
    private boolean flywayEnable = false;

    /**
     * 是否启用flyway
     */
    @SpringVs(desc = "是否启用flyway启动时的内容检查，默认禁用，如果不禁用，已执行脚本更改过之后则启动失败")
    private boolean flywayChecksumDisabled = true;

    /**
     * 是否启用flyway
     */
    @SpringVs(desc = "spring-cloud-gateway 流控规则")
    private int scaGatewayFlowQps = 400;

    /**
     * 是否启用seata
     */
    @SpringVs(desc = "是否启用seata", vs = "seata.enabled")
    private boolean seataEnable = false;

    /**
     * seata注册中心地址,地址(多个地址用逗号隔开)@用户:密码
     */
    @SpringVs(desc = "seata注册中心地址,地址(多个地址用逗号隔开)@用户:密码")
    private String seataNacosUrl;

    @SpringVs(desc = "seata注册中心集群名称，通常和vgroup-mapping对应起来", vs = "seata.registry.nacos.cluster")
    private String seataNacosCluster = "default";


    /**
     * seata事务组
     */
    @SpringVs(desc = "seata事务组", vs = "seata.tx-service-group")
    private String seataTxGroup = "default_tx_group";


    /**
     * seata注册中心nacos组
     */
    @SpringVs(desc = "seata注册中心nacos组", vs = "seata.registry.nacos.group")
    private String seataNacosGroup = "SEATA_GROUP";

    /**
     * seata注册中心类型
     */
    @SpringVs(desc = "seata注册中心类型", vs = "seata.registry.type")
    private String seataRegistryType;

    /**
     * seata事务日志是否整合到logback，默认整合
     */
    @SpringVs(desc = "seata事务日志是否整合到logback（true代表整合false代表不整合），默认不整合")
    private boolean seataTxLog = false;

    /**
     * 是否使用xxlJob
     */
    @SpringVs(desc = "是否使用xxlJob")
    private boolean xxlJobEnable = false;

    /**
     * xxlJobAdmin的地址
     */
    @SpringVs(desc = "xxlJobAdmin的地址")
    private String xxlJobAdminUrl;

    /**
     * xxlJob的accessToken默认值为default_token
     */
    @SpringVs(desc = "xxlJob的accessToken默认值为default_token")
    private String xxlJobAccessToken = "default_token";

    /**
     * 是否开启sentinel的控制台，默认不开启
     */
    @SpringVs(desc = "是否开启sentinel的控制台，默认不开启")
    private boolean sentinelDashboardEnable = false;

    /**
     * sentinel控制台是否提前初始化，默认如果启用则提前初始化
     */
    @SpringVs(desc = " （非必填）sentinel控制台是否提前初始化，默认如果启用控制台则提前初始化")
    private boolean sentinelDashboardEager = true;

    /**
     * sentinel控制台地址，示例（localhost:8080）
     */
    @SpringVs(desc = "sentinel控制台地址，示例（localhost:8080）", vs = "spring.cloud.sentinel.transport.dashboard")
    private String sentinelDashboardUrl;

    /**
     * 是否开启指标采集 默认开启
     */
    @SpringVs(desc = "是否开启指标采集 默认开启")
    private boolean metricsEnable;


    /**
     * 默认i18n,默认中文
     */
    @SpringVs(desc = "默认i18n")
    private String defaultI18n = "zh_CN";


    /**
     * 根据常量获取 对应的springboot变量
     *
     * @param constant
     * @return
     */
    public String[] getVs(String constant) {
        return getVsWith(ReflectUtil.getFields(this.getClass()), constant);
    }

    public static String[] getStaticVs(String constant) {
        return getVsWith(ReflectUtil.getFields(EjSysProperties.class), constant);
    }

    private static String[] getVsWith(Field[] Fields, String constant) {
        for (Field field : Fields) {
            String name = field.getName();
            String lowerCase = SysConstant.PARAM_PREFIX + StringPool.DOT + StrUtil.replace(
                    StrUtil.toUnderlineCase(name), StringPool.UNDERSCORE, StringPool.DASH
            ).toLowerCase();
            if (StrUtil.equals(constant, lowerCase)) {
                if (field.isAnnotationPresent(SpringVs.class)) {
                    SpringVs annotation = field.getAnnotation(SpringVs.class);
                    return Optional.ofNullable(annotation).map(SpringVs::vs).orElse(null);
                }
            }
        }
        return null;
    }

    public static SpringVs getSpringVs(String sysName) {
        Field[] fields = ReflectUtil.getFields(EjSysProperties.class);
        for (Field field : fields) {
            String name = field.getName();
            String lowerCase = SysConstant.PARAM_PREFIX + StringPool.DOT + StrUtil.replace(
                    StrUtil.toUnderlineCase(name), StringPool.UNDERSCORE, StringPool.DASH
            ).toLowerCase();
            if (StrUtil.equals(sysName, lowerCase) || StrUtil.equals(StrUtil.replaceFirst(sysName, SysConstant.PARAM_PREFIX + SP.DOT, ""), name)) {
                if (field.isAnnotationPresent(SpringVs.class)) {
                    return field.getAnnotation(SpringVs.class);
                }
            }
        }
        return null;
    }


    public Map<String, Object> getBeanMap() {
        Map<String, Object> objectMap = Maps.newHashMap();
        Class<? extends EjSysProperties> aClass = this.getClass();
        Field[] fields = ReflectUtil.getFields(aClass);
        for (Field field : fields) {
            String ejSysPropertyName = Easy4j.getEjSysPropertyName(field);
            Object fieldValue = ReflectUtil.getFieldValue(this, field);
            objectMap.put(ejSysPropertyName, fieldValue);
        }
        return objectMap;
    }

}
