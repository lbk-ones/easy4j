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
package easy4j.infra.knife4j;

import easy4j.infra.common.annotations.Desc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ControllerModule
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ControllerModule {
    /**
     * 模块名称
     *
     * @return 模块名称
     */
    @Desc("地址 尽量以英文开头 最好和 @RestController类上面的 @RequestMapping路径保持一致")
    String name() default "";

    /**
     * 描述信息
     *
     * @return 描述信息
     */
    @Desc("尽量以中文")
    String description() default "";

}
