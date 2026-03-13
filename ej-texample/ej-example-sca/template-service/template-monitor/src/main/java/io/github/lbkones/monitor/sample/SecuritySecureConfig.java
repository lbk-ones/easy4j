/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lbkones.monitor.sample;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import java.util.UUID;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

//@Profile("secure")
// tag::configuration-spring-security[]
@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig {

	private final AdminServerProperties adminServer;

	private final SecurityProperties security;

	public SecuritySecureConfig(AdminServerProperties adminServer, SecurityProperties security) {
		this.adminServer = adminServer;
		this.security = security;
	}

	// 处理 edge浏览器 会自动访问/.well-known/appspecific/com.chrome.devtools.json的问题
	@Bean
	public RequestCache requestCache() {
		return new HttpSessionRequestCache() {
			@Override
			public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
				// 排除 Chrome DevTools 的探测 URL
				String requestUri = request.getRequestURI();
				if (!requestUri.contains("/.well-known/appspecific/com.chrome.devtools.json")) {
					// 仅保存非 DevTools 的请求
					super.saveRequest(request, response);
				}
			}

			@Override
			public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
				// 若请求是 DevTools URL，返回 null（不使用保存的请求）
				String requestUri = request.getRequestURI();
				if (requestUri.contains("/.well-known/appspecific/com.chrome.devtools.json")) {
					return null;
				}
				return super.getRequest(request, response);
			}
		};
	}

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
		successHandler.setTargetUrlParameter("redirectTo");
		successHandler.setDefaultTargetUrl(this.adminServer.path("/"));
		PathPatternRequestMatcher.Builder builder = PathPatternRequestMatcher.withDefaults();
		http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests //
			.requestMatchers(
					builder.matcher(this.adminServer.path("/assets/**")),
					builder.matcher((this.adminServer.path("/actuator/info"))),
					builder.matcher(adminServer.path("/actuator/health")),
					builder.matcher(this.adminServer.path("/login")),
					builder.matcher(this.adminServer.path("/.well-known/appspecific/com.chrome.devtools.json"))
			)
			.permitAll()
			.dispatcherTypeMatchers(DispatcherType.ASYNC)
			.permitAll() // https://github.com/spring-projects/spring-security/issues/11027
			.anyRequest()
			.authenticated())
			.formLogin(
					(formLogin) -> formLogin.loginPage(this.adminServer.path("/login"))
					.successHandler(successHandler)
			)
			.logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")));
//			.httpBasic(Customizer.withDefaults());

		http.addFilterAfter(new CustomCsrfFilter(), BasicAuthenticationFilter.class)
			.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
				.ignoringRequestMatchers(
						builder.matcher(POST, this.adminServer.path("/instances")), // <6>
						builder.matcher(DELETE, this.adminServer.path("/instances/*")), // <6>
						builder.matcher(this.adminServer.path("/actuator/**")) // <7>
				));

		http.rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));

		return http.build();

	}

	// Required to provide UserDetailsService for "remember functionality"
	@Bean
	public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
		String name = security.getUser().getName();
		String password = security.getUser().getPassword();
		UserDetails user = User.withUsername(name).password(passwordEncoder.encode(password)).roles("ADMIN").build();
		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
// end::configuration-spring-security[]
