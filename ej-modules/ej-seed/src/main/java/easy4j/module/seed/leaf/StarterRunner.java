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
package easy4j.module.seed.leaf;

import easy4j.infra.context.AutoRegisterContext;
import easy4j.infra.context.Easy4jContext;
import easy4j.module.seed.CommonKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;

/**
 * StarterRunner
 *
 * @author bokun.li
 * @date 2025-05
 */
@Slf4j
public class StarterRunner implements CommandLineRunner, AutoRegisterContext {

    @Resource
    private LeafGenIdService idGenService;

    @Override
    public void run(String... args) throws Exception {
        idGenService.init();
    }

    @Override
    public void registerToContext(Easy4jContext easy4jContext) {
        easy4jContext.register(CommonKey.getCommonKey());
    }
}
