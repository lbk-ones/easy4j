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
package easy4j.module.sauth.filter;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * FilterConfig
 * 不用这个 拦截器统一管理
 *
 * @author bokun.li
 * @date 2025-05
 */
@Deprecated
public class FilterConfig implements WebMvcConfigurer {

//    Easy4jSecurityFilterInterceptor easy4jSecurityFilterInterceptor;
//
//    public FilterConfig(Easy4jSecurityFilterInterceptor easy4jSecurityFilterInterceptor) {
//        this.easy4jSecurityFilterInterceptor = easy4jSecurityFilterInterceptor;
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry
//                .addInterceptor(easy4jSecurityFilterInterceptor)
//                .order(Integer.MIN_VALUE + 1)
//                .pathMatcher(new AntPathMatcher("/**"));
//    }
}
