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

import easy4j.infra.common.utils.BusCode;

import java.lang.annotation.*;

/**
 * 需要有某个角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasRole {

    /**
     * 角色编码
     *
     * @author bokun.li
     * @date 2025-07-27
     */
    String[] value() default {};


    /**
     * 是否要同时拥有所有角色才生效
     * @return
     */
    boolean and() default false;


    /**
     * 提示消息
     *
     * @return
     */
    String message() default BusCode.A00051;
}
