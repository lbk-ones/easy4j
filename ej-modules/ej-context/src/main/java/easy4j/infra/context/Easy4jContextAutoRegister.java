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

import easy4j.infra.common.utils.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


import java.util.Map;

/**
 * AutoRegister
 * auto register all AutoRegisterContext
 *
 * @author bokun.li
 * @date 2025-06-07 13:45:58
 */
@Slf4j
public class Easy4jContextAutoRegister implements ApplicationListener<ContextRefreshedEvent> {

    boolean isInit = false;


    @Autowired
    Easy4jContext easy4jContext;


    // all bean is loaded
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (!isInit && applicationContext.getParent() == null) {
            isInit = true;

            Map<String, AutoRegisterContext> contextBeans =
                    applicationContext.getBeansOfType(AutoRegisterContext.class);

            log.info(SysLog.compact("find " + contextBeans.size() + " context"));
            for (AutoRegisterContext context : contextBeans.values()) {
                long beginTime = System.currentTimeMillis();
                context.registerToContext(easy4jContext);
                log.info(SysLog.compact(context.getName() + " context init success! in " + (System.currentTimeMillis() - beginTime) + " ms "));
            }
            // publish event
            applicationContext.publishEvent(new RegisterEvent(this, easy4jContext));
        }
    }


}