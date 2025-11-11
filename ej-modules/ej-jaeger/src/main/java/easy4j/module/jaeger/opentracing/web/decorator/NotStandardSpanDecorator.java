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
package easy4j.module.jaeger.opentracing.web.decorator;

import cn.hutool.core.date.DatePattern;
import easy4j.module.jaeger.opentracing.web.ServletFilterSpanDecorator;
import io.opentracing.Span;
import io.opentracing.tag.StringTag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
