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