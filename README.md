# Easy4J 后端框架底座

Easy4J 是一个轻量级、高性能的 Java 基础框架，旨在为企业级应用开发提供稳固的基础架构支持。框架遵循“约定优于配置”的原则，通过简单易用的
API 和丰富的扩展点，帮助开发者快速搭建高质量、易维护的应用系统。

## 支持

- **java 8及其以上**
- **springboot 2.7.18**
  目前只支持 springboot2 后面有计划支持 springboot 3+

## 特性

- **简单**：配置简化且文档丰富，易于扩展易于维护易于使用
- **自带ORM框架**：摆脱对于mybatis，jpa,hiberate等orm框架的依赖（方便整合异构数据源和底层模块开发）支持MySql、Oracle、H2、SqlServer、DB2、PostgreSql
- **内置jpa和mybatis-plus等多种ORM框架**：上游服务可以有选择性的使用
- **rpc的整合**：同时整合dubbo3和SpringCloudAlibaba体系，可以有选择性的使用
- **多样性参数注入**：默认参数，系统参数，注解参数，远程参数多维度注入
- **集成化权限认证，授权**：有两种方式：1、基于Easy4j的权限认证授权，2、基于spring-security
- **模块化设计**：采用模块化架构，各组件可独立使用或按需组合，实现高内聚低耦合
- **简化开发**：提供常用工具类和基础服务，减少重复开发，极大极大减少配置量，可以0配置启动
- **任务调度**：封装xxl-job和quartz进行中心化调度或无中心化调度
- **统一异常处理**：标准化的异常处理机制，提升系统稳定性
- **统一国际化**：标准化国际i18n
- **统一api文档规范**：使用openapi3为规范，规范系统文档
- **统一上下文**：统一全局上下文和线程级上下文
- **统一拦截器整合**：日志拦截、幂等拦截、数据脱敏、流控、权限、本地消息表
- **集成常用组件**：数据库锁,redis,druid数据源,flyway,MapStruct,分布式主键生成,Json工具Xml工具各种工具类等基础功能
- **简单链路追踪**：可整合或切换成第三方分布式链路
- **单元测试**：使用单元测试来大大提升了底座迭代的容错性，规范化代码开发，同时整合H2，Mockito,Junit5为上游服务提供基础

## 模块结构

Easy4J 框架包含以下核心模块：

- **ej-base**：核心模块，提供基础功能和框架核心组件（启动类，代码生成基础组件，knife4j文档整合等）
- **ej-common**：通用模块工具等 （异常处理，i18n，返回体等）
- **ej-context**：全局上下文、全局接口
- **ej-db-access**：自实现数据库访问模块（支持多种主流数据库），包含单表orm框架，动态表查询实现，从模型或java实体中进行动态ddl语句生成
- **ej-log**：日志模块，包括数据库日志和接口日志
- **ej-lock**：分布式锁，各种分布式锁实现
- **ej-webmvc**：整合springmvc相关通用功能
- **ej-knife4j**：整合api文档相关
- **ej-sca**：spring-cloud-alibaba 整合
- **ej-sca-gateway**：spring-cloud 网关整合比较特殊新增一个
- **ej-sca-seata**：spring-cloud 整合seata
- **ej-quartz**：封装quartz,可以使用使用工具类手动调度，也可以以类的形式加上注解自动调度
- **ej-xxl-job**：xxljob整合
- **ej-datasource**：数据源模块 目前整合了Druid 和 Druid的监控页面
- **ej-dnspom**：dubbo dns 相关依赖整合
- **ej-dubbo3**：dubbo3整合（默认配置，异常，jaeger链路整合）
- **ej-flyway**：flyway整合
- **ej-h2**：H2数据库整合
- **ej-idempotent**：幂等操作模块
- **ej-jaeger**：jaeger分布式链路追踪整合
- **ej-jpa**：jpa整合，封装了大量的jpa相关东西
- **ej-logback**：整合logback（日志策略和配置参数）
- **ej-ltl-transactional**：本地消息表封装
- **ej-mapstruct**：mapstruct的通用封装
- **ej-mybatisplus**：整合mybatisplus
- **ej-nacos-dubbo3**：将dubbo3的注册中心整合到nacos去，并实现nacos作为配置中心
- **ej-redis**：redis整合模块
- **ej-sauth**：easy4j权限认证模块
- **ej-security**：整合spring-security
- **ej-sadmin**：springboot-admin 客户端的依赖整合
- **ej-sactuator**：actuator整合
- **ej-seed**：key的生成相关
- **ej-sentinel**：将sentinel和springboot整合起来
- **ej-sentinel-dubbo3**：sentinel和dubbos3的依赖整合模块
- **ej-starter**：starter模块整合
- **ej-starter/ej-dubbo3-mp-starter**：dubbo3体系starter
- **ej-starter/ej-jpa-boot-starter**：jpa体系starter
- **ej-starter/ej-spring-boot-starter**：springboot体系starter
- **ej-starter/ej-spring-nd-boot-starter**：springboot体系无数据源整合starter
- **ej-starter/sca-spring-boot-starter**：sca启动
- **ej-starter/sca-gateway-spring-boot-starter**：spring-cloud-reactive网关
- **ej-test**：starter的测试模块
- **ej-texample**：例子

