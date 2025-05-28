package easy4j.module.jaeger.spring.logback;

import ch.qos.logback.classic.PatternLayout;
import easy4j.module.base.utils.SysConstant;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

public class ApplicationRunListener implements ApplicationListener<ApplicationStartingEvent> {

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        PatternLayout.DEFAULT_CONVERTER_MAP.put(SysConstant.TRACE_ID_NAME, JaegerTraceIdConvert.class.getName());
    }
}
