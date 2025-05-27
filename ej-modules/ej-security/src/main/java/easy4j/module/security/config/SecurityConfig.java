package easy4j.module.security.config;

import easy4j.module.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 5.7.11 官方已经不推荐 继承 WebSecurityConfigurerAdapter 去实现配置改变 更推荐 以 @Configure 注解 和 @Bean形式去重写配置
 * <p>
 * 所以引入了 HttpSecurity 这一bean
 *
 * @see org.springframework.security.config.annotation.web.configuration.HttpSecurityConfiguration#httpSecurity
 */
public class SecurityConfig {
    // 前后端分离
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 禁用Session
                .and()
                .csrf()
                .disable() // 禁用CSRF（无状态应用通常不需要）
                .formLogin()
                .disable() // 禁用表单登录（如果使用）
                .httpBasic()
                .disable() // 禁用Basic认证（如果使用）
                .authorizeRequests()
                .anyRequest()
                .authenticated();

        // 添加JWT过滤器（如果使用JWT）
        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtFilter() {
        return new JwtAuthenticationFilter();
    }

}
