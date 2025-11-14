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

import easy4j.infra.common.utils.SysConstant;
import easy4j.module.sauth.authentication.AuthenticationType;
import easy4j.module.sauth.authentication.IBearerAuthentication;
import easy4j.module.sauth.authentication.LoadAuthentication;

import java.lang.annotation.*;

/**
 * 开放api 一般是
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenApi {

    /**
     * 请求头名称 默认 Authorization
     * @return
     */
    String tokenHeaderName() default SysConstant.AUTHORIZATION;


    /**
     * 默认bearerToken
     * @return
     */
    AuthenticationType authenticationType() default AuthenticationType.BearerToken;

    /**
     * 访问口令
     * 并不是这里写了什么 对面就要传什么 只有与配置了的口令对应起来才可以
     *
     * @see easy4j.module.sauth.authentication.AccessTokenAuthentication
     */
    String accessToken() default "";


    /**
     * bearerToken鉴权方式实现
     * @see IBearerAuthentication
     */
    Class<? extends IBearerAuthentication> bearerImpl() default IBearerAuthentication.class;

    /**
     * 其他鉴权方式实现
     * @see easy4j.module.sauth.authentication.OtherAuthentication
     */
    Class<? extends LoadAuthentication> otherImpl() default LoadAuthentication.class;



}
