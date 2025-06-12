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
package ej.spring.boot.starter.server;


import easy4j.infra.base.starter.Easy4JStarter;
import easy4j.module.sentinel.EnableFlowDegrade;
import org.springframework.boot.SpringApplication;

@Easy4JStarter(
        serverPort = 9052,
        serverName = "test-ej-service",
        enableH2 = true
//        h2Url = "jdbc:h2:mem:test@easy4j:easy4j"
)
@EnableFlowDegrade
//@MapperScan("ej.spring.boot.starter.server.mapper")
/**
 * StartTest
 *
 * @author bokun.li
 * @date 2025-05
 */
public class StartTest {

    public static void main(String[] args) {
        SpringApplication.run(StartTest.class, args);
    }

}
