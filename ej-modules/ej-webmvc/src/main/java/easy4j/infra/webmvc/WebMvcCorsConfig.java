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


import easy4j.infra.common.module.Module;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * WebMvcCorsConfig
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
@Configuration
@Module(SysConstant.GLOBAL_CORS_ENABLE + ":true")
public class WebMvcCorsConfig implements WebMvcConfigurer, InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(SysLog.compact("SpringMVC允许跨域已开启"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 匹配所有接口
                .allowedOriginPatterns("*")  // 允许所有来源（生产环境建议指定具体域名）
                .allowedMethods("GET, POST, PUT, DELETE, OPTIONS")  // 允许所有请求方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(false) // 允许携带 Cookie（需与前端配合，域名需一致）
                .maxAge(3600); // 预检请求的有效期（秒）
    }
}
