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
package easy4j.infra.context;


import easy4j.infra.common.utils.ServiceLoaderUtils;

import java.util.List;

/**
 * ContextPlugins
 *
 * @author bokun.li
 * @date 2025-06-07 18:36:12
 */
public class ContextPlugins {

    private static final List<ContextChannel> contextChannelList = ServiceLoaderUtils.load(ContextChannel.class);

    public static void init(Easy4jContext easy4jContext) {
        if (easy4jContext == null) return;
        for (ContextChannel contextChannel : contextChannelList) contextChannel.init(easy4jContext);
    }

    // 主动调用子模块去获取
    public static <T> T call(String name, Class<T> aclass) {
        for (ContextChannel contextChannel : contextChannelList) {
            T listener = contextChannel.listener(name, aclass);
            if (listener != null) return listener;
        }
        return null;
    }


}
