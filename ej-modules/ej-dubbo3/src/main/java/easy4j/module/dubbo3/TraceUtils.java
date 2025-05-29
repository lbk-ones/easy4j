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
package easy4j.module.dubbo3;

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
