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
package easy4j.infra.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-06-07 13:47:41
 */
@Configuration(proxyBeanMethods = false)
public class Config {


    @Bean
    @Primary
    public Easy4jContext easy4jContext() {
        return new SingleEasy4J4jContext();
    }


    @Bean
    public Easy4jContextAutoRegister autoRegister() {
        return new Easy4jContextAutoRegister();
    }

    @Bean
    public BaseAutoRegisterContext baseAutoRegisterContext() {
        return new BaseAutoRegisterContext();
    }


}