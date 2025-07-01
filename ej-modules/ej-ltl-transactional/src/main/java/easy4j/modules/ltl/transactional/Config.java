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
package easy4j.modules.ltl.transactional;


import easy4j.infra.common.module.ModuleBoolean;
import easy4j.modules.ltl.transactional.component.LtTransactionalAspect;
import easy4j.modules.ltl.transactional.component.LtlTransactionService;
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
@ConditionalOnBean(DataSource.class)
public class Config {
    public static final String EASY4J_LTL_ENABLE = "ltl.enable";

    @Bean
    @ModuleBoolean(EASY4J_LTL_ENABLE)
    public LtTransactionalAspect ltTransactionalAspect() {

        return new LtTransactionalAspect();
    }

    @Bean
    @ModuleBoolean(EASY4J_LTL_ENABLE)
    public LtlTransactionService ltlTransactionService() {

        return new LtlTransactionService();
    }

}
