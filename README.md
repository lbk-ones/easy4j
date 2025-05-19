# Easy4J 框架底座

Easy4J 是一个轻量级、高性能的 Java 基础框架，旨在为企业级应用开发提供稳固的基础架构支持。框架遵循“约定优于配置”的原则，通过简单易用的
API 和丰富的扩展点，帮助开发者快速搭建高质量、易维护的应用系统。

## 支持

- **java 8**
- **springboot 2.7.18**
  目前只支持 java8 后面有计划支持 java 8+ 和 springboot 3+

## 特性

- **模块化设计**：采用模块化架构，各组件可独立使用或按需组合
- **简化开发**：提供常用工具类和基础服务，减少重复开发，极大极大减少配置量，可以0配置启动
- **统一异常处理**：标准化的异常处理机制，提升系统稳定性
- **统一国际化**：标准化国际i18n
- **集成常用组件**：内置日志、配置管理、工具类等基础功能
- **支持扩展**：提供丰富的扩展点，方便集成第三方组件
- **支持多种数据库**：MySql、Oracle、H2、SqlServer、DB2、PostgreSql

## 模块结构

Easy4J 框架包含以下核心模块：

- **ej-base**：核心模块，提供基础功能和框架核心组件（异常处理，i18n，启动类，代码生成基础组件，knife4j文档整合，底层数据库操作引擎，以及其他模块使用到的接口、抽象类）
- **ej-sca**：spring-cloud-alibaba 整合
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
- **ej-sadmin**：springboot-admin 客户端的依赖整合
- **ej-seed**：key的生成相关
- **ej-sentinel**：将sentinel和springboot整合起来
- **ej-sentinel-dubbo3**：sentinel和dubbos3的依赖整合模块
- **ej-starter**：starter模块整合
- **ej-starter/ej-dubbo3-mp-starter**：dubbo3体系starter
- **ej-starter/ej-jpa-boot-starter**：jpa体系starter
- **ej-starter/ej-spring-boot-starter**：springboot体系starter
- **ej-starter/ej-spring-nd-boot-starter**：springboot体系无数据源整合starter
- **ej-test**：starter的测试模块

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
        <version>2.7.16</version>
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
import easy4j.module.base.utils.ListTs;
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

