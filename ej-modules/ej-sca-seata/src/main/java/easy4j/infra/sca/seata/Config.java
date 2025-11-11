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
package easy4j.infra.sca.seata;

import easy4j.infra.common.module.ModuleBoolean;
import easy4j.infra.common.utils.SysConstant;
import easy4j.infra.common.utils.SysLog;
import easy4j.infra.dbaccess.DBAccessFactory;
import org.apache.seata.spring.boot.autoconfigure.SeataCoreAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.core.PriorityOrdered;


/**
 * Config
 *
 * @author bokun.li
 * @date 2025/6/27
 */
@Slf4j
@AutoConfigureBefore(value = {SeataCoreAutoConfiguration.class})
@ModuleBoolean(SysConstant.EASY4J_SEATA_ENABLE)
public class Config {

    public Config() {
    }

    public static class ConfigStartBeanPostProcessor extends AbstractAutoProxyCreator implements PriorityOrdered {


        private boolean actionStarting = false;

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
            if (!actionStarting) {
                // 设置并启动
                actionStarting = true;
                log.info(SysLog.compact("seata module fence begin init..."));
                DBAccessFactory.initDb("db/fence");
            }
            return bean;
        }

        @Override
        protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException {
            return new Object[0];
        }
    }

    @Bean
    public static ConfigStartBeanPostProcessor start() {
        return new ConfigStartBeanPostProcessor();
    }


}
