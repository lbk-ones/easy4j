# 系统参数解释
> 系统参数按照命名规则来 短横线命名规则
> 这些参数可以加在application.properties/yaml/yml中去 也可以从nacos和apollo等远程配置中心去获取

- **easy4j.data-source-url**: 数据源简写（url+用户名+密码） 例如 ej.datasource.url=jdbc:postgresql://localhost:5432/postgres@root:123456
- **easy4j.seed-ip-segment**: 雪花算法的ip前缀，用于多网卡确定ip的 例如 10.
- **easy4j.cors-reject-enable**: 是否开启全局跨域 默认true 但是可以关闭