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

import easy4j.infra.common.utils.SysLog;
import easy4j.infra.dbaccess.DBAccessFactory;
import io.seata.spring.annotation.GlobalTransactionScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;

/**
 * Config
 *
 * @author bokun.li
 * @date 2025/6/27
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(value = GlobalTransactionScanner.class)
public class Config implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info(SysLog.compact("seata module fence begin init..."));
        DBAccessFactory.initDb("db/fence");
    }

}
