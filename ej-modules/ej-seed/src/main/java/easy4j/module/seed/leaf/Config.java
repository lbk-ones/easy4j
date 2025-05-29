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

import easy4j.module.base.utils.SysLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


/**
 * leaf 需要在有数据源的情况下才启动
 */
@Configuration
@ConditionalOnBean(value = {DataSource.class})
public class Config implements InitializingBean {


    public Logger logger = LoggerFactory.getLogger(Config.class);

    @Bean
    public LeafGenIdService leafGenIdService() {
        return new SegmentLeafGenIdServiceImpl();
    }

    @Bean
    public LeafAllocDao leafAllocDao() {
        return new LeafAllocDaoImpl();
    }

    @Bean
    public StarterRunner starterRunner() {
        return new StarterRunner();
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info(SysLog.compact("LEAF 分布式主键开始初始化"));

    }


}
