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
package easy4j.module.security.config;

import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.utils.SysConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.http.HttpSession;

/**
 * 传统web服务 Spring Security 配置
 * 传统web服务部配置
 * 登录界面
 */
@Configuration
@ModuleBoolean(SysConstant.EASY4J_SECURITY_OLD_SCHOOL)
public class OldSchoolSecurityConfig {
    public static final String LOGIN_ROUTER = "/login.html";
    public static final String HOME_ROUTER = "/index";
    public static final String LOGIN_FAILS_ROUTER = "/login.html";
    public static final String LOGIN_API = "/loginAction";
    public static final String LOG_OUT_URL = "/logout";


    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests()
                .mvcMatchers(LOGIN_ROUTER)
                .permitAll()
                .anyRequest()
                .authenticated()
                //.antMatchers("")                                  // Ant风格匹配  ? 耽搁字符 * 0和多个不包括/ **0或多个路径分片包括/
                //.regexMatchers("")                                // 正则
                //.requestMatchers ("")                             // 自定义
                .and()
                .formLogin()
                .loginPage(LOGIN_ROUTER)// 登录页面
                .loginProcessingUrl(LOGIN_API) // 登录接口 前端 表单访问的地址
                //.successForwardUrl("/index") // 登录成功之后forward服务器内部跳转到 index界面
                .defaultSuccessUrl(HOME_ROUTER, true) // redirect 登录成功之后重定向到的界面 true代表不会进入上次进入的页面 false代表会后进入之前进入的地址页面
                .failureHandler((request, response, exception) -> {
                    String errorMessage;
                    if (exception instanceof BadCredentialsException) {
                        errorMessage = "用户名或密码错误";
                    } else if (exception instanceof LockedException) {
                        errorMessage = "账户已锁定";
                    } else {
                        errorMessage = "认证失败：" + exception.getMessage();
                    }
                    // 将错误消息存入Session
                    HttpSession session = request.getSession();
                    session.setAttribute("loginError", errorMessage);
                    // 重定向到登录页面
                    response.sendRedirect(LOGIN_FAILS_ROUTER + "?error");
                })
                //.failureUrl(LOGIN_FAILS_URL) // 重定向错误地址
                //.failureForwardUrl(LOGIN_FAILS_URL) // 请求转发到错误地址
                //.successHandler()  //  用于自定义返回json的
                .and()
                .logout()
                .logoutUrl(LOG_OUT_URL)
                .logoutSuccessUrl(LOGIN_ROUTER)
//                .logoutSuccessHandler(new LogoutSuccessHandler() {
//                    @Override
//                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//                    }
//                }) // 退出成功处理器

                .clearAuthentication(true)
                .deleteCookies()
                .invalidateHttpSession(true)

                .and()
                //.csrf().disable()// 关闭csrf 前后端不分离的时候需要这个

                .build();
    }*/
}
