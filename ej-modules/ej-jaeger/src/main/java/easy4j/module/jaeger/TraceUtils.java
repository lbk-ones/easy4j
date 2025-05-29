package easy4j.module.jaeger;

import io.opentracing.Span;
import io.opentracing.tag.StringTag;
import io.opentracing.tag.Tags;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * TraceUtils
 *
 * @author bokun.li
 * @date 2025-05
 */
public class TraceUtils {

    public static Map<String, String> logsForException(Throwable throwable) {
        Map<String, String> errorLog = new HashMap<>(3);
        errorLog.put("event", Tags.ERROR.getKey());

        String message = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
        if (message != null) {
            errorLog.put("message", message);
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        errorLog.put("stack", sw.toString());

        return errorLog;
    }

    // tag
    public static void buildTag(String tagName, String value, Span span){
        StringTag stringTag = new StringTag(tagName);
        stringTag.set(span,value);
    }
}