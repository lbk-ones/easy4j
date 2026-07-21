# Changelog

所有版本的重要变更都会记录在此文件中。

# **1.0.0** - 2025-11-18

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


# **1.0.1** - 2025-11-18

### 新增
- 将依赖模块从modules模块抽离
- 新增可独立运行的RPC调用模块
- 优化权限session刷新流程
- 新增根据数据库名称、模式名称、表名和表类型检索表/视图信息的api接口
- 添加多数据源的功能
- 新增基于easy4j的全套代码生成功能，以及操作界面
- 新增处理servlet的mvc小框架
### 修复
- 修复quartzJob出现异常无限重试的问题
- 修复自动执行sql脚本sqlserver和postgresql找不到的问题
### 移除


# **1.0.1** - 2025-11-18

### 新增
- 将依赖模块从modules模块抽离
- 新增可独立运行的RPC调用模块
- 优化权限session刷新流程
- 新增根据数据库名称、模式名称、表名和表类型检索表/视图信息的api接口
- 添加多数据源的功能
- 新增基于easy4j的全套代码生成功能，以及操作界面
- 新增处理servlet的mvc小框架
### 修复
- 修复quartzJob出现异常无限重试的问题
- 修复自动执行sql脚本sqlserver和postgresql找不到的问题
### 移除

# **2.0.0** - 2025-11-18
### 新增
支持java17及其以上
支持springboot3.X
### 修复
### 移除
移除对java8的支持


# **2.1.0** - 2026-06-11
### ✨ 新增功能

本版本主要增强了配置中心、认证体系、缓存能力与代码生成体系：

#### 🧩 配置与配置中心
- 新增配置模块能力（config module）
- 支持额外配置文件解析（additional file parse）
- 支持 Spring Boot 配置属性文件扩展解析
- 新增配置中心整体能力增强（config module improve）
#### 🔐 认证与安全（SAuth / AccessToken）
- UserContext 扩展支持 roleCodeList
- Authentication 增加 userAgent、deviceId 参数
- AccessTokenAuthentication 重构 queryDbUser 方法
- 新增 Cookie 工具类（CookieUtil）及 Cookie 认证支持
- 默认增加 Sentinel fallback 实现
#### 🧠 缓存与中间件增强
- Redis 相关 fallback 机制优化（RedisCacheWithFallback）
- 新增延迟执行器（DelayExecutor）
- SAuth 缓存机制优化
- Redis / Sentinel 功能增强与扩展支持
#### ⚙️ 代码生成（CodeGen）
- codegen 页面优化与增强
- codegen 页面 npm 依赖升级
- codegen 与 dbaccess / sauth 联动重构优化
#### 🧱 RPC 与系统能力
- EJ-RPC 新增本地注入能力（local inject feature）
#### 📘 文档与兼容性
- 更新 README
- 支持 Spring Boot Admin 3.5.11 版本适配

### 🛠️ 修复问题（Fixes）

- 本版本修复了多个稳定性与兼容性问题：

#### 🔧 配置与中间件问题修复
- 修复 config center 模块问题
- 修复 Sentinel 控制台访问问题
- 修复 Redis server.port 等属性混淆问题
- 修复 MySQL codegen 相关问题
#### 🗄️ 数据库与 SQL
- 修复 datasource password 包含 @ 导致解析错误问题
- 修复 MyBatis Plus 版本与依赖问题
- 修复 ddl 大小写问题
#### 🔐 安全与认证
- 修复 sauth 多个 bug
- 修复 RPC proxy invoke 调用异常
- 修复从 1.0.1 → 2.0.1 合并遗留 bug
#### ⚙️ 配置与依赖问题
- 修复 pom 循环依赖问题
- 修复 Knife4j 不兼容问题及其他调整
### ⚡ 优化

- 本版本对多个核心模块进行了系统性优化：

#### 🔐 SAuth / 权限系统优化
- sauth 模块整体优化
- sauth 缓存机制优化
- auth 模块整体优化
#### 🧠 Redis & 缓存体系
- Redis fallback factory 清理规则优化
- RedisCacheWithFallback 性能与结构优化
#### ⚙️ CodeGen & 工具链
- codegen 页面优化
- sauth + codegen + dbaccess 重构优化
#### 🧩 系统架构优化
- config module 架构优化
- 整体模块结构与依赖关系优化

# **2.1.1** - 2026-06-15
- 文档补充
- 处理部分发现的高危漏洞依赖
- 调整内部脚本的执行顺序，调整到flyway迁移之前执行

# **2.1.2** - 2026-06-25
- 新增flyway多数据源迁移方案 @see easy4j.infra.flyway.Easy4jFlywayMigrationStrategy.dynamicDataSourceMigrate
- 代码生成功能中指定其他数据库地址之后同步刷新表集合
- 代码生成页面加入统一路径后缀修改
- 修复代码生成功能中指定controller包路径之后生成的package错误的问题
- 修复了db-access模块mysql无符号类型无法被识别的缺陷
- 修复代码生成页面修复MapperStruct类名修改，实现类代码中类名无法同步修改的BUG
- 新增使用文档 ej-starter/ej-spring-boot-starter/README.md
- ListTs添加递归循环工具函数doLoop
- 修复动态数据源在springboot3.x下不生效的问题
- 修复@RequestLog注解在boot3.x下无法记录请求体参数的问题
- 升级hutool版本
- 新增加解密和脱敏二合一模块ej-encryption **[`帮助文档`](./ej-modules/ej-encryption/README.md)**
- 新增ej-spring-cloud模块以及下面的5个子模块，子模块未集成到starter里面，只能按需引入模块
- 新增操作日志记录注解@OperateLog
- 添加ej-spring-cloud-pure-nacos用来无缝升级nacos版本

# **2.1.3** - 2026-07-17
- 完善ej-config-api对接spring-cloud-配置中心的能力
- 修复ej-spring-cloud-pure-nacos不支持@RefreshScope注解的问题
- 修复配置文件解析BUG

# **2.1.4** - 2026-07-21
- 完善接口日志写入逻辑，优化全局异常处理
- 重构ej-db-access模块的orm实现逻辑，弃用原先的DBAccess
- 其他优化若干