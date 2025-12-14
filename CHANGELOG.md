# Changelog

所有版本的重要变更都会记录在此文件中。

## [1.0.0] - 2025-11-18

### 新增

- 新增 ej-spring-boot-starter 传统带数据源单服务模块
- 新增 ej-spring-nd-boot-starter 传统不带数据源单服务模块
- 新增 jpa-spring-boot-starter 传统以jpa为核心的单服务模块
- 新增 dubbo3-spring-boot-starter 微服务以dubbo3+nacos+sentinel为核心的dns体系
- 新增 knife4j-nacos-aggregation-starter knife4j通过nacos聚合微服务文档模块
- 新增 sca-gateway-spring-boot-starter springcloudgateway网关模块
- 新增 sca-spring-boot-starter 以springcloudalibaba为体系的微服务启动包
- 支持 JDK 8及其以上兼容性
- 支持 springboot 2.X
- 新增几十个模块，详情请看 README.md
- 新增系统配置详情请看 var.md
- 新增部分工具类如下

```text
easy4j.infra.base.starter.env.Easy4j            系统参数或者配置中心远程参数获取
easy4j.module.sauth.core.Easy4jAuth             系统权限相关操作，认证、鉴权、获取用户信息等
easy4j.infra.dbaccess.dialect.v2.DialectV2      不同数据库方言相关内容获取
easy4j.infra.dbaccess.dynamic.dll.op.DynamicDDL 动态DDL工具（建表，逆向相关）
easy4j.infra.dbaccess.dynamic.DynamicTableQuery 动态表查询
easy4j.infra.dbaccess.DBAccess                  摆脱上层orm框架，直连其他数据库的orm框架
easy4j.infra.dbaccess.TempDataSource            临时数据源实现，每次都拿取连接
easy4j.infra.common.utils.ThreadPoolUtils       线程池工具
easy4j.infra.common.utils.SP                    字符串常量比如逗号句号什么的
easy4j.infra.common.utils.ServiceLoaderUtils    java拿取SPI工具集合
easy4j.infra.common.utils.ListTs                集合工具集
easy4j.infra.common.utils.EasyMap               Map工具
easy4j.infra.common.utils.BusCode               系统使用的i18n字段
easy4j.infra.common.utils.lambda.EasyLambda     lambda工具集
easy4j.infra.common.utils.json.JacksonUtil      json工具类
easy4j.infra.common.utils.xml.JacksonXmlUtil    xml工具类（之前代码里面的xml工具全部废掉，用这个）
easy4j.infra.common.utils.ObjectHolder          单例类
easy4j.infra.common.header.EasyResult           通用返回实体（带泛型）
easy4j.infra.common.i18n.I18nUtils              i18n工具集
easy4j.infra.common.header.CheckUtils           参数检查工具
easy4j.infra.common.exception.EasyException     通用异常（所有业务异常都抛它i18n也是一样）
easy4j.infra.common.utils.EStopWatch            步进器（StopWatch的改良版本）
easy4j.infra.log.DbLog                          数据库日志工具
easy4j.module.mapstruct.TransferMapper          如果MapStruct不使用 ConversionService 那么就用这个
easy4j.module.redis.RedisCacheWithFallback      redis缓存查询降级
easy4j.module.seed.CommonKey                    雪花算法主键生成器
easy4j.module.mybatisplus.IdGenner              mybatisplus版本的主键生成
easy4j.module.seed.leaf.LeafGenIdService        leaf主键生成器
easy4j.infra.context.api.lock.RedissonLock      redis分布式锁（如果使用了redis的话）
easy4j.infra.context.api.lock.DbLock            数据库分布式锁
easy4j.infra.context.Easy4jContext              上下文获取工具
cn.hutool.extra.spring.SpringUtil               通过代码的形式拿取bean
cn.hutool.core.convert.Convert                  类型转换
cn.hutool.core.date.DateUtil                    时间工具类
cn.hutool.core.util.StrUtil                     字符串处理
easy4j.infra.common.utils.minio.EasyMinio       MinIo工具类
easy4j.infra.common.utils.EasyExcelUtils        Excel工具类
easy4j.infra.common.utils.MonitoredBlockingQueue 阻塞队列监控
easy4j.infra.common.utils.OSUtils               系统信息获取工具类
easy4j.infra.common.utils.ServiceLoaderUtils    SPI缓存工具类
org.springframework.cache.CacheManager          缓存管理器如果要使用缓存就用这个（有Redis和Caffeine版本）
```

### 修复


### 移除


## [1.0.1] - 2025-11-18

### 新增
- 将依赖模块从modules模块抽离
- 新增可独立运行的RPC调用模块
- 优化权限session刷新流程
- 新增根据数据库名称、模式名称、表名和表类型检索表/视图信息的api接口

### 修复
- 修复quartzJob出现异常无限重试的问题
- 修复自动执行sql脚本sqlserver和postgresql找不到的问题
### 移除