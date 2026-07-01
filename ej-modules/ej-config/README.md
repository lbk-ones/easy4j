

**配置中心**

- ej-config-api：配置中心抽象模块
- ej-config-nacos-http：nacos配置中心的Open-Api调用实现
- ej-nacos-client-2.X：适配nacos2.x
- ej-nacos-client-3.X：适配nacos3.x

**如何使用？**

> 在使用easy4j架构的服务中引入坐标比如
```text
<dependency>
  <groupId>io.github.lbkones</groupId>
  <artifactId>ej-config-nacos-http</artifactId>
  <version>xxx</version>
</dependency>

<dependency>
  <groupId>io.github.lbkones</groupId>
  <artifactId>ej-nacos-client-2.X</artifactId>
  <version>xxx</version>
</dependency>

<dependency>
  <groupId>io.github.lbkones</groupId>
  <artifactId>ej-nacos-client-3.X</artifactId>
  <version>xxx</version>
</dependency>
```

**启动配置**

```shell
easy4j.server-name=test-service
# 地址 可以没有密码
easy4j.nacos-url=NACOS地址@用户名:密码
# namespace
easy4j.nacos-name-space=develop
# group
easy4j.nacos-group=dataspace-service
# dataids: 如果组和easy4j.nacos-group不一致 则可以单独写 easy4j.data-ids=xxx.properties@group2,xxx.properties@group3
easy4j.data-ids=common.properties,dataspace-auth.properties
```
**注意**

- 配置名称（也就是data-id）必须以配置类型结尾如果是properties就是xxx.properties,是yml就是xxx.yml
- 配置只能刷新spring环境中的值，并不具备springcloud类似的ResfreshScope的功能
- 所以配置的获取只能通过 Easy4j.getProperties("xxx") 或者 SpringUtil.getProperty("xxx") @Value和属性配置Bean注入的方式不会刷新
- 如果使用了sca架构那么这个不会生效，这个主要是给非sca架构的单体服务准备的