## 快速开始

### 引入依赖

在您的 Maven 项目中根据自己的需求选择依赖添加：

```xml

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>xxx.xxx.xxx</groupId>
    <artifactId>xxx</artifactId>
    <packaging>jar</packaging>

    <name>xxx</name>
    <url>http://maven.apache.org</url>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>

        <!--按需选择-->
        <!--dubbo3 (dns体系) + mybatisplus 微服务-->
        <dependency>
            <artifactId>ej-dubbo3-mp-starter</artifactId>
            <version>1.0-SNAPSHOT</version>
            <packaging>jar</packaging>
        </dependency>

        <!--springboot封装 微服务(带数据源)-->
        <dependency>
            <groupId>easy4j.module.boot</groupId>
            <artifactId>ej-spring-boot-starter</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!--springboot封装 微服务(不带数据源)-->
        <dependency>
            <groupId>easy4j.module.boot</groupId>
            <artifactId>ej-spring-nd-boot-starter</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>

        <!--springboot jpa封装-->
        <dependency>
            <groupId>easy4j.module.boot</groupId>
            <artifactId>ej-jpa-boot-starter</artifactId>
            <version>1.0.0-SNAPSHOT</version>
        </dependency>
        <!--     注释掉上面不需要的注释      -->

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.30</version>
                        </path>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>1.5.5.Final</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

### 启动代码

```java

package ej.spring.boot.starter.test;
/**
 * 最简单的启动方式 可以使用配置文件去配置 也可以使用在启动注解里面写少量配置
 * @author bokun
 */

import easy4j.module.base.starter.Easy4JStarter;
import easy4j.module.sentinel.EnableFlowDegrade;
import org.springframework.boot.SpringApplication;

// @Easy4JNdStarterNd是无数据源注解
@Easy4JStarter(
        serverPort = 10001,// 服务端口
        serverName = "test-server",// 服务名称
        serviceDesc = "测试",// 服务解释
        author = "bk.li",// 服务作者
        enableH2 = false,// true 代表启动h2作为服务数据库 false h2Url不会生效 
        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false", // 使用h2当数据库
        ejDataSourceUrl = "jdbc:postgresql://localhost:5432/postgres@root:123456" // 数据库地址简略写法，其他东西easy4j会自动解析
)
@EnableFlowDegrade // 流控启动注解
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

### 测试模块代码

```java
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import easy4j.module.base.plugin.dbaccess.DBAccess;
import easy4j.module.base.plugin.dbaccess.DBAccessFactory;
import easy4j.module.base.plugin.dbaccess.domain.SysLogRecord;
import easy4j.module.base.starter.Easy4JStarter;
import easy4j.infra.common.utils.ListTs;
import easy4j.module.seed.CommonKey;
import easy4j.module.sentinel.EnableFlowDegrade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Easy4JStarter(
        serverPort = 10002,
        serverName = "build-server",
        serviceDesc = "测试服务",
        author = "bokun.li",
        enableH2 = true,
        h2Url = "jdbc:h2:file:~/h2/testdb;DB_CLOSE_ON_EXIT=false"
        // 使用h2当数据库
)
@EnableFlowDegrade
// 需要标识启动类
@SpringBootTest(classes = AppTest.class)
public class AppTest {

    @Autowired
    DataSource dataSource;

    // saveRecord
    @Test
    public void test1() throws SQLException {
        DBAccess dbAccess = DBAccessFactory.getDBAccess(dataSource);
        SysLogRecord sysLogRecord = new SysLogRecord();
        sysLogRecord.setId(CommonKey.gennerString());
        sysLogRecord.setRemark("this is the remark");
        sysLogRecord.setParams("this is the params");
        sysLogRecord.setTag("测试新增");
        sysLogRecord.setTagDesc("这是" + DateUtil.formatDate(new Date()) + "的测试用例");
        sysLogRecord.setStatus("1");
        sysLogRecord.setErrorInfo("cause by this is a error info list.....");
        sysLogRecord.setCreateDate(new Date());
        String s = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println("uuid---" + s);
        sysLogRecord.setTraceId(s);
        int i = dbAccess.saveOne(sysLogRecord, SysLogRecord.class);
        System.out.println("更新条数---" + i);
    }
}

```

> 启动服务可以不用application.properties


# git提交规则
使用 Conventional Commits 规则
```text
<类型>[可选作用域]: <描述>

[可选正文]

[可选脚注]
类型：
feat：新功能（会触发语义化版本的 MINOR 升级）
fix：修复 bug（会触发语义化版本的 PATCH 升级）
docs：仅修改文档（如 README、注释）
style：不影响代码逻辑的格式调整（如空格、缩进、标点）
refactor：既非新功能也非修复 bug 的代码重构
perf：性能优化（如算法改进、减少资源消耗）
test：添加或修改测试代码（如单元测试、集成测试）
build：构建流程或依赖管理的修改（如 pom.xml、package.json）
ci：CI 配置文件或脚本的修改（如 GitHub Actions、Jenkinsfile）
chore：其他不修改 src 或 test 目录的变更（如配置文件）
```

