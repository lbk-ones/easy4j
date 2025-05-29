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
package easy4j.module.datasource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@EnableConfigurationProperties({DataSourceProperties.class})
@Configuration
public class Config {

    @Bean
    @ConfigurationProperties("spring.datasource.druid.filter.log4j3")
    @ConditionalOnProperty(
            prefix = "spring.datasource.druid.filter.log4j3",
            name = {"enabled"}
    )
    @ConditionalOnMissingBean
    public Log4j3Filter log4j3Filter() {
        return new Log4j3Filter();
    }

}
