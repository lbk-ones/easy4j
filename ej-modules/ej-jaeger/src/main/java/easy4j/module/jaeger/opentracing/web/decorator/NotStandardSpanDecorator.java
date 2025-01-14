package easy4j.module.jaeger.opentracing.web.decorator;

import cn.hutool.core.date.DatePattern;
import easy4j.module.jaeger.opentracing.web.ServletFilterSpanDecorator;
import io.opentracing.Span;
import io.opentracing.tag.StringTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 非标TAG处理器
 * 接收时间 reqTime
 * 返回时间 resTime
 */
public class NotStandardSpanDecorator implements ServletFilterSpanDecorator {

    private String prefix;

    public static ServletFilterSpanDecorator NOT_STANDARD_TAGS = new NotStandardSpanDecorator();

    @Override
    public void onRequest(HttpServletRequest httpServletRequest, Span span) {
        buildTag("reqTime").set(span, DatePattern.NORM_DATETIME_MS_FORMAT.format(new Date()));
        buildTag("serverHost").set(span, httpServletRequest.getRemoteHost());
    }

    @Override
    public void onResponse(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Span span) {
        buildTag("resTime").set(span, DatePattern.NORM_DATETIME_MS_FORMAT.format(new Date()));
    }

    @Override
    public void onError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Throwable exception, Span span) {
    }

    @Override
    public void onTimeout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, long timeout, Span span) {

    }

    private StringTag buildTag(String tag) {
        if (prefix == null) {
            return new StringTag(tag);
        }
        return new StringTag(prefix + tag);
    }
}
