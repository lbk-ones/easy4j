package io.github.lbkones.monitor.ne2;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import easy4j.infra.base.starter.env.Easy4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

//@Configuration
//@EnableWebSecurity
public class SecurityConfig {
    private final AdminServerProperties adminServer;

    public SecurityConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(adminServer.path("/"));
        successHandler.setAlwaysUseDefaultTargetUrl(true);
        PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();
        http
                .authorizeHttpRequests(authorize -> authorize
                        // 允许静态资源、登录页匿名访问
                        .requestMatchers(adminServer.path("/assets/**")).permitAll()
                        .requestMatchers(adminServer.path("/login")).permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 表单登录配置
                .formLogin(form -> form
                        .loginPage(adminServer.path("/login")) // 自定义登录页路径
                        .successHandler(successHandler) // 登录成功处理器
                        .permitAll() // 允许匿名访问登录页（显式声明更安全）
                )
                .logout(logout -> logout
                        .logoutUrl(adminServer.path("/logout"))
                        .addLogoutHandler(new LogoutHandler() {
                            @Override
                            public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                                Easy4j.info("接收到退出请求"+request.getRequestURI()+"方法:"+request.getMethod());
                            }
                        })// 匹配你的退出接口路径
                        .logoutSuccessUrl(adminServer.path("/login?logout"))
                        .invalidateHttpSession(true) // 销毁 Session
                        .deleteCookies("JSESSIONID") // 删除认证 Cookie
                        .permitAll() // 允许所有用户访问退出接口
                )
                //.httpBasic(Customizer.withDefaults())
                // CSRF 配置
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                builder.matcher(adminServer.path("/logout")),
                                builder.matcher(adminServer.path("/instances")),
                                builder.matcher(adminServer.path("/actuator/**"))
                        )
                );


        return http.build();
    }
}