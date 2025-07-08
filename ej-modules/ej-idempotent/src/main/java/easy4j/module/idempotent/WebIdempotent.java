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
package easy4j.module.idempotent;

import easy4j.infra.common.annotations.Desc;

import java.lang.annotation.*;

/**
 * WebIdempotent
 *
 * @author bokun.li
 * @date 2025-05
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebIdempotent {

    // 唯一key的获取方式
    @Desc("唯一key的获取方式 默认是获取X-Access-Token来校验的")
    String keyGeneratorType() default "token";

    @Desc("存储方式 默认db 代表把唯一key存储到数据库中去")
    StorageTypeEnum storageType() default StorageTypeEnum.DB;

    @Desc("key的过期时间")
    int expireSeconds() default 60 * 5;

    /**
     * 全局幂等，该接口只能一个一个来，以请求方法加请求路径来确定
     *
     * @author bokun.li
     * @date 2025/7/8
     */
    @Desc("全局幂等，该接口只能一个一个来，以请求方法加请求路径来确定")
    boolean globalIdempotent() default false;

    /**
     * 没有唯一key(或者免登录)则降级为全局幂等,默认不开启
     *
     * @author bokun.li
     * @date 2025/7/8
     */
    @Desc("没有唯一key则降级为全局幂等,默认不开启")
    boolean degradeGlobalIdempotent() default false;

}
