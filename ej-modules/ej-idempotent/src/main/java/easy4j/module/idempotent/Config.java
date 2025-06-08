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
package easy4j.module.idempotent;

import easy4j.infra.context.api.idempotent.Easy4jIdempotentKeyGenerator;
import easy4j.infra.context.api.idempotent.Easy4jIdempotentStorage;
import easy4j.module.idempotent.rules.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@Configuration
@ConditionalOnBean(value = {DataSource.class})
public class Config {
//    @Bean(name = "easy4jIdempotentWebConfig")
//    public WebConfig webConfig() {
//        return new WebConfig();
//    }

//    @Bean("idempotentHandlerInterceptor")
//    public IdempotentHandlerInterceptor idempotentHandlerInterceptor() {
//        return new IdempotentHandlerInterceptor();
//    }

    @Bean
    public IdempotentToolFactory idempotentToolFactory() {
        return new IdempotentToolFactory();
    }


    @Bean("headerKeyGenerator")
    public Easy4jIdempotentKeyGenerator takeKeyGenerator() {
        return new HeaderEasy4jIdempotentKeyGenerator();
    }

    @Bean("queryKeyGenerator")
    public Easy4jIdempotentKeyGenerator queryKeyGenerator() {
        return new QueryEasy4jIdempotentKeyGenerator();
    }

    @Bean("formKeyGenerator")
    public Easy4jIdempotentKeyGenerator formKeyGenerator() {
        return new FormEasy4jIdempotentKeyGenerator();
    }

    @Bean("tokenKeyGenerator")
    public Easy4jIdempotentKeyGenerator tokenKeyGenerator() {
        return new TokenEasy4jIdempotentKeyGenerator();
    }

    @Bean("dbIdempotentStorage")
    public Easy4jIdempotentStorage dbIdempotentStorage() {
        return new DbEasy4jIdempotentStorage();
    }


}
