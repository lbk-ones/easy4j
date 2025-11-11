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
package easy4j.infra.webmvc;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * WebMvcHandler
 *
 * @author bokun.li
 */
public interface Easy4JWebMvcHandler {


    boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod);

    void postHandle(HttpServletRequest request, HttpServletResponse response, ModelAndView modelAndView, HandlerMethod handlerMethod);

    void afterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex, HandlerMethod handlerMethod);

    default Integer getOrder() {
        return 999;
    }

}
