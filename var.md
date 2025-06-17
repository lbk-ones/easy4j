# 系统参数解释

> - 系统参数按照命名规则来 短横线命名规则
> - 这些参数可以加在application.properties/yaml/yml中去 也可以从nacos和apollo等远程配置中心去获取
> - 拿取以easy4j.开头的系统参数统一使用Easy4j.getProperty("xxx") 或者 Easy4j.getEjSysProperties()
    来拿取，比如获取数据源地址：使用Easy4j.getEjSysProperties().getDataSourceUrl()
>

- **easy4j.dev**: 是否是开发环境，如果是开发环境那么有些参数会降低提升应用启动速度
- **easy4j.author**: 业务模块负责人
- **easy4j.server-port**: 服务端口 默认8080 等同于server.port
- **easy4j.server-name**: 服务名称 等同于 spring.application.name
- **easy4j.server-desc**: 服务描述
- **easy4j.data-source-url**: 数据源简写，例如：“jdbc:postgresql://localhost:5432/postgres@root:123456”
- **easy4j.seed-ip-segment**: seed模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10.“设置了ip前缀之后会按照ip来分配工作ID
  分布式系统则不会主键重复”
- **easy4j.cors-reject-enable**: 是否开启全局允许跨域 默认true 但是可以关闭
- **easy4j.h2-enable**: 是否启用h2数据库
- **easy4j.h2-url**: h2 数据库地址
- **easy4j.h2-console-username**: h2控制台用户名 默认 easy4j
- **easy4j.h2-console-password**: H2 控制台密码 默认 easy4j
- **easy4j.enable-sca**: 是否启用sca 如果引用了 sca模块的starter那么这个默认是开启的
- **easy4j.env**: env 类似于 spring.profiles.active 用于在nacos远程配置文件名称加后缀
- **easy4j.nacos-url**: nacos 地址 如果有密码 可以 地址@username:password简写
- **easy4j.nacos-username**: nacos用户名
- **easy4j.nacos-password**: nacos用户密码
- **easy4j.nacos-config-url**: nacos配置中心地址
- **easy4j.nacos-config-username**: nacos配置中心用户名
- **easy4j.nacos-config-password**: nacos配置中心密码
- **easy4j.nacos-config-group**: nacos配置中心group
- **easy4j.nacos-config-namespace**: nacos配置中心命名空间
- **easy4j.nacos-config-strict**: nacos配置中心严格模式
- **easy4j.nacos-config-file-extension**: nacos 远程配置文件后缀默认为：properties
- **easy4j.nacos-discovery-url**: nacos 注册中心地址
- **easy4j.nacos-discovery-username**: nacos 注册中心用户名
- **easy4j.nacos-discovery-password**: nacos 注册中心密码
- **easy4j.nacos-discovery-group**: nacos 注册中心group
- **easy4j.nacos-discovery-namespace**: nacos 注册中心命名空间
- **easy4j.data-ids**: nacos配置中心data-ids 多个,逗号分割如果属于不同组那么就 data-id?group=XXX_GROUP
- **easy4j.nacos-group**: nacos配置中心group，如果设置了这个 则配置中心和注册中心可以不用填group
- **easy4j.nacos-name-space**: nacos配置中心namespace，如果设置了这个 则配置中心和注册中心可以不用填namespace
- **easy4j.signature-secret**: 签名密钥串(字典等敏感接口)
- **easy4j.jwt-secret**: jwt签名密钥串
- **easy4j.sign-urls**: 需要加强校验的接口清单
- **easy4j.session-expire-time-seconds**: 会话过期时间 默认3个小时
- **easy4j.simple-link-tracking**: 单服务简单链路追踪，默认未开启，true为开启
- **easy4j.print-request-log**: 是否打印简单的请求日志，默认不打印，true为打印
- **easy4j.simple-auth-session-storage-type**: 权限session存储类型：db代表数据库，redis代表redis
- **easy4j.simple-auth-enable**: 简单权限认证 默认没开启 true为开启
- **easy4j.simple-auth-username**: 简单权限认证的用户名
- **easy4j.simple-auth-username-cn**: 简单权限认证的用户名中文
- **easy4j.simple-auth-password**: 简单权限认证的密码
- **easy4j.db-request-log-enable**: 是否启用RequestLog注解进行请求日志收集 默认启用false关闭
- **easy4j.enable-print-sys-db-sql**: 是否开启系统sql日志记录 true 代表开启，默认开启
- **easy4j.cache-http-content-length**: 请求体缓存字节流最大大小，默认5M
- **easy4j.admin-server-url**: BootAdmin监控地址,配置了代表自动开启admin-client
- **easy4j.redis-server-url**: redis server 地址 127.0.0.1:6379@user:123456 用户名如果省略第一位就是密码
- **easy4j.redis-connection-type**: Redis连接方式: Single、Sentinel、Cluster 默认单点
- **easy4j.redis-enable**: 是否启用redis 如果配置了 redis-server-url 那么这个自动变成true
- **easy4j.redis-min-ide-size**: redis最小空闲连接数 默认30
- **easy4j.redis-connection-pool-size**: redis连接池最大连接数量 默认500
- **easy4j.flyway-enable**: 是否启用flyway默认没启动，但是如果在linux服务器上默认是启用了的，开发环境需要置为true才会生效
- **easy4j.sca-gateway-flow-qps**: spring-cloud-gateway 流控规则