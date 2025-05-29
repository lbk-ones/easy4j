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
package easy4j.module.jaeger.spring.logback;

import ch.qos.logback.classic.PatternLayout;
import easy4j.module.base.utils.SysConstant;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

/**
 * ApplicationRunListener
 *
 * @author bokun.li
 * @date 2025-05
 */
public class ApplicationRunListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        PatternLayout.DEFAULT_CONVERTER_MAP.put(SysConstant.TRACE_ID_NAME, JaegerTraceIdConvert.class.getName());
    }
}
