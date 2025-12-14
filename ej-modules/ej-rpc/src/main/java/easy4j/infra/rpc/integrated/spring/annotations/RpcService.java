/**
 * Copyright (c) 2025, libokun(2100370548@qq.com). All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package easy4j.infra.rpc.integrated.spring.annotations;

import java.lang.annotation.*;

/**
 * <pre>
 * 标注位置（只在提供方标注）
 * -------------------------
 * 接口:
 * -------------------------
 * 标在接口上之后
 * 消费方如果使用接口代理的方式调用的话，
 * 成员变量上的RpcProxy注解可以不用指定服务名称了
 * -------------------------
 * 实现类：
 * -------------------------
 * 服务提供方的实现类可以不加这个注解，一样运行没有问题
 * 但是如果服务方的实现类加了RpcService之后
 * 1、就可以不加spring的依赖注入注解了
 * 2、它会被自动注入到spring的ioc容器中
 * 3、同时会额外的注册一个服务到注册中心去用来单独管理这个服务
 * --------------------------
 * </pre>
 *
 * @author bokun
 * @since 2025-11-29 15:57:49
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {

    /**
     * serviceName 服务名称
     */
    String serviceName() default "";


    /**
     * 是否禁用这个服务
     *
     * @return
     */
    boolean disabled() default false;
}