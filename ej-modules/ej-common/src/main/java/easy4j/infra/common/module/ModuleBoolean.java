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
package easy4j.infra.common.module;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 根据配置来决定是否启用这个模块 默认是不启用的 默认是false
 *
 * @Bean
 * @Moodule("xxx.enable") public XXX xxx() {
 * return new XXX();
 * }
 * <p>
 * <p>
 * <br/>
 * 说人话就是 当 easy4j.xxx.enable=true 的时候，才会启用这个模块(才会加载 XXX 这个 bean)
 * <br/>
 * 如果配置成 xxx.enable:true 默认就是开启
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional(ModuleCondition.class)
public @interface ModuleBoolean {

    /**
     * 组件名称 不能带前缀
     */
    String[] value();
}
