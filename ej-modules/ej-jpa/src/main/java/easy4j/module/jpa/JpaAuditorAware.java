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
package easy4j.module.jpa;

import easy4j.module.jpa.aware.EasyJpaAuditorAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Spring Data JPA 自动审计
 * @author bokun
 * @date 2023/5/4
 */
//@Component
@Slf4j
public class JpaAuditorAware implements AuditorAware<String> {

    @Autowired
    EasyJpaAuditorAware easyJpaAuditorAware;

    @Override
    public Optional<String> getCurrentAuditor() {
        return easyJpaAuditorAware.getCurrentAuditor();
    }
}
