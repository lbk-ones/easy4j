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

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * <pre>
 * RpcService
 * 标注在变量上面(变量必须是接口其他不行)，只要标注了这个注解就给他自动生成一个代理 并赋值
 * 两种可能：
 * 1、传入服务名称，直接去注册中心找到服务对应的ip端口
 * 2、不传入服务名称，根据参数类型的全类名去找他属于哪一个服务（服务注册的时候就要将这个映射关系注册到注册中心去） ，再通过服务名称找到注册信息
 * </pre>
 *
 * @author bokun
 * @since 2025-11-29 15:57:49
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcProxy {

    /**
     * 服务名称
     */
    String value() default "";

    /**
     * 超时时间
     */
    long timeOut() default 30 * 1000L;

    /**
     * 是否广播
     */
    boolean broadcast() default false;

    /**
     * 是否异步广播，broadcast必须为true才生效
     */
    boolean broadcastAsync() default false;

    /**
     * 直连地址
     */
    String url() default "";

    /**
     * 最大重试次数 默认三次 服务治理 > 注解配置 > 配置文件
     */
    int invokeRetryMaxCount() default 3;
}