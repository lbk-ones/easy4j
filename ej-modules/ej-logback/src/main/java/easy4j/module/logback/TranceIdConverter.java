package easy4j.module.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * TranceIdConverter
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TranceIdConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        return "[]";
    }
}