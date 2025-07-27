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
package easy4j.module.sauth.annotations;

import easy4j.infra.common.annotations.Desc;
import easy4j.infra.common.utils.BusCode;

import java.lang.annotation.*;

/**
 * 是否需要权限
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    /**
     * 权限编码
     *
     * @author bokun.li
     * @date 2025-07-27
     */
    @Desc("权限编码")
    String[] value() default {};

    /**
     * 权限组,由权限扩展接口传入
     *
     * @author bokun.li
     * @date 2025-07-27
     */
    @Desc("权限组,由权限扩展接口传入")
    String[] group() default {};


    /**
     * 是否要同时满足所有设置的权限，权限组和权限编码都适用，默认不用全部满足，默认只满足一个就行
     *
     * @author bokun.li
     * @date 2025-07-27
     */
    @Desc("是否要同时满足所有设置的权限，权限组和权限编码都适用，默认不用全部满足，默认只满足一个就行")
    boolean and() default false;


    /**
     * 提示消息可以是i18n码也可以直接是想要提示的内容
     *
     * @return
     */
    @Desc("提示消息可以是i18n码也可以直接是想要提示的内容")
    String message() default BusCode.A00051;

}