# 系统参数
- **easy4j.dev**: 是否是开发环境，如果是开发环境那么有些参数会降低提升应用启动速度
- **easy4j.author**: 业务模块负责人
- **easy4j.server-port**: 服务端口 默认8080 等同于server.port
- **easy4j.server-name**: 服务名称 等同于 spring.application.name
- **easy4j.server-desc**: 服务描述
- **easy4j.data-source-url**: 数据源简写，例如：“jdbc:postgresql://localhost:5432/postgres@root:123456”
- **easy4j.seed-ip-segment**: seed模块的雪花算法 ip前缀，用于多网卡确定ip的 例如 10.“设置了ip前缀之后会按照ip来分配工作ID 分布式系统则不会主键重复”
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
- **easy4j.session-refresh-time-remaining**: 会话刷新剩余时间，秒为单位，默认十分钟
- **easy4j.simple-link-tracking**: 单服务简单链路追踪，默认未开启，true为开启
- **easy4j.print-request-log**: 是否打印简单的请求日志，默认不打印，true为打印
- **easy4j.simple-auth-enable**: 简单权限认证 默认没开启 true为开启
- **easy4j.simple-auth-is-server**: 简单权限认证服务端，默认不是，如果是服务端那么会自动建表，自动注册服务暴露
- **easy4j.simple-auth-session-storage-type**: 权限session存储类型：db代表数据库，redis代表redis
- **easy4j.simple-auth-username**: 简单权限认证的用户名
- **easy4j.simple-auth-username-cn**: 简单权限认证的用户名中文
- **easy4j.simple-auth-password**: 简单权限认证的密码
- **easy4j.simple-auth-user-impl-type**: 用户信息的实现类型（default、extra）default代表默认实现（默认实现会自动建表），extra代表是外部业务实现，如果是extra则不建默认用户表：该字段无默认值如果开启了EASY4J_SAUTH_IS_SERVER那么必须设置
- **easy4j.simple-auth-is-cache-authority**: 简单权限是否缓存权限列表
- **easy4j.simple-auth-register-to-nacos**: 服务端是否将权限注册到nacos去远程调用
- **easy4j.simple-auth-scan-package-prefix**: 权限扫描包名，比如org.springframework这种前缀,只有处于这个包前缀的类才会被权限验证，默认是启动类所在包路径
- **easy4j.simple-auth-session-repeat-strategy**: 认证时会话重复策略,默认default也就是共用会话,new新建会话,reject不允许重复，public共用会话，kick把已存在的会话踢下线
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
- **easy4j.flyway-checksum-disabled**: 是否启用flyway启动时的内容检查，默认禁用，如果不禁用，已执行脚本更改过之后则启动失败
- **easy4j.sca-gateway-flow-qps**: spring-cloud-gateway 流控规则
- **easy4j.seata-enable**: 是否启用seata
- **easy4j.seata-nacos-url**: seata注册中心地址,地址(多个地址用逗号隔开)@用户:密码
- **easy4j.seata-nacos-cluster**: seata注册中心集群名称，通常和vgroup-mapping对应起来
- **easy4j.seata-tx-group**: seata事务组
- **easy4j.seata-nacos-group**: seata注册中心nacos组
- **easy4j.seata-registry-type**: seata注册中心类型
- **easy4j.seata-tx-log**: seata事务日志是否整合到logback（true代表整合false代表不整合），默认不整合
- **easy4j.xxl-job-enable**: 是否使用xxlJob
- **easy4j.xxl-job-admin-url**: xxlJobAdmin的地址
- **easy4j.xxl-job-access-token**: xxlJob的accessToken默认值为default_token
- **easy4j.sentinel-dashboard-enable**: 是否开启sentinel的控制台，默认不开启
- **easy4j.sentinel-dashboard-eager**:  （非必填）sentinel控制台是否提前初始化，默认如果启用控制台则提前初始化
- **easy4j.sentinel-dashboard-url**: sentinel控制台地址，示例（localhost:8080）
- **easy4j.metrics-enable**: 是否开启指标采集 默认开启
- **easy4j.default-i18n**: 默认i18n，默认中文
- **easy4j.db-access-not-cache-schema**: 是否不缓存动态表查询的schema信息，默认false也就是要缓存
- **easy4j.log-path**: 日志所在目录,默认程序运行当前目录logs文件夹下面
- **easy4j.minio-url**: minio地址
- **easy4j.minio-access-key**: minio访问key
- **easy4j.minio-secret-key**: minio访问密钥
- **easy4j.global-quartz-job-print-log**: quartz全局日志打印
- **easy4j.force-register-to-registry**: 强制将本机服务注册到注册中心,本机启动的服务默认不会注册到中心了
- **easy4j.quartz-job-restart-check-delete**: 针对quartz任务，如果任务被从代码层面删除，那么重启服务之后是否也停止调度，默认不停止调度
