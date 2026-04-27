```text

sca 架构下 一般都会考虑控制台，会自动引入@SentinelResource注解的拦截器，这个服务只能兼容@SentinelResource


如果不是sca 
实现方式：
我把官方的拦截器模块代码抄过来 进行流控降级规则全局初始化或者定向初始化

则可以使用：
1、@EnableFlowDegrade 这个注解是拦截标注了@FlowDegradeResource注解的资源
2、@EnableEasy4jSentinelResource 这个注解是拦截标注了原生注解@SentinelResource的
为什么要单独搞两个注解来，因为在不使用dashboard的时候很难去单独控制某一个资源的流控和降级规则 
这个@EnableFlowDegrade注解可以在在注解属性里面控制流控规则
这个@EnableEasy4jSentinelResource注解不能在注解属性里面控制流控规则 但是会设置一个全局的规则，这个规则可以随着spring属性而动态变更（如果是使用了配置中心的话）一改所有接口都改了

ps:如果使用了dashboard也不影响，通过注解设置的规则，通过dashboard改变规则则会被再次替换掉

如果使用了sca架构 那么这个模块不会被引入，所以它只会出现在单体服务架构中


```

