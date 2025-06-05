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
package ej.spring.boot.admin;


import de.codecentric.boot.admin.server.config.EnableAdminServer;
import easy4j.module.base.starter.Easy4JStarterNd;
import org.springframework.boot.SpringApplication;

/**
 * BootAdminServer
 * 文档：https://docs.spring-boot-admin.com/2.7.4/#_what_is_spring_boot_admin
 * <p>
 * boot admin 监控不要数据源
 *
 * @author bokun.li
 * @date 2025-06-05 21:53:25
 */
@Easy4JStarterNd(
        enableH2 = true
)
@EnableAdminServer
public class BootAdminServer {
    public static void main(String[] args) {
        SpringApplication.run(BootAdminServer.class, args);
    }
}
