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

import easy4j.infra.rpc.integrated.spring.config.BeanImport;
import easy4j.infra.rpc.integrated.spring.config.RpcServiceProcessor;
import org.springframework.context.annotation.Import;

/**
 * EnableEasy4jRpc
 * 是否启用 rpc
 *
 * @author bokun
 * @since 2025-11-29 16:54:09
 */
@Import(value = {BeanImport.class, RpcServiceProcessor.class})
public @interface EnableEasy4jRpc {

    /**
     * 基本路径，扫描指定路径 暴露服务，将元数据注册到注册中心去
     *
     * @return
     */
    String[] basePackage() default {};

}