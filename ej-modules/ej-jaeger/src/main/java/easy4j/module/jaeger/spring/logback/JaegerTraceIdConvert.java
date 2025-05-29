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

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import easy4j.module.jaeger.ThreadLocalTools;
import io.jaegertracing.internal.JaegerSpan;
import io.jaegertracing.internal.propagation.TextMapCodec;
import io.opentracing.util.GlobalTracer;

/**
 * JaegerTraceIdConvert
 *
 * @author bokun.li
 * @date 2025-05
 */
public class JaegerTraceIdConvert extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        String traceId = "";
        if(GlobalTracer.get() != null && GlobalTracer.get().activeSpan() != null){
            traceId = TextMapCodec.contextAsString(((JaegerSpan)GlobalTracer.get().activeSpan()).context());
        }
        return "trace[" + traceId + "]";
    }
}
