# 轻量级rpc调用框架
## 摆脱对dubbo、openfeign、grpc的依赖，自定义TCP应用层协议，以netty作为底层通讯，支持hession、kryo、jackson协议

### 服务发现

客户端从注册中心拿取主机信息，并监控主机信息变化更新本地缓存（√）

### 服务注册

服务提供者将自身元数据信息定时上报到注册中心去（√）
- 单服务注册（大粒度）（√）
- 自定义服务注册（细粒度）（√）
- 自定义handler调用(以SPI的形式注册，供客户端无接口API调用)（√）

### 服务调用

以动态代理的方式代理接口，然后组装请求体，发起网络通讯，并等待结果（√）
- 泛化调用（√）
- 广播调用（√）
- 直连调用（√）
- spring代理调用（√）

> ps: 直连地址配置方式 可以直接在 @RpcProxy注解的url属性配置，也可以直接在配置文件中配置 easy4j.rpc.reference.服务名称.url=127.0.0.1:9898 不用带协议schema

### 服务治理

- 热更新以下服务策略（√）
- 负载均衡：支持，轮询，加权轮询，随机，连接量，服务性能 这几种算法（√）
- 服务禁用（√）
- 权重变更（√）
- 熔断阈值
- 超时时间(ms)（√）
- 重试次数（√）
- 限流策略
- QPS限制

### 服务调用监控（指标暴露）

> - 总服务数 健康服务 异常服务 总调用数 平均响应时间 告警总数
> - 服务列表（服务名称	服务类型	服务状态	实例数  QPS	平均延迟	成功率 | CPU使用率，内存使用率，磁盘使用率，连接数，权重）
> - 实时监控（QPS，延迟，成功率，错误率）
> - 服务依赖（依赖的服务名称）
> - 调用链追踪
> - 告警信息（紧急，警告，信息）服务异常、调用延迟升高、服务恢复、QPS 突增


### 监控大盘

提供独立的监控页面

### Getting start (开始使用)

#### 提供者

```java

import easy4j.infra.rpc.integrated.spring.annotations.EnableEasy4jRpc;
import easy4j.infra.rpc.integrated.spring.annotations.RpcService;
import org.springframework.boot.SpringApplication;
import easy4j.infra.base.starter.Easy4JStarter;

@Easy4JStarter
@EnableEasy4jRpc
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RpcService("服务名称（可选如果写了值那么客户端@RpcProxy不用写服务名称）")
public interface IApiService {
    String hello(String name);
}
// RpcService 注解在接口和实现类添加都生效，实现类的优先级高于接口
// 如果他们的服务名称和该服务名称不一样那么会单独再注册一个服务到注册中心上去
// 不管是接口还是实现类添加了 这个类都会注册到springIOC容器里面去
@RpcService("一个单独的服务名称")
public class IApiServiceImpl implements IApiService {
    public String hello(String name){
        return "hello" + name + "!";
    }
}
```
#### 消费者

```java

import easy4j.infra.rpc.integrated.spring.annotations.RpcProxy;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    @RpcProxy("指定服务名称，可以不指定，具体看上面服务名称的规则")
    IApiService iApiService;

    public void hello() {
        String res = iApiService.hello("bk");
        System.out.println(res);
    }
}


```