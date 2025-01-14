package easy4j.module.jaeger.spring.logback;

import ch.qos.logback.classic.PatternLayout;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;

public class ApplicationRunListener implements ApplicationListener<ApplicationStartingEvent> {

    public static final String LOG_BACK = "traceId";

    @Override
    public void onApplicationEvent(ApplicationStartingEvent event) {
        PatternLayout.DEFAULT_CONVERTER_MAP.put(LOG_BACK, JaegerTraceIdConvert.class.getName());
    }
}
