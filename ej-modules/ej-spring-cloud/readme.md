> - spring cloud 和spring cloud alibaba 的相关东西
> - 按需引入模块，不整合到starter里面去，主要是为了控制版本，和简化配置
## **模块详解**
- ej-spring-cloud-config-nacos 只使用nacos当做配置中心 目前这个模块只支持nacos3.x 如果需要对接nacos2.x 需要使用ej-config模块下面的针对性处理方案
- ej-spring-cloud-openfeign 如果只需要openfeign则引入这个模块
- ej-spring-cloud-registry-nacos 如果需要注册中心则引入这个模块进行服务注册和服务发现
- ej-spring-cloud-sentinel 引入这个模块则代表需要使用WEB、OPENFEIGN、RESOURCE级别的流控降级
- ej-spring-cloud-seata seata相关的集成以及一些工具类