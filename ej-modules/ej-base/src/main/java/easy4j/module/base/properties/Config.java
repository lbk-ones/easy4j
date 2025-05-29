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
package easy4j.module.base.properties;


import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static easy4j.module.base.log.DefLog.*;


/**
 * Config
 *
 * @author bokun.li
 * @date 2025-05
 */
@EnableConfigurationProperties({EjSysProperties.class})
@Slf4j
@AutoConfigureBefore({SpringApplicationAdminJmxAutoConfiguration.class})
public class Config implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollUtil.isNotEmpty(infoLine) && log.isInfoEnabled()) {
            for (String s : infoLine) {
                log.info(s);
            }
            infoLine.clear();
        }
        if (CollUtil.isNotEmpty(warnLine) && log.isWarnEnabled()) {
            for (String s : warnLine) {
                log.warn(s);
            }
            warnLine.clear();
        }
        if (CollUtil.isNotEmpty(errorLine) && log.isErrorEnabled()) {
            for (String s : errorLine) {
                log.error(s);
            }
            errorLine.clear();
        }

        if (CollUtil.isNotEmpty(debugLine) && log.isDebugEnabled()) {
            for (String s : debugLine) {
                log.debug(s);
            }
            debugLine.clear();
        }

        if (CollUtil.isNotEmpty(traceLine) && log.isTraceEnabled()) {
            for (String s : traceLine) {
                log.trace(s);
            }
            traceLine.clear();
        }
    }
}
