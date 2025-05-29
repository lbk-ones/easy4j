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
package easy4j.module.base.log;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

/**
 * DefLog
 *
 * @author bokun.li
 * @date 2025-05
 */
public class DefLog {

    public static final List<String> infoLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> warnLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> errorLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> debugLine = Collections.synchronizedList(Lists.newLinkedList());
    public static final List<String> traceLine = Collections.synchronizedList(Lists.newLinkedList());


    public void info(String e){
        infoLine.add(e);
    }

    public  void debug(String e){
        debugLine.add(e);
    }

    public  void trace(String e){
        traceLine.add(e);
    }

    public void error(String e){
        errorLine.add(e);
    }
}
