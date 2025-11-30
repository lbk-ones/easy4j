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
 * RpcService
 * 标注在类上面 暴露服务
 *
 * @author bokun
 * @since 2025-11-29 15:57:49
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcService {

    /**
     * 如果有多个实现接口 这个标识它的真正 接口类是什么
     *
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 是否暴露出去
     *
     * @return
     */
    boolean export() default true;


    /**
     * 是否注册到注册中心去
     *
     * @return
     */
    boolean register() default true;
}