> by bokun.li 可视化模块

## client
```text
原生java不引入spring相关，主要维护客户端配置，如果开启上报则必须配置collect的端口和地址默认127.0.0.1:12988
服务会启动一个指定的rpc服务端端口如果不指定会自动生成一个端口默认12987占用则自动寻址，同时这个端口会上报到collect。
这个模块主要负责埋点，采集，指标暴露，转换/适配，上报：http/rpc ,引入collect_api模块
```
## spring-client
```text
通过SPI的机制让spring的配置和client原生配置整合
```
## collect-api
```text
给client提供DTO模型和调用collect的rpc接口
```
## server-conf
```text
暴露各个服务端配置（collect、query、web）
collect、query、web模块全部引入这个模块
```
## collect
```text
实现ej_insight_API的接口并注入SPRING环境去，同时注入到注册中心
默认RPC端口为12988
如果单独嵌入该服务，则开启一个HTTP接口和收集上报数据的TCP接口
收集客户端上报或者主动查询来的数据，
也可以通过指标接口主动查询客户端或者rpc接口主动定向查询，
心跳健康检测，
接受客户端上报的信息入库（入库到数据库或者ES或者其他地方），
引入API和SERVER_CONF模块
和QUERY完全解耦通过API模块共享数据结构
```
## query
```text
如果单独嵌入该服务，则开启一个HTTP接口。
根据collect入库逻辑，实现对应查询采集信息的逻辑。
同时暴露门户后端自己的功能接口。
中间件监控信息采集
引入API、SERVER_CONF、QUERY_API模块
```
## query-api
```text
如果单独嵌入该服务，则开启一个HTTP接口。
根据collect入库逻辑，实现对应查询采集信息的逻辑。
同时暴露门户后端自己的功能接口。
中间件监控信息采集
引入API和SERVER_CONF模块
```
## insight-web
```text
如果不是ALL模块那么需要获取QUERY的RPC端口
只整合前端静态页面并引入QUERY_API服务，
然后调用QUERY服务的HTTP查询接口,引入API和SERVER_CONF模块
```
## all
```text
collect，query，web 不独立部署3合1，只提供一个上报的TCP端口和HTTP端口
```
## common
```text
公共工具模块
```