package ej.spring.boot.admin.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
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

        http.authorizeRequests()
                // 允许访问静态资源
                .antMatchers(adminServer.path("/assets/**")).permitAll()
                .antMatchers(adminServer.path("/login")).permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
                .and()
                // 配置登录表单
                .formLogin()
                .loginPage(adminServer.path("/login"))
                .successHandler(successHandler)
                //.defaultSuccessUrl(adminServer.path("/"), true)
                .and()
                // 配置注销
                .logout()
                .logoutUrl(adminServer.path("/logout"))
                .logoutSuccessUrl(adminServer.path("/login?logout")) // 退出成功后跳转
                .and()
                // 启用 HTTP 基本认证（用于客户端注册）
                .httpBasic()
                .and()
                // 禁用 CSRF 保护（对于特定端点）
                .csrf()
                .ignoringRequestMatchers(
                        new AntPathRequestMatcher(adminServer.path("/logout")),
                        new AntPathRequestMatcher(adminServer.path("/instances")),
                        new AntPathRequestMatcher(adminServer.path("/actuator/**"))
                );

        return http.build();
    }
}